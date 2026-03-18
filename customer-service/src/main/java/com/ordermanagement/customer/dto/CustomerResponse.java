package com.ordermanagement.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Customer response object")
public class CustomerResponse {

    @Schema(description = "Unique customer ID", example = "1001")
    private Long customerId;

    @Schema(description = "Customer full name", example = "Anusha JM")
    private String name;

    @Schema(description = "Customer email address", example = "anusha@gmail.com")
    private String email;

    @Schema(description = "Customer phone number", example = "9876543210")
    private String phone;

    @Schema(description = "Customer creation timestamp", example = "2026-03-18T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Customer last updated timestamp", example = "2026-03-18T12:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "List of customer addresses")
    private List<AddressResponse> addresses;
}