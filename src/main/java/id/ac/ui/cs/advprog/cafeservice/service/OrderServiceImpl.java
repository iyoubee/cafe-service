package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.InvalidJSONException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDoesNotExistException;
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

    @Override
    public Order findById(Integer id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty())
            throw new OrderDoesNotExistException(id);
        return order.get();
    }

    @Override
    public Order create(OrderRequest request) {
        var order = Order.builder().session(request.getSession()).build();
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        request.getOrderDetailsData().forEach(orderDetailsData -> {
            var menuItem = menuItemRepository.findById(orderDetailsData.getMenuItemId());
            if (menuItem.isEmpty()) {
                throw new MenuItemDoesNotExistException(orderDetailsData.getMenuItemId());
            }
            OrderDetails orderDetails = OrderDetails.builder()
                    .menuItem(menuItem.get())
                    .quantity(orderDetailsData.getQuantity())
                    .totalPrice(menuItem.get().getPrice() * orderDetailsData.getQuantity())
                    .status("Menunggu konfirmasi")
                    .build();
            orderDetails.setOrder(order);
            orderDetailsRepository.save(orderDetails);
            orderDetailsList.add(orderDetails);
        });
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

        request.getOrderDetailsData().forEach(details -> {
            var menu = menuItemRepository.findById(details.getMenuItemId());
            if (menu.isEmpty()) {
                throw new MenuItemDoesNotExistException(details.getMenuItemId());
            }

            var orderDetails = orderDetailsRepository.findByOrderIdAndMenuItemId(orderId, menu.get().getId());
            if (orderDetails.isEmpty()) {
                orderDetailsList.add(createAndUpdateOrderDetails(order, details, menu.get()));
            } else {
                listOfOrderDetails.remove(orderDetails.get());
                orderDetailsList.add(updateOrderDetails(order, orderDetails.get(), details, menu.get()));
            }
        });

        orderDetailsRepository.deleteAll(listOfOrderDetails);
        order.setOrderDetailsList(orderDetailsList);
        return order;
    }

    private OrderDetails createAndUpdateOrderDetails(Order order, OrderDetailsData details, MenuItem menuItem) {
        OrderDetails updated = orderDetailsRepository.save(
                OrderDetails.builder()
                        .order(order)
                        .quantity(details.getQuantity())
                        .totalPrice(menuItem.getPrice() * details.getQuantity())
                        .menuItem(menuItem)
                        .status(details.getStatus())
                        .build());

        if (updated.getStatus().equalsIgnoreCase("Selesai")) {
            try {
                addToBill(updated);
                updated.setStatus("Masuk bill");
            } catch (JSONException e) {
                throw new InvalidJSONException();
            }
        }

        return updated;
    }

    private OrderDetails updateOrderDetails(Order order, OrderDetails existingOrderDetails, OrderDetailsData details,
            MenuItem menuItem) {
        OrderDetails updated = orderDetailsRepository.save(
                OrderDetails.builder()
                        .id(existingOrderDetails.getId())
                        .order(order)
                        .quantity(details.getQuantity())
                        .totalPrice(menuItem.getPrice() * details.getQuantity())
                        .menuItem(menuItem)
                        .status(details.getStatus())
                        .build());

        if (updated.getStatus().equalsIgnoreCase("Selesai")) {
            try {
                addToBill(updated);
                updated.setStatus("Masuk bill");
            } catch (JSONException e) {
                throw new InvalidJSONException();
            }
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
        if (orderBySession.isPresent()) {
            return orderBySession.get();
        } else {
            return new ArrayList<>();
        }
    }

    public boolean isOrderDoesNotExist(Integer id) {
        return orderRepository.findById(id).isEmpty();
    }

    public void addToBill(OrderDetails orderDetails) throws JSONException {
        int id = 2;
        String url = "http://34.142.223.187/api/v1/invoices/" + id + "/bills";

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

}
