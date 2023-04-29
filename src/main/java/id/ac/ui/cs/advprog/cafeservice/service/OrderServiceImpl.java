package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.InvalidJSONException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemOutOfStockException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.UUIDNotFoundException;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderRepository;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final MenuItemService menuItemService;
    private final MenuItemRepository menuItemRepository;

    private RestTemplate restTemplate;
    private static final String CANCELLED_STATUS = "Dibatalkan";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
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
            OrderDetails orderDetails = OrderDetails.builder()
                    .menuItem(menuItem.get())
                    .quantity(orderDetailsData.getQuantity())
                    .status("Menunggu konfirmasi")
                    .totalPrice(menuItem.get().getPrice() * orderDetailsData.getQuantity())
                    .build();
            MenuItemRequest menuItemRequest = MenuItemRequest.builder()
                    .name(menuItem.get().getName())
                    .price(menuItem.get().getPrice())
                    .stock(menuItem.get().getStock() - orderDetailsData.getQuantity())
                    .build();
            if (from != null && from.equalsIgnoreCase("warnet")) {
                orderDetails.setTotalPrice(0);
            }
            menuItemService.update(menuItem.get().getId(), menuItemRequest);
            orderDetails.setOrder(order);
            orderDetailsRepository.save(orderDetails);
            orderDetailsList.add(orderDetails);
        }
        order.setOrderDetailsList(orderDetailsList);
        orderRepository.save(order);
        return order;
    }

    @Override
    public Order update(Integer orderId, OrderRequest request) {
        if (isOrderDoesNotExist(orderId)) {
            throw new OrderDoesNotExistException(orderId);
        }

        var order = Order.builder().id(orderId).session(request.getSession()).build();
        var listOfOrderDetails = orderDetailsRepository.findAllByOrderId(orderId);
        var orderDetailsList = new ArrayList<OrderDetails>();

        for (OrderDetailsData details : request.getOrderDetailsData()) {
            var menu = menuItemRepository.findById(details.getMenuItemId());
            if (menu.isEmpty()) {
                throw new MenuItemDoesNotExistException(details.getMenuItemId());
            }

            var orderDetails = orderDetailsRepository.findByOrderIdAndMenuItemId(orderId, menu.get().getId());
            if (details.getQuantity() > menu.get().getStock() + (orderDetails.isPresent() ? orderDetails.get().getQuantity() : 0)) {
                throw new MenuItemOutOfStockException(menu.get().getName());
            }

            if (orderDetails.isEmpty()) {
                orderDetailsList.add(createAndUpdateOrderDetails(order, details, menu.get()));

            } else {
                listOfOrderDetails.remove(orderDetails.get());
                orderDetailsList.add(updateOrderDetails(order, orderDetails.get(), details, menu.get()));
            }
        }
        orderDetailsRepository.deleteAll(listOfOrderDetails);
        order.setOrderDetailsList(orderDetailsList);
        return order;
    }

    private OrderDetails createAndUpdateOrderDetails(Order order, OrderDetailsData details, MenuItem menuItem) {
        OrderDetails updated = orderDetailsRepository.save(
                OrderDetails.builder()
                        .order(order)
                        .quantity(details.getQuantity())
                        .menuItem(menuItem)
                        .status(details.getStatus())
                        .totalPrice(menuItem.getPrice() * details.getQuantity())
                        .build());

        if (updated != null && updated.getStatus().equalsIgnoreCase("Selesai")) {
            try {
                addToBill(updated);
                updated.setStatus("Masuk bill");
            } catch (JSONException e) {
                throw new InvalidJSONException();
            }
        }

        MenuItemRequest menuItemRequest = MenuItemRequest.builder()
                .name(menuItem.getName())
                .price(menuItem.getPrice())
                .stock(menuItem.getStock() - details.getQuantity())
                .build();
        menuItemService.update(menuItem.getId(), menuItemRequest);

        return updated;
    }

    private OrderDetails updateOrderDetails(Order order, OrderDetails existingOrderDetails, OrderDetailsData details, MenuItem menuItem) {

        if (existingOrderDetails.getStatus().equalsIgnoreCase(CANCELLED_STATUS)){
            return existingOrderDetails;
        }

        OrderDetails updated = orderDetailsRepository.save(
                OrderDetails.builder()
                        .id(existingOrderDetails.getId())
                        .order(order)
                        .quantity(details.getQuantity())
                        .menuItem(menuItem)
                        .status(details.getStatus())
                        .totalPrice(menuItem.getPrice() * details.getQuantity())
                        .build());

        if (updated != null && updated.getStatus().equalsIgnoreCase("Selesai")) {
            try {
                addToBill(updated);
                updated.setStatus("Masuk bill");
            } catch (JSONException e) {
                throw new InvalidJSONException();
            }
        }

        if (!existingOrderDetails.getStatus().equalsIgnoreCase(CANCELLED_STATUS)){
            MenuItemRequest menuItemRequest;
            if (details.getStatus().equalsIgnoreCase(CANCELLED_STATUS)){
                menuItemRequest = MenuItemRequest.builder()
                        .name(menuItem.getName())
                        .price(menuItem.getPrice())
                        .stock(menuItem.getStock() + existingOrderDetails.getQuantity())
                        .build();
            } else {
                menuItemRequest = MenuItemRequest.builder()
                        .name(menuItem.getName())
                        .price(menuItem.getPrice())
                        .stock(menuItem.getStock() + existingOrderDetails.getQuantity() - details.getQuantity())
                        .build();
            }
            menuItemService.update(menuItem.getId(), menuItemRequest);
        }

        return updated;
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
    public List<Order> findBySession(UUID session) {
        Optional<List<Order>> orderBySession = orderRepository.findBySession(session);
        return orderBySession.orElseGet(ArrayList::new);
    }

    public boolean isOrderDoesNotExist(Integer id) {
        return orderRepository.findById(id).isEmpty();
    }

    public void addToBill(OrderDetails orderDetails) throws JSONException {
        int id = getInvoiceId(orderDetails.getOrder().getSession());
        String url = "http://34.142.223.187/api/v1/bills";

        MenuItem orderedMenu = orderDetails.getMenuItem();
        JSONObject requestBody = new JSONObject();

        requestBody.put("name", orderedMenu.getName());
        requestBody.put("price", orderedMenu.getPrice());
        requestBody.put("quantity", orderDetails.getQuantity());
        requestBody.put("subTotal", (long) orderedMenu.getPrice() * orderDetails.getQuantity());
        requestBody.put("invoiceId", id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        restTemplate.postForObject(url, entity, String.class);
    }

    public int getInvoiceId(UUID session)  {
        String url = "http://34.142.223.187/api/v1/invoices/" + session;

        String response = restTemplate.getForObject(url, String.class);
        JSONObject obj = new JSONObject(response);
        JSONObject content = (JSONObject) obj.get("content");

        if (content == null) {
            throw new UUIDNotFoundException();
        } else {
            return (Integer) content.get("id");
        }

    }

}
