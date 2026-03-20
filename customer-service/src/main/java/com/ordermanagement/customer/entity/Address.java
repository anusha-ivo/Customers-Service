package com.ordermanagement.customer.entity;

import lombok.Data;

@Data
public class Address {
    private Long id;
    private Long customerId;

    private String label;
    private String line1;
    private String line2;
    private String city;
    private String state;

    private String country;
    private String postalCode;

    private Boolean isDefault;
}
