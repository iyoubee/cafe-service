package id.ac.ui.cs.advprog.cafeservice.pattern.statusStrategy;

import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.service.OrderServiceImpl;

public class PrepareStatus implements StatusStrategy{

    OrderDetails orderDetails;
    OrderServiceImpl orderService;
    MenuItemRepository menuItemRepository;

    public PrepareStatus(OrderDetails orderDetails, OrderServiceImpl orderService, MenuItemRepository menuItemRepository) {
        this.orderDetails = orderDetails;
        this.orderService = orderService;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void setStatus() {
        orderDetails.setStatus("Sedang Disiapkan");
    }
}
