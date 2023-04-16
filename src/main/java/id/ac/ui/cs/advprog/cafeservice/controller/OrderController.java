package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.BadRequest;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/id/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Integer id) {
        Order response = orderService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest) {
        Order response = orderService.create(orderRequest);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Order> changeStatus(@PathVariable Integer id, @RequestBody OrderRequest request) {
        Order response = orderService.update(id, request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Integer id) {
        orderService.delete(id);
        return ResponseEntity.ok(String.format("Deleted Order with id %d", id));
    }
}
