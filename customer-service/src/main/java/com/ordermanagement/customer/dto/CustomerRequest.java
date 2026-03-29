package com.ordermanagement.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request payload for creating or updating a customer")
public class CustomerRequest {

    @Schema(description = "Full name of the customer", example = "Anusha JM")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Email address of the customer", example = "anusha@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "10-digit mobile number of the customer", example = "9876543210")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone must be 10 digits"
    )
    private String phone;

    @Schema(
            description = "List of customer addresses (home, office, etc.)",
            implementation = AddressRequest.class
    )
    private List<AddressRequest> address;
}