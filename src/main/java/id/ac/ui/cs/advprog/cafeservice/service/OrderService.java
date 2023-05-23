package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public interface OrderService {
    List<Order> findAll();
    List<Order> findBySession(UUID session);
    List<Order> findByPagination(int page);
    Order findById(Integer id);
    Order create(OrderRequest request, String from);
    OrderDetails updateOrderDetailStatus(Integer orderDetailId, String status);
    void delete(Integer id);
    int getCount();
}
