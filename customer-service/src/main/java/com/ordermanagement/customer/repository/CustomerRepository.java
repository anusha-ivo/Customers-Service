package com.ordermanagement.customer.repository;

import com.ordermanagement.customer.config.SqlQueryProvider;
import com.ordermanagement.customer.dto.CustomerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;


@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SqlQueryProvider sqlQueryProvider;

    public CustomerRepository(JdbcTemplate jdbcTemplate,SqlQueryProvider sqlQueryProvider){
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueryProvider=sqlQueryProvider;
    }

    public long insertCustomer(String name, String email, String phone) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
         String insertQuery=sqlQueryProvider.getQuery("customer.insert");

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps =
                            con.prepareStatement(insertQuery, new String[]{"customer_id"});
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, phone);
                    return ps;
                },
                keyHolder
        );

        return keyHolder.getKey().longValue();
    }

    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String existsPhoneQuery = sqlQueryProvider.getQuery("customer.exists-phone");
        Integer count = jdbcTemplate.queryForObject(existsPhoneQuery, Integer.class, phone);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        String existsEmailQuery = sqlQueryProvider.getQuery("customer.exists-email");
        Integer count = jdbcTemplate.queryForObject(existsEmailQuery, Integer.class, email);
        return count != null && count > 0;
    }

    public void updateCustomer(long customerId, String name, String email, String phone) {
        String updateQuery = sqlQueryProvider.getQuery("customer.update");
        jdbcTemplate.update(updateQuery, name, email, phone, customerId);
    }

    public void deleteCustomer(long customerId) {
        String deleteQuery= sqlQueryProvider.getQuery("customer.delete");
        jdbcTemplate.update(deleteQuery, customerId);
    }

    public boolean existsByEmailForOtherCustomer(String email, long customerId) {
        String existsEmailOtherQuery= sqlQueryProvider.getQuery("customer.exists-email-other");

        Integer count = jdbcTemplate.queryForObject(existsEmailOtherQuery, Integer.class, email, customerId);
        return count != null && count > 0;
    }

    public boolean existsByPhoneForOtherCustomer(String phone, long customerId) {
        String existsPhoneOtherQuery= sqlQueryProvider.getQuery("customer.exists-phone-other");
        if (phone == null || phone.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(existsPhoneOtherQuery, Integer.class, phone, customerId);
        return count != null && count > 0;
    }
    public boolean existsById(long customerId) {
        String  existsIdQuery = sqlQueryProvider.getQuery("customer.exists-id");
        Integer count = jdbcTemplate.queryForObject(
                existsIdQuery,
                Integer.class,
                customerId
        );
        return count != null && count > 0;
    }


    public CustomerResponse findById(long customerId) {
        String  findByIdQuery = sqlQueryProvider.getQuery("customer.find-by-id");

        return jdbcTemplate.queryForObject(
                findByIdQuery,
                (rs, rowNum) -> {

                    CustomerResponse c = new CustomerResponse();

                    c.setCustomerId(rs.getLong("customer_id"));
                    c.setName(rs.getString("name"));
                    c.setEmail(rs.getString("email"));
                    c.setPhone(rs.getString("phone"));


                    if (rs.getTimestamp("created_at") != null) {
                        c.setCreatedAt(
                                rs.getTimestamp("created_at").toLocalDateTime()
                        );
                    }

                    if (rs.getTimestamp("updated_at") != null) {
                        c.setUpdatedAt(
                                rs.getTimestamp("updated_at").toLocalDateTime()
                        );
                    }

                    return c;
                },
                customerId
        );
    }

}