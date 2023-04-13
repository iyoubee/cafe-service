package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public Order update(String orderId, OrderRequest request) {
        Optional<Order> getOrder = orderRepository.findById(orderId);
        if (getOrder.isEmpty()) throw new OrderDoesNotExistException(orderId);
        Order getDetail = getOrder.get();
        getDetail.setSession(request.getSession());
        getDetail.setOrderDetailsList(request.getOrderDetailsList());
        return orderRepository.save(getDetail);
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(String id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) throw new MenuItemDoesNotExistException(id);
        return order.get();
    }

}
