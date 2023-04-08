package id.ac.ui.cs.advprog.cafeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemRequest {
    private String name;
    private Integer price;
    private Integer stock;
}
