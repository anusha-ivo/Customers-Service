package com.ordermanagement.customer.service;

import com.ordermanagement.customer.dto.AddressRequest;
import com.ordermanagement.customer.dto.AddressResponse;
import com.ordermanagement.customer.dto.CustomerRequest;
import com.ordermanagement.customer.dto.CustomerResponse;
import com.ordermanagement.customer.entity.Address;
import com.ordermanagement.customer.entity.Customer;
import com.ordermanagement.customer.exceptions.*;
import com.ordermanagement.customer.repository.AddressRepository;
import com.ordermanagement.customer.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

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

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        long customerId = customerRepository.insertCustomer(customer);

        for (AddressRequest addressRequest : request.getAddress()) {
            Address address = new Address();
            address.setCustomerId(customerId);
            address.setLabel(addressRequest.getLabel());
            address.setLine1(addressRequest.getLine1());
            address.setLine2(addressRequest.getLine2());
            address.setCity(addressRequest.getCity());
            address.setState(addressRequest.getState());
            address.setCountry(addressRequest.getCountry());
            address.setPostalCode(addressRequest.getPostalCode());
            address.setIsDefault(addressRequest.getIsDefault());

            addressRepository.insertAddress(address);
        }

        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(customerId);
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());


        List<AddressResponse> addresses = request.getAddress().stream().map(a -> {
            AddressResponse res = new AddressResponse();
            res.setLabel(a.getLabel());
            res.setLine1(a.getLine1());
            res.setLine2(a.getLine2());
            res.setCity(a.getCity());
            res.setState(a.getState());
            res.setCountry(a.getCountry());
            res.setPostalCode(a.getPostalCode());
            res.setIsDefault(a.getIsDefault());
            return res;
        }).toList();

        response.setAddresses(addresses);

        return response;
    }

    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(long customerId)
            throws CustomerNotFound {

        validateCustomerExists(customerId);

        customerRepository.deleteCustomer(customerId);
    }

    @Transactional
    public CustomerResponse updateCustomer(long customerId,
                                           CustomerRequest request)
            throws CustomerNotFound, DuplicateResourceException {

        validateCustomerExists(customerId);
        Customer existingCustomer = customerRepository.findById(customerId);

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

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        customerRepository.updateCustomer(customer);

        return getCustomer(customerId);
    }

    @Transactional
    public AddressResponse createAddress(long customerId,
                                         AddressRequest request)
            throws CustomerNotFound {

        validateCustomerExists(customerId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.unsetDefaultAddress(customerId);
        }

        Address address = new Address();
        address.setCustomerId(customerId);
        address.setLabel(request.getLabel());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setIsDefault(request.getIsDefault());

        long addressId = addressRepository.insertAddress(address);

        AddressResponse response = new AddressResponse();
        response.setAddressId(addressId);
        response.setLabel(request.getLabel());
        response.setLine1(request.getLine1());
        response.setLine2(request.getLine2());
        response.setCity(request.getCity());
        response.setState(request.getState());
        response.setCountry(request.getCountry());
        response.setPostalCode(request.getPostalCode());
        response.setIsDefault(request.getIsDefault());

        return response;
    }

    @Transactional
    public AddressResponse updateAddress(long customerId,
                                          long addressId,
                                          AddressRequest request)
            throws CustomerNotFound, AddressNotFoundException {

        validateCustomerExists(customerId);
        validateAddressExists(customerId, addressId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.unsetDefaultAddress(customerId);
        }

        Address address = new Address();
        address.setId(addressId);
        address.setCustomerId(customerId);
        address.setLabel(request.getLabel());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setIsDefault(request.getIsDefault());

        addressRepository.updateAddress(address);

        AddressResponse response = new AddressResponse();
        response.setAddressId(addressId);
        response.setLabel(request.getLabel());
        response.setLine1(request.getLine1());
        response.setLine2(request.getLine2());
        response.setCity(request.getCity());
        response.setState(request.getState());
        response.setCountry(request.getCountry());
        response.setPostalCode(request.getPostalCode());
        response.setIsDefault(request.getIsDefault());
        return response;
    }

    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(long customerId, long addressId)
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
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(long customerId)
            throws CustomerNotFound {

        validateCustomerExists(customerId);

        Customer customer = customerRepository.findById(customerId);

        List<Address> address =
                addressRepository.findByCustomerId(customerId);
        List<AddressResponse> addresses = address.stream().map(a -> {
            AddressResponse res = new AddressResponse();
            res.setAddressId(a.getId());
            res.setLabel(a.getLabel());
            res.setLine1(a.getLine1());
            res.setLine2(a.getLine2());
            res.setCity(a.getCity());
            res.setState(a.getState());
            res.setCountry(a.getCountry());
            res.setPostalCode(a.getPostalCode());
            res.setIsDefault(a.getIsDefault());
            return res;
        }).toList();


        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        response.setAddresses(addresses);

        return response;
    }
    @Transactional
    public AddressResponse setDefaultAddress(long customerId, long addressId)
            throws CustomerNotFound, AddressNotFoundException {

        validateCustomerExists(customerId);
        validateAddressExists(customerId, addressId);


        addressRepository.unsetDefaultAddress(customerId);


        addressRepository.markAsDefault(addressId);

        Address address = addressRepository.findAddressById(addressId);

        AddressResponse response = new AddressResponse();
        response.setAddressId(address.getId());
        response.setLabel(address.getLabel());
        response.setLine1(address.getLine1());
        response.setLine2(address.getLine2());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setCountry(address.getCountry());
        response.setPostalCode(address.getPostalCode());
        response.setIsDefault(true);

        return response;
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