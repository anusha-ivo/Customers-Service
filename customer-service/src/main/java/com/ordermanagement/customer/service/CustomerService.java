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
            throw new CustomerException(
                    "Email already exists",
                    HttpStatus.CONFLICT,
                    "DUPLICATE_EMAIL"
            );
        }

        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new CustomerException(
                    "Phone already exists",
                    HttpStatus.CONFLICT,
                    "DUPLICATE_PHONE"
            );
        }
        if (request.getAddress() == null || request.getAddress().isEmpty()) {
            throw new CustomerException(
                    "At least one address is required",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_ADDRESS"
            );
        }
        long defaultCount = request.getAddress()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .count();

        if (defaultCount != 1) {
            throw new CustomerException(
                    "Exactly one default address required",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_DEFAULT_ADDRESS"
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
    public void deleteCustomer(long customerId) {

        Customer customer = customerRepository.findById(customerId);

        if (customer == null) {
            throw new CustomerException(
                    "Customer not found",
                    HttpStatus.NOT_FOUND,
                    "CUSTOMER_NOT_FOUND"
            );
        }

        customerRepository.deleteCustomer(customerId);
    }
    @Transactional
    public CustomerResponse updateCustomer(long customerId,
                                           CustomerRequest request)
          {


        Customer existingCustomer = customerRepository.findById(customerId);
              if (existingCustomer == null) {
                  throw new CustomerException(
                          "Customer not found",
                          HttpStatus.NOT_FOUND,
                          "CUSTOMER_NOT_FOUND"
                  );
              }

        if (!existingCustomer.getEmail().equals(request.getEmail()) &&
                customerRepository.existsByEmailForOtherCustomer(
                        request.getEmail(), customerId)) {

            throw new CustomerException(
                    "Email already exists",
                    HttpStatus.CONFLICT,
                    "DUPLICATE_EMAIL"
            );
        }


        if (!existingCustomer.getPhone().equals(request.getPhone()) &&
                customerRepository.existsByPhoneForOtherCustomer(
                        request.getPhone(), customerId)) {

            throw new CustomerException(
                    "Phone already exists",
                    HttpStatus.CONFLICT,
                    "DUPLICATE_PHONE"
            );
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
             {

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
            {

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
             {

        validateCustomerExists(customerId);
        validateAddressExists(customerId, addressId);

        if (addressRepository.isDefaultAddress(addressId)) {
            long total = addressRepository.countAddresses(customerId);
            if (total <= 1) {
                throw new CustomerException(
                        "Cannot delete last default address",
                        HttpStatus.BAD_REQUEST,
                        "INVALID_OPERATION"
                );
            }
        }

        addressRepository.deleteAddress(addressId);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(long customerId)
             {

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
            {

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
             {

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerException(
                    "Customer not found",
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
                    "Address not found",
                    HttpStatus.NOT_FOUND,
                    "ADDRESS_NOT_FOUND"
            );
        }
    }
}