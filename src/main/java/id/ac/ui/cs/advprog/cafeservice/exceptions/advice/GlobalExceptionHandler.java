package id.ac.ui.cs.advprog.cafeservice.exceptions.advice;

import id.ac.ui.cs.advprog.cafeservice.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {
            MenuItemDoesNotExistException.class,
            OrderDoesNotExistException.class,
            OrderDetailDoesNotExistException.class
    })
    public ResponseEntity<Object> itemNotAvailable(Exception exception) {
        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ErrorTemplate baseException = new ErrorTemplate(
                exception.getMessage(),
                notFound,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(baseException, notFound);
    }
    @ExceptionHandler(value = {
            BadRequest.class,
            MenuItemValueEmpty.class,
            MenuItemValueInvalid.class,
            OrderDetailsQtyInvalid.class,
            OrderDetailsValueEmpty.class,
            OrderDetailStatusInvalid.class,
            InvalidJSONException.class,
            UUIDNotFoundException.class,
            MenuItemOutOfStockException.class,
            UpdateStatusInvalid.class
    })
    public ResponseEntity<Object> requestIsInvalid(Exception exception) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ErrorTemplate baseException = new ErrorTemplate(
                exception.getMessage(),
                badRequest,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(baseException, badRequest);
    }
}