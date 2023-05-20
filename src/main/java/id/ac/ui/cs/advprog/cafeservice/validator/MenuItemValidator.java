package id.ac.ui.cs.advprog.cafeservice.validator;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.BadRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemValueEmpty;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemValueInvalid;

public class MenuItemValidator {

    public void validateRequest(MenuItemRequest request) {
        if(request.getName() == null || request.getPrice() == null || request.getStock() == null){
            throw new BadRequest();
        }

        else if(request.getName().isEmpty()){
            throw new MenuItemValueEmpty("Name");
        }

        else if(request.getPrice() < 0){
            throw new MenuItemValueInvalid("Price");
        }

        else if(request.getStock() < 0){
            throw new MenuItemValueInvalid("Stock");
        }
    }
}
