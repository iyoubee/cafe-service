package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import org.springframework.boot.configurationprocessor.json.JSONException;

@Service
public interface OrderService {
    List<Order> findAll();
    List<Order> findBySession(UUID session);
    Order findById(Integer id);
    Order create(OrderRequest request);
    Order update(Integer id, OrderRequest request);
    void delete(Integer id);
    void addToBill(OrderDetails orderDetails) throws JSONException;
}
