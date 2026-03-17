package com.ordermanagement.customer.controller;

import com.ordermanagement.customer.dto.AddressRequest;
import com.ordermanagement.customer.dto.CustomerRequest;
import com.ordermanagement.customer.dto.CustomerResponse;
import com.ordermanagement.customer.exceptions.AddressNotFoundException;
import com.ordermanagement.customer.exceptions.CustomerNotFound;
import com.ordermanagement.customer.exceptions.DuplicateResourceException;
import com.ordermanagement.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Customer API", description = "Operations for managing customers and addresses")
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService){
        this.customerService = customerService;
    }
    @Operation(
            summary = "Create Customer",
            description = "Creates a customer with at least one default address"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer created"),
            @ApiResponse(responseCode = "409", description = "Duplicate email/phone")
    })
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @RequestHeader(value = "x-conversation-id", required = false)
            String conversationId,
            @Valid @RequestBody CustomerRequest request)
             {

        CustomerResponse response = customerService.createCustomer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @Operation(
            summary = "Delete Customer",
            description = "Deletes a customer and all associated addresses"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer deleted"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> deleteCustomer(
            @RequestHeader(value = "x-conversation-id", required = false)
            String conversationId,
            @PathVariable Long customerId)
            {

        CustomerResponse deletedCustomer =
                customerService.deleteCustomer(customerId);

        return ResponseEntity.ok(deletedCustomer);
    }
    @Operation(
            summary = "Update Customer",
            description = "Updates customer details. Duplicate email/phone not allowed"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer updated"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate email/phone")
    })
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @RequestHeader(value = "x-conversation-id", required = false)
            String conversationId,
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerRequest request)
            throws CustomerNotFound, DuplicateResourceException {

        CustomerResponse updatedCustomer =
                customerService.updateCustomer(customerId, request);

        return ResponseEntity.ok(updatedCustomer);
    }
    @Operation(
            summary = "Add Address",
            description = "Adds an address to a customer. If default=true, previous default is removed"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address added"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<CustomerResponse> createAddress(
            @RequestHeader(value = "x-conversation-id", required = false)
            String conversationId,
            @PathVariable Long customerId,
            @Valid @RequestBody AddressRequest request)
            throws CustomerNotFound {

        CustomerResponse response =
                customerService.createAddress(customerId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @Operation(
            summary = "Update Address",
            description = "Updates address details. Ensures only one default address"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address updated"),
            @ApiResponse(responseCode = "404", description = "Address/Customer not found")
    })
    @PutMapping("/{customerId}/address/{addressId}")
    public ResponseEntity<CustomerResponse> updateAddress(
            @RequestHeader(value = "x-conversation-id", required = false)
            String conversationId,
            @PathVariable Long customerId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request)
            throws CustomerNotFound, AddressNotFoundException {

        CustomerResponse response =
                customerService.updateAddress(customerId, addressId, request);

        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "Delete Address",
            description = "Deletes an address. Cannot delete default address unless another exists"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid operation"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/{customerId}/address/{addressId}")
    public ResponseEntity<CustomerResponse> deleteAddress(
            @RequestHeader(value = "x-conversation-id", required = false)
            String conversationId,
            @PathVariable Long customerId,
            @PathVariable Long addressId)
            {

        CustomerResponse response =
                customerService.deleteAddress(customerId, addressId);

        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "Get Customer",
            description = "Fetch customer details along with all addresses"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer fetched"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @RequestHeader(value = "x-conversation-id", required = false)
            String conversationId,
            @PathVariable Long customerId)
             {
                 System.out.println("Conversation ID: " + conversationId);
        return ResponseEntity.ok(
                customerService.getCustomer(customerId)
        );
    }
}