package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface MenuItemService {
    List<MenuItem> findAll();
    MenuItem findById(Integer id);
    MenuItem create(MenuItemRequest request);
    MenuItem update(Integer id, MenuItemRequest request);
    void delete(Integer id);
}
