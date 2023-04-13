package id.ac.ui.cs.advprog.cafeservice.repository;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<MenuItem, String> {
    @NonNull
    List<Order> findAll();
    @NonNull
    Optional<Order> findById(@NonNull String id);
    void deleteById(@NonNull String id);
}
