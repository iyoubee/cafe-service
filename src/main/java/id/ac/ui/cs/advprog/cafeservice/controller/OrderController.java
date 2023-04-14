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
        if(orderRequest.getSession() == null || orderRequest.getOrderDetailsList() == null){
            throw new BadRequest();
        } else {
            Order response = orderService.create(orderRequest);
            return ResponseEntity.ok(response);
        }
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Order> changeStatus(@PathVariable Integer id, @RequestBody OrderRequest request) {
        if(request.getSession() == null || request.getOrderDetailsList() == null){
            throw new BadRequest();
        }
        else {
            Order response = orderService.update(id, request);
            return ResponseEntity.ok(response);
        }
    }
}
