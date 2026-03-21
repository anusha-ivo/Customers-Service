package com.ordermanagement.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response payload representing a customer address")
public class AddressResponse {

    @Schema(description = "Unique identifier of the address", example = "101")
    private Long addressId;

    @Schema(description = "Label for the address (e.g., Home, Office)", example = "Home")
    private String label;

    @Schema(description = "Primary address line", example = "123 MG Road")
    private String line1;

    @Schema(description = "Secondary address line", example = "Near City Mall")
    private String line2;

    @Schema(description = "City name", example = "Bangalore")
    private String city;

    @Schema(description = "State name", example = "Karnataka")
    private String state;

    @Schema(description = "Country name", example = "India")
    private String country;

    @Schema(description = "Postal or ZIP code", example = "560001")
    private String postalCode;

    @Schema(description = "Indicates whether this address is the default address for the customer", example = "true")
    private Boolean isDefault;
}