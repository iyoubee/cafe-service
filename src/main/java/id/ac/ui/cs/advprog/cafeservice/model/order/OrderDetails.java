package id.ac.ui.cs.advprog.cafeservice.model.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @JsonBackReference
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "order_id")
    private Order order;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;
    private Integer quantity;
    private String status;
    private Integer totalPrice;
    private Integer idPC;
    private Integer noPC;
    private Integer noRuangan;
}
