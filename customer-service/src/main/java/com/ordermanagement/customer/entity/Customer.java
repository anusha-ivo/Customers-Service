package com.ordermanagement.customer.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Customer {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
