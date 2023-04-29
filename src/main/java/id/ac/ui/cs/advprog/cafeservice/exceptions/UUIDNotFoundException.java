package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class UUIDNotFoundException extends RuntimeException{

    public UUIDNotFoundException() {
        super("The UUID is not found");
    }
}
