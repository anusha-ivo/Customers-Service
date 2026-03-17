package com.ordermanagement.customer.repository;

import com.ordermanagement.customer.config.SqlQueryProvider;
import com.ordermanagement.customer.dto.AddressRequest;
import com.ordermanagement.customer.dto.AddressResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AddressRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SqlQueryProvider sqlQueryProvider;

    public AddressRepository(JdbcTemplate jdbcTemplate,SqlQueryProvider sqlQueryProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueryProvider=sqlQueryProvider;
    }

    public long insertAddress(long customerId,
                              AddressRequest addressRequest) {
        String   insertQuery = sqlQueryProvider.getQuery("address.insert");

        jdbcTemplate.update(
                insertQuery,
                customerId,
                addressRequest.getLabel(),
                addressRequest.getLine1(),
                addressRequest.getLine2(),
                addressRequest.getCity(),
                addressRequest.getState(),
                addressRequest.getCountry(),
                addressRequest.getPostalCode(),
                addressRequest.getIsDefault()
        );
        return customerId;
    }

    public void unsetDefaultAddress(long customerId) {
        String unsetDefaultQuery = sqlQueryProvider.getQuery("address.unset-default");
        jdbcTemplate.update(unsetDefaultQuery, customerId);
    }

    public void updateAddress(long addressId,
                              long customerId,
                              AddressRequest request) {
        String updateQuery = sqlQueryProvider.getQuery("address.update");

        jdbcTemplate.update(
                updateQuery,
                request.getLabel(),
                request.getLine1(),
                request.getLine2(),
                request.getCity(),
                request.getState(),
                request.getCountry(),
                request.getPostalCode(),
                request.getIsDefault(),
                addressId,
                customerId
        );
    }

    public boolean isDefaultAddress(long addressId) {
        String isDefaultQuery= sqlQueryProvider.getQuery("address.is-default");
        Boolean result = jdbcTemplate.queryForObject(
                isDefaultQuery,
                Boolean.class,
                addressId
        );

        return Boolean.TRUE.equals(result);
    }

    public long countAddresses(long customerId) {
        String countQuery = sqlQueryProvider.getQuery("address.count");
        Long count = jdbcTemplate.queryForObject(
                countQuery,
                Long.class,
                customerId
        );

        return count != null ? count : 0;
    }

    public void deleteAddress(long addressId)
    {
        String deleteQuery = sqlQueryProvider.getQuery("address.delete");
        jdbcTemplate.update(deleteQuery, addressId);
    }

    public boolean existsByIdAndCustomerId(long addressId, long customerId) {//check address is present or not before dlt,update so we need this method
        String existsQuery = sqlQueryProvider.getQuery("address.exists");
        Integer count = jdbcTemplate.queryForObject(
                existsQuery,
                Integer.class,
                addressId,
                customerId
        );

        return count != null && count > 0;
    }


    public List<AddressResponse> findByCustomerId(long customerId) {
        String  findByCustomerIdQuery = sqlQueryProvider.getQuery("address.find-by-customer-id");
        return jdbcTemplate.query(
                findByCustomerIdQuery,
                (rs, rowNum) -> {
                    AddressResponse a = new AddressResponse();
                    a.setAddressId(rs.getLong("address_id"));
                    a.setLabel(rs.getString("label"));
                    a.setLine1(rs.getString("line1"));
                    a.setLine2(rs.getString("line2"));
                    a.setCity(rs.getString("city"));
                    a.setState(rs.getString("state"));
                    a.setCountry(rs.getString("country"));
                    a.setPostalCode(rs.getString("postal_code"));
                    a.setIsDefault(rs.getBoolean("is_default"));
                    return a;
                },
                customerId
        );
    }
}