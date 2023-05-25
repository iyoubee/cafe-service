package id.ac.ui.cs.advprog.cafeservice.repository;

import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @NonNull
    List<Order> findAll();
    @NonNull
    Optional<Order> findById(@NonNull Integer id);
    @NonNull
    Optional<List<Order>> findBySession(@NonNull UUID session);
    void deleteById(@NonNull Integer id);
    @Query(value = "SELECT COUNT(*) FROM order_details;", nativeQuery = true)
    int getCount();
    @Query(value = "SELECT * FROM order_menu_item ORDER BY id DESC OFFSET ?1 ROWS FETCH NEXT ?2 ROWS ONLY;", nativeQuery = true)
    List<Order> getByPage(int offset, int next);
}
