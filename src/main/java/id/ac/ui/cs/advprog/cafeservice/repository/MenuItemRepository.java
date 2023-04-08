package id.ac.ui.cs.advprog.cafeservice.repository;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {
    @NonNull
    List<MenuItem> findAll();
    @NonNull
    Optional<MenuItem> findById(@NonNull String id);
    void deleteById(@NonNull String id);
}
