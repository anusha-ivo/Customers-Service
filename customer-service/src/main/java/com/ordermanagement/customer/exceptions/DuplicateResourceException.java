package com.ordermanagement.customer.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends CustomerException {

    public DuplicateResourceException(String message) {
        super(
                message,
                HttpStatus.CONFLICT,
                "Conflict"
        );
    }
}
