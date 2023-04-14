package id.ac.ui.cs.advprog.cafeservice.model.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_menu_item")
public class Order {
    @Id
    @GeneratedValue
    private Integer id;
    private Integer pc;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetails> orderDetailsList;
}
