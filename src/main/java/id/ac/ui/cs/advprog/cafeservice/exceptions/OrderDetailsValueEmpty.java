package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class OrderDetailsValueEmpty extends RuntimeException{

    public OrderDetailsValueEmpty(String name) {
        super(String.format("%s can't be empty", name));
    }
}
