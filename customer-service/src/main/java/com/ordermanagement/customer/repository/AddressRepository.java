package com.ordermanagement.customer.repository;

import com.ordermanagement.customer.config.SqlQueryProvider;
import com.ordermanagement.customer.entity.Address;
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

    public long insertAddress(Address address){
        String   insertQuery = sqlQueryProvider.getQuery("address.insert");

        jdbcTemplate.update(
                insertQuery,
                address.getCustomerId(),
                address.getLabel(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPostalCode(),
                address.getIsDefault()
        );
        return address.getCustomerId();
    }

    public void unsetDefaultAddress(long customerId) {
        String unsetDefaultQuery = sqlQueryProvider.getQuery("address.unsetDefault");
        jdbcTemplate.update(unsetDefaultQuery, customerId);
    }

    public void updateAddress(Address address) {
        String updateQuery = sqlQueryProvider.getQuery("address.update");

        jdbcTemplate.update(
                updateQuery,
                address.getLabel(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPostalCode(),
                address.getIsDefault(),
                address.getId(),
                address.getCustomerId()
        );
    }

    public boolean isDefaultAddress(long addressId) {
        String isDefaultQuery= sqlQueryProvider.getQuery("address.isDefault");
        Boolean result = jdbcTemplate.queryForObject(
                isDefaultQuery,
                Boolean.class,
                addressId
        );

        return Boolean.TRUE.equals(result);
    }

    public long countAddresses(long customerId) {
        String countQuery = sqlQueryProvider.getQuery("address.countByCustomerId");
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
        String existsQuery = sqlQueryProvider.getQuery("address.existsByIdAndCustomerId");
        Integer count = jdbcTemplate.queryForObject(
                existsQuery,
                Integer.class,
                addressId,
                customerId
        );

        return count != null && count > 0;
    }

    public void markAsDefault(long addressId) {
        String markDefaultQuery = sqlQueryProvider.getQuery("address.markAsDefault");

        jdbcTemplate.update(markDefaultQuery, addressId);
    }
    public List<Address> findByCustomerId(long customerId) {
        String  findByCustomerIdQuery = sqlQueryProvider.getQuery("address.findByCustomerId");
        return jdbcTemplate.query(
                findByCustomerIdQuery,
                (rs, rowNum) -> {
                    Address a = new Address();
                    a.setId(rs.getLong("address_id"));
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
    public Address findAddressById(long addressId) {
        String query = sqlQueryProvider.getQuery("address.findById");

        return jdbcTemplate.queryForObject(
                query,
                (rs, rowNum) -> {
                    Address a = new Address();
                    a.setId(rs.getLong("address_id"));
                    a.setCustomerId(rs.getLong("customer_id"));
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
                addressId
        );
    }
}