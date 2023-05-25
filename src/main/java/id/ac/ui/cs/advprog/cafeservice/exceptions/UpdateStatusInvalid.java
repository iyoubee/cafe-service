package id.ac.ui.cs.advprog.cafeservice.exceptions;

public class UpdateStatusInvalid extends RuntimeException{

    public UpdateStatusInvalid(String oldStatus, String newStatus) {
        super(String.format("Cannot change status from %s to %s", oldStatus, newStatus));
    }
}
