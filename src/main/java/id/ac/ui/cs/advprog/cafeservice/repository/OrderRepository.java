package id.ac.ui.cs.advprog.cafeservice.repository;

import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
