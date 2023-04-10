package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class MenuItemValueEmpty extends RuntimeException {
    public MenuItemValueEmpty(String emptyValueName) {
        super("The menu item " + emptyValueName + " request can't be empty");
    }
}
