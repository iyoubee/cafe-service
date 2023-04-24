package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface MenuItemService {
    List<MenuItem> findAll(String query);
    MenuItem findById(String id);
    MenuItem create(MenuItemRequest request);
    MenuItem update(String id, MenuItemRequest request);
    void delete(String id);
}
