package com.ordermanagement.customer.exceptions;


import org.springframework.http.HttpStatus;

public class InvalidOperationException extends AppException {

    public InvalidOperationException(String message) {
        super(
                message,
                HttpStatus.BAD_REQUEST,
                "Bad Request"
        );
    }
}