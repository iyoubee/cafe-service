package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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
        if (order.isEmpty()) throw new OrderDoesNotExistException(id);
        return order.get();
    }
    @Override
    public Order create(OrderRequest request){
        var order = Order.builder().session(request.getSession()).build();
        request.getOrderDetailsData().forEach(orderDetails -> {
            var menu = menuItemRepository.findById(orderDetails.getMenuItemId());
            if (menu.isEmpty()){
                throw new MenuItemDoesNotExistException(orderDetails.getMenuItemId());
            }
            orderDetailsRepository.save(
                    OrderDetails.builder()
                            .order(order)
                            .menuItem(menu.get())
                            .quantity(orderDetails.getQuantity())
                            .totalPrice(menu.get().getPrice() * orderDetails.getQuantity())
                            .status(orderDetails.getStatus())
                            .build()
            );
        });
        orderRepository.save(order);
        return order;
    }

    @Override
    public Order update(Integer orderId, OrderRequest request) {
        if (isOrderDoesNotExist(orderId)){
            throw new OrderDoesNotExistException(orderId);
        }
        var order = Order.builder().session(request.getSession()).build();
        var listOfOrderDetails = orderDetailsRepository.findAllByOrderId(orderId);
        request.getOrderDetailsData().forEach(details -> {
            var menu = menuItemRepository.findById(details.getMenuItemId());
            if (menu.isEmpty()){
                throw new MenuItemDoesNotExistException(details.getMenuItemId());
            }
            var orderDetails = orderDetailsRepository.findByOrderIdAndMenuItemId(orderId, menu.get().getId());
            if (orderDetails.isEmpty()) {
                orderDetailsRepository.save(
                        OrderDetails.builder()
                                .order(order)
                                .quantity(details.getQuantity())
                                .totalPrice(menu.get().getPrice() * details.getQuantity())
                                .menuItem(menu.get())
                                .status(details.getStatus())
                                .build()
                );
            } else {
                listOfOrderDetails.remove(orderDetails.get());
                orderDetailsRepository.save(
                        OrderDetails.builder()
                                .id(orderDetails.get().getId())
                                .order(order)
                                .quantity(details.getQuantity())
                                .totalPrice(menu.get().getPrice() * details.getQuantity())
                                .menuItem(menu.get())
                                .status(details.getStatus())
                                .build()
                );
            }
        });
        orderDetailsRepository.deleteAll(listOfOrderDetails);
        return order;
    }

    @Override
    public void delete(Integer id){
        if (isOrderDoesNotExist(id)){
            throw new OrderDoesNotExistException(id);
        } else {
            orderRepository.deleteById(id);
        }
    }

    public boolean isOrderDoesNotExist(Integer id) {
        return orderRepository.findById(id).isEmpty();
    }
}
