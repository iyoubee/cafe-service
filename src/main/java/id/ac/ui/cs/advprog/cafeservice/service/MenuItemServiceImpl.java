package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {
    private final MenuItemRepository menuItemRepository;

    @Override
    public List<MenuItem> findAll() {
        return menuItemRepository.findAll();
    }

    @Override
    public MenuItem findById(Integer id) {
        MenuItem menuItem = menuItemRepository.findById(id).get();
        return menuItem;
    }

    @Override
    public MenuItem create(MenuItemRequest request) {
        return null;
    }

    @Override
    public MenuItem update(Integer id, MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(id).get();
        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setStock(request.getStock());
        return menuItem;
    }

    @Override
    public void delete(Integer id) {
        menuItemRepository.deleteById(id);
    }


}
