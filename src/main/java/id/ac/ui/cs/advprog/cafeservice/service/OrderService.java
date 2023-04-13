package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    List<Order> findAll();
    Order update(String id, OrderRequest request);
    Order findById(String id);
}
