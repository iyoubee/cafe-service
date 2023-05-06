package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cafe/order")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrder() {
        List<Order> response = orderService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{session}")
    public ResponseEntity<List<Order>> getOrderBySession(@PathVariable String session) {
        List<Order> response = orderService.findBySession(UUID.fromString(session));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Integer id) {
        Order response = orderService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest, @RequestParam(required = false) String from) {
        Order response = orderService.create(orderRequest, from);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<OrderDetails> changeStatus(@PathVariable Integer id, @RequestParam String status) {
        OrderDetails response = orderService.updateOrderDetailStatus(id, status);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Integer id) {
        orderService.delete(id);
        return ResponseEntity.ok(String.format("Deleted Order with id %d", id));
    }
}
