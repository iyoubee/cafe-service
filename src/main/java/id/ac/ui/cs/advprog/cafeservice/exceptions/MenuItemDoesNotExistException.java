package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class MenuItemDoesNotExistException extends RuntimeException {
    public MenuItemDoesNotExistException(Integer id) {
        super("Menu item with id " + id + " does not exist");
    }
}
