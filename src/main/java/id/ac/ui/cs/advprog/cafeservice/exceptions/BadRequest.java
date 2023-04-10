package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class BadRequest extends RuntimeException {
    public BadRequest() {
        super("400 Bad Request");
    }
}
