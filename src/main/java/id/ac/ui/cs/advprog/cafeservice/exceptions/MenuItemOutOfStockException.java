package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class MenuItemOutOfStockException extends RuntimeException{
    public MenuItemOutOfStockException(String menuItemName) {
        super(String.format("The menu item %s is out of stock", menuItemName));
    }
}
