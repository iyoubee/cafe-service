package id.ac.ui.cs.advprog.cafeservice.validator;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.BadRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDetailsQtyInvalid;
import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDetailsValueEmpty;

public class OrderValidator {

    public void validateRequest(OrderRequest request) {
        if (request.getSession() == null || request.getOrderDetailsData() == null) {
            throw new BadRequest();
        }

        request.getOrderDetailsData().forEach(data -> {
            if (data.getMenuItemId() == null || data.getQuantity() == null) {
                throw new BadRequest();
            }

            if (data.getMenuItemId().isEmpty()) {
                throw new OrderDetailsValueEmpty("Menu Item ID");
            }

            if (data.getQuantity() <= 0) {
                throw new OrderDetailsQtyInvalid(data.getQuantity());
            }
        });
    }
}
