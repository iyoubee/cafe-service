package id.ac.ui.cs.advprog.cafeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private UUID session;
    private List<OrderDetails> orderDetailsList;
}