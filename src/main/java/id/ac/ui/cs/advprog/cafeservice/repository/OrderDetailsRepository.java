package id.ac.ui.cs.advprog.cafeservice.repository;

import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Integer> {
    @NonNull
    List<OrderDetails> findAll();
    @NonNull
    Optional<OrderDetails> findById(@NonNull Integer id);

    List<OrderDetails> findAllByOrderId(Integer id);

    Optional<OrderDetails> findByOrderIdAndMenuItemId(Integer orderId, String menuItemId);
}
