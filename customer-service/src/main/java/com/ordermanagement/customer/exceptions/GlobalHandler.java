package com.ordermanagement.customer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandler {

    private static final String SOURCE_APP = "AUTH-IRIS";

    private Map<String, Object> buildError(
            String label,
            HttpStatus status,
            String message,
            String level,
            String severity) {

        return Map.of(
                "label", label,
                "code", String.valueOf(status.value()),
                "level", level,
                "severity", severity,
                "message", message,
                "httpStatus", status.toString(),
                "sourceApplication", "CUSTOMER_SERVIVE"
        );
    }
    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex) {

        HttpStatus status = ex.getStatus();

        return new ResponseEntity<>(
                buildError(
                        ex.getLabel(),
                        status,
                        ex.getMessage(),
                        "REQUEST",
                        "NONFATAL"
                ),
                status
        );
    }
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {

            Map<String, String> errors = new HashMap<>();

            ex.getBindingResult().getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );

            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {

        ex.printStackTrace();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(
                buildError(
                        "Internal Server Error",
                        status,
                        "Something went wrong",
                        "SYSTEM",
                        "FATAL"
                ),
                status
        );
    }
}






