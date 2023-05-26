package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.*;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
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
    private static final String CANCELLED_STATUS = "Dibatalkan";

    private static final String DONE_STATUS = "Selesai";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

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
                    setOrderPC(request.getSession(), orderDetails, executorService), executorService).join();
            orderDetailsFuture.thenAcceptAsync(orderDetails ->
                    orderDetails.setOrder(order),executorService).join();

            OrderDetails orderDetails = orderDetailsFuture.join();
            orderDetailsRepository.save(orderDetails);
            orderDetailsFuture.thenAcceptAsync(orderDetailsList::add, executorService);
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

        if (orderDetails.getStatus().equals(DONE_STATUS) || orderDetails.getStatus().equals(CANCELLED_STATUS)) {
            throw new OrderDetailStatusInvalid(orderDetailId);
        }

        StatusStrategy statusStrategy = chooseStatusStrategy(status, orderDetails);

        statusStrategy.setStatus();

        orderDetailsRepository.save(orderDetails);

        return orderDetails;
    }

    private StatusStrategy chooseStatusStrategy(String status, OrderDetails orderDetails) {
        switch (status) {
            case "prepare" -> {
                if (!orderDetails.getStatus().equalsIgnoreCase("Menunggu konfirmasi")) {
                    throw new UpdateStatusInvalid(orderDetails.getStatus(), "Sedang Disiapkan");
                }
                return new PrepareStatus(orderDetails, this, menuItemRepository);
            }
            case "deliver" -> {
                if (!orderDetails.getStatus().equalsIgnoreCase("Sedang disiapkan")) {
                    throw new UpdateStatusInvalid(orderDetails.getStatus(), "Sedang Diantar");
                }
                return new DeliverStatus(orderDetails, this, menuItemRepository);
            }
            case "done" -> {
                if (!orderDetails.getStatus().equalsIgnoreCase("Sedang diantar")) {
                    throw new UpdateStatusInvalid(orderDetails.getStatus(), DONE_STATUS);
                }
                return new DoneStatus(orderDetails, this, menuItemRepository, restTemplate);
            }
            case "cancel" -> {
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
        Optional<List<Order>> orderBySession = orderRepository.findBySession(session);
        return orderBySession.orElseGet(ArrayList::new);
    }

    public boolean isOrderDoesNotExist(Integer orderId) {
        return orderRepository.findById(orderId).isEmpty();
    }

    public void addToBill(OrderDetails orderDetails) throws JSONException {
        String url = "http://34.142.223.187/api/v1/bills";

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
        String url = "http://34.143.176.116/warnet/info_sesi/session_detail/" + session;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            HttpEntity<String> entityResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String response = entityResponse.getBody();
            JSONObject jsonResponse = new JSONObject(response);
            JSONObject sessionInfo = jsonResponse.getJSONObject("session");
            JSONObject pcInfo = sessionInfo.getJSONObject("pc");

            CompletableFuture<Void> setIdPc = CompletableFuture.runAsync(() ->
                    orderDetails.setIdPC(pcInfo.getInt("id")), executorService);
            CompletableFuture<Void> setNoPc = CompletableFuture.runAsync(() ->
                    orderDetails.setNoPC(pcInfo.getInt("noPC")), executorService);
            CompletableFuture<Void> setNoRuangan = CompletableFuture.runAsync(() ->
                    orderDetails.setNoRuangan(pcInfo.getInt("noRuangan")), executorService);

            CompletableFuture<Void> setOrderPC = CompletableFuture.allOf(setIdPc, setNoPc, setNoRuangan);
            setOrderPC.join();

        } catch (HttpClientErrorException e) {
            throw new UUIDNotFoundException();
        }
    }
}
