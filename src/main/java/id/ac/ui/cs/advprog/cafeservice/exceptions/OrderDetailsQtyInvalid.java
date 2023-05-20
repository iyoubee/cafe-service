package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class OrderDetailsQtyInvalid extends RuntimeException{

    public OrderDetailsQtyInvalid(Integer amount) {
        super(String.format("Order quantity can't be %d", amount));
    }
}
