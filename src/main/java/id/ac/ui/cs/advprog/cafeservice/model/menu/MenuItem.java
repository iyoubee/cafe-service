package id.ac.ui.cs.advprog.cafeservice.model.menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MenuItem {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String name;
    private Integer price;
    private Integer stock;

}
