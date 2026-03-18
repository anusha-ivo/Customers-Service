package com.ordermanagement.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request object for creating or updating a customer address")
public class AddressRequest {

    @NotBlank(message = "Label is required")
    @Schema(description = "Address label (e.g., Home, Office)", example = "Home", required = true)
    private String label;

    @NotBlank(message = "Line1 is required")
    @Schema(description = "Primary address line", example = "123 Main Street", required = true)
    private String line1;

    @Schema(description = "Secondary address line (optional)", example = "Near Metro Station")
    private String line2;

    @NotBlank(message = "City is required")
    @Schema(description = "City name", example = "Bangalore", required = true)
    private String city;

    @NotBlank(message = "State is required")
    @Schema(description = "State name", example = "Karnataka", required = true)
    private String state;

    @NotBlank(message = "Country is required")
    @Schema(description = "Country name", example = "India", required = true)
    private String country;

    @NotBlank(message = "Postal code is required")
    @Schema(description = "Postal/ZIP code", example = "560001", required = true)
    private String postalCode;

    @NotNull(message = "isDefault must be true or false")
    @Schema(description = "Indicates whether this is the default address", example = "true", required = true)
    private Boolean isDefault;
}