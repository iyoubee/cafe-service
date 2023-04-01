package id.ac.ui.cs.advprog.cafeservice.repository;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    @NonNull
    List<MenuItem> findAll();
    @NonNull
    Optional<MenuItem> findById(@NonNull Integer id);
    void deleteById(@NonNull Integer id);
}
