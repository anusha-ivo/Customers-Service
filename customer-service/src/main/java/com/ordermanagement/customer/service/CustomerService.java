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

import static java.util.stream.Collectors.toList;

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

        Customer customer = mapToCustomerEntity(request, null);//dto->entity,convert req to db object

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

        CustomerResponse response = new CustomerResponse();//api response object
        response.setCustomerId(customerId);
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());


        List<AddressResponse> addresses = request.getAddress().stream().map(a -> {
            AddressResponse res = mapToAddressResponse(a,null);
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

        //convert  dto here entity
              Customer customer = mapToCustomerEntity(request, customerId);

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

                 Address address = mapToNewAddressEntity(request,customerId);//creating entity to store in db


                 long addressId = addressRepository.insertAddress(address);


        AddressResponse response = mapToAddressResponse(request, addressId);

        return response;
    }

    @Transactional
    public AddressResponse updateAddress(long customerId,
                                          long addressId,
                                          AddressRequest request)
            {

        validateCustomerExists(customerId);
        validateAddressExists(customerId, addressId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {//new address mark as default n then remove existing one as default
            addressRepository.unsetDefaultAddress(customerId);
        }

                Address address = mapToExistingAddressEntity(request, customerId, addressId);

        addressRepository.updateAddress(address);

                AddressResponse response = mapToAddressResponse(request, addressId);
        return response;
    }

    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(long customerId, long addressId)
             {

        //validateCustomerExists(customerId);
        validateAddressExists(customerId, addressId);//check address belong to perticular customer

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


        Customer customer = customerRepository.findById(customerId);
                 if (customer == null) {
                     throw new CustomerException(
                             "Customer not found",
                             HttpStatus.NOT_FOUND,
                             "CUSTOMER_NOT_FOUND"
                     );
                 }
        List<Address> address =
                addressRepository.findByCustomerId(customerId);
                 List<AddressResponse> addresses = address.stream()
                         .map(a -> mapToAddressResponse(a))
                         .toList();


        CustomerResponse response = new CustomerResponse();//create mtd to convert response
        response.setCustomerId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());

        response.setAddresses(addresses);

        return response;
    }
    @Transactional
    public AddressResponse setDefaultAddress(long customerId, long addressId)
            {

        validateAddressExists(customerId, addressId);


        addressRepository.unsetDefaultAddress(customerId);


        addressRepository.markAsDefault(addressId);

       // Address address = addressRepository.findAddressById(addressId);

                Address address = addressRepository.findAddressById(addressId);
                AddressResponse response = mapToAddressResponse(address);
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
    private Customer mapToCustomerEntity(CustomerRequest request, Long id) {//remove id,n rename method
        Customer customer = new Customer();
        if (id != null) customer.setId(id);
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        return customer;
    }

    private AddressResponse mapToAddressResponse(Address address) {
        AddressResponse res = new AddressResponse();
        res.setAddressId(address.getId());
        res.setLabel(address.getLabel());
        res.setLine1(address.getLine1());
        res.setLine2(address.getLine2());
        res.setCity(address.getCity());
        res.setState(address.getState());
        res.setCountry(address.getCountry());
        res.setPostalCode(address.getPostalCode());
        res.setIsDefault(address.getIsDefault());
        return res;
    }

    private AddressResponse mapToAddressResponse(AddressRequest request, Long addressId) {
        AddressResponse res = new AddressResponse();
        res.setAddressId(addressId);
        res.setLabel(request.getLabel());
        res.setLine1(request.getLine1());
        res.setLine2(request.getLine2());
        res.setCity(request.getCity());
        res.setState(request.getState());
        res.setCountry(request.getCountry());
        res.setPostalCode(request.getPostalCode());
        res.setIsDefault(request.getIsDefault());
        return res;
    }

    private Address mapToNewAddressEntity(AddressRequest request, Long customerId) {
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

        return address;
    }
    private Address mapToExistingAddressEntity(AddressRequest request, Long customerId, Long addressId) {
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

        return address;
    }
}