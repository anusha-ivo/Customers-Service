package com.ordermanagement.customer.service;

import com.ordermanagement.customer.dto.AddressRequest;
import com.ordermanagement.customer.dto.AddressResponse;
import com.ordermanagement.customer.dto.CustomerRequest;
import com.ordermanagement.customer.dto.CustomerResponse;
import com.ordermanagement.customer.exceptions.AddressNotFoundException;
import com.ordermanagement.customer.exceptions.CustomerNotFound;
import com.ordermanagement.customer.exceptions.DuplicateResourceException;
import com.ordermanagement.customer.exceptions.InvalidOperationException;
import com.ordermanagement.customer.repository.AddressRepository;
import com.ordermanagement.customer.repository.CustomerRepository;

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
            throw new DuplicateResourceException("Email already exists");
        }

        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone already exists");
        }
        if (request.getAddress() == null || request.getAddress().isEmpty()) {
            throw new InvalidOperationException("At least one address is required");
        }
        long defaultCount = request.getAddress()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .count();

        if (defaultCount != 1) {
            throw new InvalidOperationException(
                    "Exactly one default address required"
            );
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
            throws CustomerNotFound {

        validateCustomerExists(customerId);

        CustomerResponse response = getCustomer(customerId);

        customerRepository.deleteCustomer(customerId);

        return response;
    }

    @Transactional
    public CustomerResponse updateCustomer(long customerId,
                                           CustomerRequest request)
            throws CustomerNotFound, DuplicateResourceException {

        validateCustomerExists(customerId);
        CustomerResponse existingCustomer = customerRepository.findById(customerId);

        if (!existingCustomer.getEmail().equals(request.getEmail()) &&
                customerRepository.existsByEmailForOtherCustomer(
                        request.getEmail(), customerId)) {

            throw new DuplicateResourceException("Email already exists");
        }

        if (!existingCustomer.getPhone().equals(request.getPhone()) &&
                customerRepository.existsByPhoneForOtherCustomer(
                        request.getPhone(), customerId)) {

            throw new DuplicateResourceException("Phone already exists");
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
            throws CustomerNotFound {

        validateCustomerExists(customerId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.unsetDefaultAddress(customerId);
        }

        addressRepository.insertAddress(customerId, request);

        return getCustomer(customerId);
    }

    @Transactional
    public CustomerResponse updateAddress(long customerId,
                                          long addressId,
                                          AddressRequest request)
            throws CustomerNotFound, AddressNotFoundException {

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
            throws CustomerNotFound, AddressNotFoundException {

        validateCustomerExists(customerId);
        validateAddressExists(customerId, addressId);

        if (addressRepository.isDefaultAddress(addressId)) {
            long total = addressRepository.countAddresses(customerId);
            if (total <= 1) {
                throw new InvalidOperationException(
                        "Cannot delete default address without another default"
                );
            }
        }

        addressRepository.deleteAddress(addressId);

        return getCustomer(customerId);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(long customerId)
            throws CustomerNotFound {

        validateCustomerExists(customerId);

        CustomerResponse customer = customerRepository.findById(customerId);

        List<AddressResponse> addresses =
                addressRepository.findByCustomerId(customerId);

        customer.setAddresses(addresses);

        return customer;
    }

    private void validateCustomerExists(long customerId)
            throws CustomerNotFound {

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFound(customerId);
        }
    }

    private void validateAddressExists(long customerId,
                                       long addressId)
            throws AddressNotFoundException {

        if (!addressRepository.existsByIdAndCustomerId(addressId, customerId)) {
            throw new AddressNotFoundException(addressId);
        }
    }
}