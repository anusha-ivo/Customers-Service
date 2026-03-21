package com.ordermanagement.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating or updating a customer address")
public class AddressRequest {

    @Schema(description = "Label for the address (e.g., Home, Office)", example = "Home")
    @NotBlank(message = "Label is required")
    private String label;

    @Schema(description = "Primary address line (street, house number, etc.)", example = "123 MG Road")
    @NotBlank(message = "Line1 is required")
    private String line1;

    @Schema(description = "Secondary address line (optional)", example = "Near City Mall")
    private String line2;

    @Schema(description = "City name", example = "Bangalore")
    @NotBlank(message = "City is required")
    private String city;

    @Schema(description = "State name", example = "Karnataka")
    @NotBlank(message = "State is required")
    private String state;

    @Schema(description = "Country name", example = "India")
    @NotBlank(message = "Country is required")
    private String country;

    @Schema(description = "Postal/ZIP code", example = "560001")
    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @Schema(description = "Marks this address as default for the customer", example = "true")
    @NotNull(message = "isDefault must be true or false")
    private Boolean isDefault;
}