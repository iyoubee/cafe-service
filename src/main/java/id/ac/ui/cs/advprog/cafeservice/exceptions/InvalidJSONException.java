package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class InvalidJSONException extends RuntimeException{
    public InvalidJSONException() {
        super("Invalid request body");
    }
}
