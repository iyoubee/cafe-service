package id.ac.ui.cs.advprog.cafeservice.pattern.strategy.create;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateFromWarnet implements CreateStrategy{
    private final MenuItem menuItem;
    private final OrderDetailsData orderDetailsData;
    @Override
    public OrderDetails create() {
        return OrderDetails.builder()
                .menuItem(menuItem)
                .quantity(orderDetailsData.getQuantity())
                .status("Menunggu Konfirmasi")
                .totalPrice(0)
                .build();
    }
}
