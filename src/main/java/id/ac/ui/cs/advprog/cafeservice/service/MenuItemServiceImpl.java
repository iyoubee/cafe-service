package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
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

        Optional<MenuItem> menuItem = menuItemRepository.findById(id);
        if (menuItem.isEmpty()) throw new MenuItemDoesNotExistException(id);
        return menuItem.get();
    }

    @Override
    public MenuItem create(MenuItemRequest request) {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setStock(request.getStock());
        return menuItemRepository.save(menuItem);
    }

    @Override
    public MenuItem update(Integer id, MenuItemRequest request) {

        Optional<MenuItem> menuItem = menuItemRepository.findById(id);
        if (menuItem.isEmpty()) throw new MenuItemDoesNotExistException(id);
        MenuItem item = menuItem.get();
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setStock(request.getStock());
        return menuItemRepository.save(item);
    }

    @Override
    public void delete(String id) {
        Optional<MenuItem> menuItem = menuItemRepository.findById(id);
        if (menuItem.isEmpty()) throw new MenuItemDoesNotExistException(id);
        menuItemRepository.deleteById(id);
    }


}
