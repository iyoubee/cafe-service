package id.ac.ui.cs.advprog.cafeservice.pattern.statusStrategy;

import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDetailStatusInvalid;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.service.OrderServiceImpl;

public class CancelStatus implements StatusStrategy {

    OrderDetails orderDetails;
    OrderServiceImpl orderService;
    MenuItemRepository menuItemRepository;

    public CancelStatus(OrderDetails orderDetails, OrderServiceImpl orderService, MenuItemRepository menuItemRepository) {
        this.orderDetails = orderDetails;
        this.orderService = orderService;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void setStatus() {
        if (orderDetails.getStatus().equals("Menunggu Konfirmasi") && orderDetails.getTotalPrice() != 0) {
            MenuItem menuItem = orderDetails.getMenuItem();
            menuItem.setStock(menuItem.getStock() + orderDetails.getQuantity());
            menuItemRepository.save(menuItem);
            orderDetails.setStatus("Dibatalkan");
        } else {
            throw new OrderDetailStatusInvalid(orderDetails.getId());
        }
    }
}
