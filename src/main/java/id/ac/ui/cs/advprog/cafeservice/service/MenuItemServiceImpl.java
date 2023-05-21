package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {
    private final MenuItemRepository menuItemRepository;

    private final OrderDetailsRepository orderDetailsRepository;

    @Override
    public List<MenuItem> findAll(String query) {
        if (query != null && query.equals("available")) {
            return menuItemRepository.findByStockGreaterThan(0);
        }
        return menuItemRepository.findAll();
    }

    @Override
    public MenuItem findById(String id) {

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
    public MenuItem update(String id, MenuItemRequest request) {

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
        List<OrderDetails> orderDetailsList = orderDetailsRepository.getByMenuItem(id);
        for (OrderDetails orderDetails : orderDetailsList) {
            orderDetails.setMenuItem(null);
            if (!orderDetails.getStatus().equals("Selesai")) orderDetails.setStatus("Dibatalkan");
            orderDetailsRepository.save(orderDetails);
        }
        menuItemRepository.deleteById(id);
    }
}
