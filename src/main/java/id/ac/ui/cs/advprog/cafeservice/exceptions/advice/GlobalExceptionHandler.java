package id.ac.ui.cs.advprog.cafeservice.exceptions.advice;

import id.ac.ui.cs.advprog.cafeservice.exceptions.ErrorTemplate;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {MenuItemDoesNotExistException.class})
    public ResponseEntity<Object> menuItemNotAvailable(Exception exception) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ErrorTemplate baseException = new ErrorTemplate(
                exception.getMessage(),
                badRequest,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(baseException, badRequest);
    }
}