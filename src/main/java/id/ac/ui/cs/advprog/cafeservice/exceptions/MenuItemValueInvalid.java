package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class MenuItemValueInvalid extends RuntimeException {
    public MenuItemValueInvalid(String invalidValueCategoryName) {
        super("The value of " + invalidValueCategoryName + " is invalid");
    }
}
