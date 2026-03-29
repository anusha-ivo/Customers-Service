package com.ordermanagement.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Response payload containing customer details along with addresses")
public class CustomerResponse {

    @Schema(description = "Unique identifier of the customer", example = "1001")
    private Long customerId;

    @Schema(description = "Full name of the customer", example = "Anusha JM")
    private String name;

    @Schema(description = "Email address of the customer", example = "anusha@example.com")
    private String email;

    @Schema(description = "Phone number of the customer", example = "9876543210")
    private String phone;


    @Schema(
            description = "List of addresses associated with the customer",
            implementation = AddressResponse.class
    )
    private List<AddressResponse> addresses;
}