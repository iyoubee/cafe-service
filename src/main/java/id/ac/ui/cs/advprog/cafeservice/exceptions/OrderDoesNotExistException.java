package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class OrderDoesNotExistException extends RuntimeException {
    public OrderDoesNotExistException(String id) {
        super("Order with id " + id + " does not exist");
    }
}
