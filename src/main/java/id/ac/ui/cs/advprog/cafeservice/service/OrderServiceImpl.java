package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final MenuItemRepository menuItemRepository;

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
                    .status(orderDetailsData.getStatus())
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
                OrderDetails updated = orderDetailsRepository.save(
                        OrderDetails.builder()
                                .order(order)
                                .quantity(details.getQuantity())
                                .totalPrice(menu.get().getPrice() * details.getQuantity())
                                .menuItem(menu.get())
                                .status(details.getStatus())
                                .build());
                orderDetailsList.add(updated);
            } else {
                listOfOrderDetails.remove(orderDetails.get());
                OrderDetails updated = orderDetailsRepository.save(
                        OrderDetails.builder()
                                .id(orderDetails.get().getId())
                                .order(order)
                                .quantity(details.getQuantity())
                                .totalPrice(menu.get().getPrice() * details.getQuantity())
                                .menuItem(menu.get())
                                .status(details.getStatus())
                                .build());
                orderDetailsList.add(updated);
            }
        });
        orderDetailsRepository.deleteAll(listOfOrderDetails);
        order.setOrderDetailsList(orderDetailsList);
        return order;
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
        return orderBySession.get();
    }


    public boolean isOrderDoesNotExist(Integer id) {
        return orderRepository.findById(id).isEmpty();
    }
}
