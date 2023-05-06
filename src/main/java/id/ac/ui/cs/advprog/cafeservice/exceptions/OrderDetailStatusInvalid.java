package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class OrderDetailStatusInvalid extends RuntimeException {
    public OrderDetailStatusInvalid(Integer id) {
        super("Order Detail status with id " + id + " invalid");
    }
}
