package com.ordermanagement.customer.exceptions;

import org.springframework.http.HttpStatus;

public class CustomerNotFound extends AppException {

    public CustomerNotFound(long id) {
        super(
                "Customer not found with id: " + id,
                HttpStatus.NOT_FOUND,
                "Not Found"
        );
    }
}