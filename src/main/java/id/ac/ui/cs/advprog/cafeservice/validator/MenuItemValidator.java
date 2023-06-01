package id.ac.ui.cs.advprog.cafeservice.validator;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.BadRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemValueEmpty;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemValueInvalid;

public class MenuItemValidator {

    public void validateRequest(MenuItemRequest request) {
        if (request.getName() == null || request.getPrice() == null || request.getStock() == null) {
            throw new BadRequest();
        }

        if (request.getName().matches("[0-9]*$")) {
            throw new MenuItemValueInvalid("Name");
        }

        if (request.getName().trim().isEmpty()) {
            throw new MenuItemValueEmpty("Name");
        }

        if (request.getName().length() > 30) {
            throw new MenuItemValueInvalid("Name");
        }

        if (request.getPrice() < 0) {
            throw new MenuItemValueInvalid("Price");
        }

        if (request.getPrice() >= 1000000) {
            throw new MenuItemValueInvalid("Price");
        }

        if (request.getStock() < 0) {
            throw new MenuItemValueInvalid("Stock");
        }
    }
}
