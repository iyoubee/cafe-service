package id.ac.ui.cs.advprog.cafeservice.repository;

import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Integer> {
    @NonNull
    List<OrderDetails> findAll();

    List<OrderDetails> findAllByOrderId(Integer id);
}
