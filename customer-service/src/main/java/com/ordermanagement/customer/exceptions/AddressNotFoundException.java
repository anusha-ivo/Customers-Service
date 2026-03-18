package com.ordermanagement.customer.exceptions;

import org.springframework.http.HttpStatus;

public class AddressNotFoundException extends CustomerException {

    public AddressNotFoundException(long id) {
        super(
                "Address not found with id: " + id,
                HttpStatus.NOT_FOUND,
                "Not Found"
        );
    }
}