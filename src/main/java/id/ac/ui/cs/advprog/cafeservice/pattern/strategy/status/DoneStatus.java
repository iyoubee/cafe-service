package id.ac.ui.cs.advprog.cafeservice.pattern.strategy.status;

import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.service.OrderServiceImpl;
import org.springframework.web.client.RestTemplate;

public class DoneStatus implements StatusStrategy {
    OrderDetails orderDetails;
    OrderServiceImpl orderService;
    MenuItemRepository menuItemRepository;
    private RestTemplate restTemplate;

    public DoneStatus(OrderDetails orderDetails, OrderServiceImpl orderService, MenuItemRepository menuItemRepository, RestTemplate restTemplate) {
        this.orderDetails = orderDetails;
        this.orderService = orderService;
        this.menuItemRepository = menuItemRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public void setStatus() {
        if (orderDetails.getTotalPrice() != 0) {
            orderService.addToBill(orderDetails);
        }
        orderDetails.setStatus("Selesai");
    }
}
