package com.ordermanagement.customer.service;

import com.ordermanagement.customer.dto.AddressRequest;
import com.ordermanagement.customer.dto.AddressResponse;
import com.ordermanagement.customer.dto.CustomerRequest;
import com.ordermanagement.customer.dto.CustomerResponse;
import com.ordermanagement.customer.exceptions.CustomerException;
import com.ordermanagement.customer.repository.AddressRepository;
import com.ordermanagement.customer.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public CustomerService(AddressRepository addressRepository,
                           CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request)  {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new CustomerException("Email already exists", HttpStatus.CONFLICT, "DUPLICATE_EMAIL");
        }

        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new CustomerException("Phone already exists", HttpStatus.CONFLICT, "DUPLICATE_PHONE");
        }
        if (request.getAddress() == null || request.getAddress().isEmpty()) {
            throw new CustomerException("At least one address is required for a Customer ", HttpStatus.BAD_REQUEST, "ADDRESS_REQUIRED");
        }
        long defaultCount = request.getAddress()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .count();

        if (defaultCount != 1) {
            throw new CustomerException("Exactly one default address required", HttpStatus.BAD_REQUEST, "INVALID_DEFAULT_ADDRESS");
        }

        long customerId = customerRepository.insertCustomer(
                request.getName(),
                request.getEmail(),
                request.getPhone()
        );

        for (AddressRequest addressRequest : request.getAddress()) {
            addressRepository.insertAddress(customerId, addressRequest);
        }

        return getCustomer(customerId);
    }

    @Transactional
    public CustomerResponse deleteCustomer(long customerId)
            {

        validateCustomerExists(customerId);

        CustomerResponse response = getCustomer(customerId);//only one

        customerRepository.deleteCustomer(customerId);

        return response;//no content in ,no content or successfulyy dlteddd
    }

    @Transactional
    public CustomerResponse updateCustomer(long customerId,
                                           CustomerRequest request)
            {


        CustomerResponse existingCustomer = customerRepository.findById(customerId);
                if (existingCustomer == null) {
                    throw new CustomerException(
                            "Customer not found with id " + customerId,
                            HttpStatus.NOT_FOUND,
                            "CUSTOMER_NOT_FOUND"
                    );
                }

                String email = request.getEmail().trim().toLowerCase();
        if (!existingCustomer.getEmail().equals(request.getEmail()) &&
                customerRepository.existsByEmailForOtherCustomer(
                        request.getEmail(), customerId)) {//dlt this

            throw new CustomerException("Email already exists", HttpStatus.CONFLICT, "DUPLICATE_EMAIL");
        }

                if (!existingCustomer.getEmail().trim().equalsIgnoreCase(request.getEmail().trim()) &&
                        customerRepository.existsByEmailForOtherCustomer(
                                request.getEmail().trim(), customerId)) {

                    throw new CustomerException(
                            "Email already exists",
                            HttpStatus.CONFLICT,
                            "DUPLICATE_EMAIL"
                    );
                }

        customerRepository.updateCustomer(
                customerId,
                request.getName(),
                request.getEmail(),
                request.getPhone()
        );

        return getCustomer(customerId);
    }

    @Transactional
    public CustomerResponse createAddress(long customerId,
                                          AddressRequest request)
             {

        validateCustomerExists(customerId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.unsetDefaultAddress(customerId);
        }

        addressRepository.insertAddress(customerId, request);

        return getCustomer(customerId);//return only address
    }

    @Transactional
    public CustomerResponse updateAddress(long customerId,
                                          long addressId,
                                          AddressRequest request)
            {

        validateCustomerExists(customerId);
        validateAddressExists(customerId, addressId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.unsetDefaultAddress(customerId);
        }

        addressRepository.updateAddress(addressId, customerId, request);

        return getCustomer(customerId);
    }

    @Transactional
    public CustomerResponse deleteAddress(long customerId,
                                          long addressId)
             {

        validateCustomerExists(customerId);
        validateAddressExists(customerId, addressId);

        if (addressRepository.isDefaultAddress(addressId)) {
            long total = addressRepository.countAddresses(customerId);
            if (total <= 1) {
                throw new CustomerException(
                        "Cannot delete default address without another default",
                        HttpStatus.BAD_REQUEST,
                        "INVALID_DELETE_DEFAULT_ADDRESS"
                );
            }
        }

        addressRepository.deleteAddress(addressId);

        return getCustomer(customerId);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(long customerId)
             {



        CustomerResponse customer = customerRepository.findById(customerId);

                 if (customer == null) {
                     throw new CustomerException(
                             "Customer not found with id " + customerId,
                             HttpStatus.NOT_FOUND,
                             "CUSTOMER_NOT_FOUND"
                     );
                 }
        List<AddressResponse> addresses =
                addressRepository.findByCustomerId(customerId);

        customer.setAddresses(addresses);

        return customer;
    }

    private void validateCustomerExists(long customerId)
            {

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerException(
                    "Customer not found with id " + customerId,
                    HttpStatus.NOT_FOUND,
                    "CUSTOMER_NOT_FOUND"
            );
        }
    }

    private void validateAddressExists(long customerId,
                                       long addressId)
             {

        if (!addressRepository.existsByIdAndCustomerId(addressId, customerId)) {
            throw new CustomerException(
                    "Address not found with id " + addressId,
                    HttpStatus.NOT_FOUND,
                    "ADDRESS_NOT_FOUND"
            );
        }
    }
}