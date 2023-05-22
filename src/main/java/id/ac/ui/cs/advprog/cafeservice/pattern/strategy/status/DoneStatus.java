package id.ac.ui.cs.advprog.cafeservice.pattern.strategy.status;

import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.service.OrderServiceImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoneStatus implements StatusStrategy {
    OrderDetails orderDetails;
    OrderServiceImpl orderService;
    MenuItemRepository menuItemRepository;

    public DoneStatus(OrderDetails orderDetails, OrderServiceImpl orderService, MenuItemRepository menuItemRepository) {
        this.orderDetails = orderDetails;
        this.orderService = orderService;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void setStatus() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        if (orderDetails.getTotalPrice() != 0) {
            CompletableFuture.runAsync(() -> orderService.addToBill(orderDetails), executorService);
        }
        CompletableFuture.runAsync(() -> orderDetails.setStatus("Selesai"), executorService);
    }
}
