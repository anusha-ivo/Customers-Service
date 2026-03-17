package com.ordermanagement.customer.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class CustomerResponse {
    private Long customerId;  // changed to Long

    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<AddressResponse> addresses;
}
