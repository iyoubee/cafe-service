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
        var order = Order.builder().pc(request.getPc()).build();
        request.getOrderDetailsList().forEach(orderDetails -> {
            var menu = menuItemRepository.findById(orderDetails.getMenuItem().getId());
            if (menu.isEmpty()){
                throw new MenuItemDoesNotExistException(orderDetails.getMenuItem().getId());
            }
            orderDetailsRepository.save(
                    OrderDetails.builder()
                            .order(order)
                            .menuItem(menu.get())
                            .quantity(orderDetails.getQuantity())
                            .totalPrice(orderDetails.getTotalPrice())
                            .status(orderDetails.getStatus())
                            .build()
            );
        });
        orderRepository.save(order);
        return order;
    }

    @Override
    public Order update(Integer orderId, OrderRequest request) {
        Optional<Order> getOrder = orderRepository.findById(orderId);
        if (getOrder.isEmpty()) throw new OrderDoesNotExistException(orderId);
        Order getDetail = getOrder.get();
        getDetail.setPc(request.getPc());
        getDetail.setOrderDetailsList(request.getOrderDetailsList());
        return orderRepository.save(getDetail);
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
