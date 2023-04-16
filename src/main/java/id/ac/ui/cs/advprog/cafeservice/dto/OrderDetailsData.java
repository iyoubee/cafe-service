package id.ac.ui.cs.advprog.cafeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsData {
    private String menuItemId;
    private Integer quantity;
    private String status;
}
