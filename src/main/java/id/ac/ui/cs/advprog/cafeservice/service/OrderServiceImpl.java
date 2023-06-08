package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.*;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.model.order.Status;
import id.ac.ui.cs.advprog.cafeservice.model.order.RequestParam;
import id.ac.ui.cs.advprog.cafeservice.pattern.strategy.create.CreateFromCafe;
import id.ac.ui.cs.advprog.cafeservice.pattern.strategy.create.CreateFromWarnet;
import id.ac.ui.cs.advprog.cafeservice.pattern.strategy.create.CreateStrategy;
import id.ac.ui.cs.advprog.cafeservice.pattern.strategy.status.*;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final MenuItemService menuItemService;
    private final MenuItemRepository menuItemRepository;

    private RestTemplate restTemplate;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Value("${API_BAYAR}")
    private String apiBayar;

    @Value("${API_WARNET}")
    private String apiWarnet;

    @Override
    public List<Order> findByPagination(int page) {
        int offset = (page - 1) * 16;
        int next = offset + 16;
        return orderRepository.getByPage(offset, next);
    }

    @Override
    public Order findById(Integer id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty())
            throw new OrderDoesNotExistException(id);
        return order.get();
    }

    @Override
    public Order create(OrderRequest request, String from) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        var order = Order.builder().session(request.getSession()).build();
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        for (OrderDetailsData orderDetailsData : request.getOrderDetailsData()) {
            var menuItem = menuItemRepository.findById(orderDetailsData.getMenuItemId());
            if (menuItem.isEmpty()) {
                throw new MenuItemDoesNotExistException(orderDetailsData.getMenuItemId());
            }
            if (orderDetailsData.getQuantity() > menuItem.get().getStock()) {
                throw new MenuItemOutOfStockException(menuItem.get().getName());
            }

            CreateStrategy createStrategy;
            if (from != null && from.equalsIgnoreCase("warnet")) {
                createStrategy = new CreateFromWarnet(menuItem.get(), orderDetailsData);
            } else {
                createStrategy = new CreateFromCafe(menuItem.get(), orderDetailsData);
            }

            CompletableFuture<OrderDetails> orderDetailsFuture = CompletableFuture.supplyAsync(createStrategy::create, executorService);

            MenuItemRequest menuItemRequest = MenuItemRequest.builder()
                    .name(menuItem.get().getName())
                    .price(menuItem.get().getPrice())
                    .stock(menuItem.get().getStock() - orderDetailsData.getQuantity())
                    .build();
            menuItemService.update(menuItem.get().getId(), menuItemRequest);

            orderDetailsFuture.thenAcceptAsync(orderDetails ->
                    setOrderPC(request.getSession(), orderDetails, executorService), executorService);
            orderDetailsFuture.thenAcceptAsync(orderDetails ->
                    orderDetails.setOrder(order), executorService);

            OrderDetails orderDetails = orderDetailsFuture.join();
            orderDetailsRepository.save(orderDetails);
            orderDetailsList.add(orderDetails);
        }
        order.setOrderDetailsList(orderDetailsList);
        orderRepository.save(order);
        return order;
    }

    @Override
    public OrderDetails updateOrderDetailStatus(Integer orderDetailId, String status) {

        Optional<OrderDetails> optionalOrderDetails = orderDetailsRepository.findById(orderDetailId);

        if (optionalOrderDetails.isEmpty()) {
            throw new OrderDetailDoesNotExistException(orderDetailId);
        }

        OrderDetails orderDetails = optionalOrderDetails.get();

        if (orderDetails.getStatus().equals(Status.DONE.getValue()) || orderDetails.getStatus().equals(Status.CANCEL.getValue())) {
            throw new OrderDetailStatusInvalid(orderDetailId);
        }

        StatusStrategy statusStrategy = chooseStatusStrategy(status, orderDetails);

        statusStrategy.setStatus();

        orderDetailsRepository.save(orderDetails);

        return orderDetails;
    }

    private StatusStrategy chooseStatusStrategy(String status, OrderDetails orderDetails) {
        RequestParam statusEnum = RequestParam.fromString(status);
        switch (statusEnum) {
            case PREPARE -> {
                if (!orderDetails.getStatus().equals(Status.CONFIRM.getValue())) {
                    throw new UpdateStatusInvalid(orderDetails.getStatus(), Status.CONFIRM.getValue());
                }
                return new PrepareStatus(orderDetails, this, menuItemRepository);
            }
            case DELIVER -> {
                if (!orderDetails.getStatus().equals(Status.PREPARE.getValue())) {
                    throw new UpdateStatusInvalid(orderDetails.getStatus(), Status.DELIVER.getValue());
                }
                return new DeliverStatus(orderDetails, this, menuItemRepository);
            }
            case DONE -> {
                if (!orderDetails.getStatus().equals(Status.DELIVER.getValue())) {
                    throw new UpdateStatusInvalid(orderDetails.getStatus(), Status.DONE.getValue());
                }
                return new DoneStatus(orderDetails, this, menuItemRepository, restTemplate);
            }
            case CANCEL -> {
                return new CancelStatus(orderDetails, this, menuItemRepository);
            }
            default -> throw new BadRequest();
        }
    }

    @Override
    public void delete(Integer id) {
        if (isOrderDoesNotExist(id)) {
            throw new OrderDoesNotExistException(id);
        } else {
            orderRepository.deleteById(id);
        }
    }

    @Override
    public int getCount() {
        return orderRepository.getCount();
    }

    @Override
    public List<Order> findBySession(UUID session) {
        String url = apiWarnet + "/info_sesi/session_detail/" + session;
        try {
            getSessionDetails(url);
            Optional<List<Order>> orderBySession = orderRepository.findBySession(session);
            return orderBySession.orElseGet(ArrayList::new);
        } catch (HttpClientErrorException e) {
            throw new UUIDNotFoundException();
        }
    }

    public boolean isOrderDoesNotExist(Integer orderId) {
        return orderRepository.findById(orderId).isEmpty();
    }

    public void addToBill(OrderDetails orderDetails) throws JSONException {
        String url = apiBayar + "/bills";

        MenuItem orderedMenu = orderDetails.getMenuItem();
        JSONObject requestBody = new JSONObject();

        requestBody.put("name", orderedMenu.getName());
        requestBody.put("price", orderedMenu.getPrice());
        requestBody.put("quantity", orderDetails.getQuantity());
        requestBody.put("subTotal", (long) orderDetails.getTotalPrice());
        requestBody.put("sessionId", orderDetails.getOrder().getSession());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        restTemplate.postForObject(url, entity, String.class);
    }

    public void setOrderPC(UUID session, OrderDetails orderDetails, ExecutorService executorService) {
        String url = apiWarnet + "/info_sesi/session_detail/" + session;

        try {
            JSONObject sessionInfo = getSessionDetails(url).getJSONObject("session");
            JSONObject pcInfo = sessionInfo.getJSONObject("pc");

            CompletableFuture.runAsync(() -> orderDetails.setIdPC(pcInfo.getInt("id")));
            CompletableFuture.runAsync(() -> orderDetails.setNoPC(pcInfo.getInt("noPC")));
            CompletableFuture.runAsync(() -> orderDetails.setNoRuangan(pcInfo.getInt("noRuangan")));

        } catch (HttpClientErrorException e) {
            throw new UUIDNotFoundException();
        }
    }

    public JSONObject getSessionDetails(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        HttpEntity<String> entityResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String response = entityResponse.getBody();
        return new JSONObject(response);
    }
}
