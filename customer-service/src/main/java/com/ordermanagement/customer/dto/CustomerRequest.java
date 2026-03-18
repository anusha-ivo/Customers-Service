package com.ordermanagement.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request object for creating or updating a customer")
public class CustomerRequest {

    @NotBlank(message = "Name is required")
    @Schema(
            description = "Customer full name",
            example = "Anusha JM",
            required = true
    )
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(
            description = "Customer email address",
            example = "anusha@gmail.com",
            required = true
    )
    private String email;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone must be 10 digits"
    )
    @Schema(
            description = "Customer phone number (10 digits)",
            example = "9876543210"
    )
    private String phone;

    @Schema(
            description = "List of customer addresses",
            required = true
    )
    private List<AddressRequest> address;
}