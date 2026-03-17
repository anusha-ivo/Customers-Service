package com.ordermanagement.customer.exceptions;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String label;

    public AppException(String message, HttpStatus status, String label) {
        super(message);
        this.status = status;
        this.label = label;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getLabel() {
        return label;
    }
}