package com.ordermanagement.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Customer address response")
public class AddressResponse {

    @Schema(description = "Unique address ID", example = "101")
    private Long addressId;

    @Schema(description = "Address label (Home, Office, etc.)", example = "Home")
    private String label;

    @Schema(description = "Primary address line", example = "123 Main Street")
    private String line1;

    @Schema(description = "Secondary address line", example = "Near Metro Station")
    private String line2;

    @Schema(description = "City name", example = "Bangalore")
    private String city;

    @Schema(description = "State name", example = "Karnataka")
    private String state;

    @Schema(description = "Country name", example = "India")
    private String country;

    @Schema(description = "Postal code", example = "560001")
    private String postalCode;

    @Schema(description = "Indicates if this is default address", example = "true")
    private Boolean isDefault;
}