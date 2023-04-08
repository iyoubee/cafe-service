package id.ac.ui.cs.advprog.cafeservice.model.menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MenuItem {
    @Id
    @GeneratedValue
    private String id;
    private String name;
    private Integer price;
    private Integer stock;

}
