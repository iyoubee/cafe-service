package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class OrderDetailDoesNotExistException extends RuntimeException {
    public OrderDetailDoesNotExistException(Integer id) {
        super("Order Detail with id " + id + " does not exist");
    }
}
