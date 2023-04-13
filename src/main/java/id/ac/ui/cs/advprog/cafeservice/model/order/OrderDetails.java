package id.ac.ui.cs.advprog.cafeservice.model.order;

import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OrderDetails {
    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    private Order order;
    @ManyToOne
    private MenuItem menuItem;
    private Integer quantity;
    private Integer totalPrice;
}
