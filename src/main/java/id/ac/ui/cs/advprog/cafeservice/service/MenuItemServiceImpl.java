package id.ac.ui.cs.advprog.cafeservice.service;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.model.order.Status;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        setMenuItem(menuItem, request);
        return menuItemRepository.save(menuItem);
    }

    @Override
    public MenuItem update(String id, MenuItemRequest request) {

        Optional<MenuItem> item = menuItemRepository.findById(id);
        if (item.isEmpty()) throw new MenuItemDoesNotExistException(id);
        MenuItem menuItem = item.get();
        setMenuItem(menuItem, request);
        return menuItemRepository.save(menuItem);
    }

    private void setMenuItem(MenuItem menuItem, MenuItemRequest request) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CompletableFuture.runAsync(() -> menuItem.setName(request.getName()), executorService);
        CompletableFuture.runAsync(() -> menuItem.setPrice(request.getPrice()), executorService);
        CompletableFuture.runAsync(() -> menuItem.setStock(request.getStock()), executorService);
    }

    @Override
    public void delete(String id) {
        Optional<MenuItem> menuItem = menuItemRepository.findById(id);
        if (menuItem.isEmpty()) throw new MenuItemDoesNotExistException(id);
        List<OrderDetails> orderDetailsList = orderDetailsRepository.getByMenuItem(id);
        for (OrderDetails orderDetails : orderDetailsList) {
            orderDetails.setMenuItem(null);
            if (!orderDetails.getStatus().equals(Status.DONE.getValue())) orderDetails.setStatus(Status.CANCEL.getValue());
            orderDetailsRepository.save(orderDetails);
        }
        menuItemRepository.deleteById(id);
    }
}
