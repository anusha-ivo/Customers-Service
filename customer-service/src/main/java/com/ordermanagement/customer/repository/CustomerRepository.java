package com.ordermanagement.customer.repository;

import com.ordermanagement.customer.config.SqlQueryProvider;
import com.ordermanagement.customer.entity.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;


@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SqlQueryProvider sqlQueryProvider;

    public CustomerRepository(JdbcTemplate jdbcTemplate,SqlQueryProvider sqlQueryProvider){
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueryProvider=sqlQueryProvider;
    }

    public long insertCustomer(Customer customer) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
         String insertQuery=sqlQueryProvider.getQuery("customer.insert");

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps =
                            con.prepareStatement(insertQuery, new String[]{"customer_id"});
                    ps.setString(1, customer.getName());
                    ps.setString(2, customer.getEmail());
                    ps.setString(3, customer.getPhone());
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
        String existsPhoneQuery = sqlQueryProvider.getQuery("customer.existsByPhone");
        Integer count = jdbcTemplate.queryForObject(existsPhoneQuery, Integer.class, phone);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        String existsEmailQuery = sqlQueryProvider.getQuery("customer.existsByEmail");
        Integer count = jdbcTemplate.queryForObject(existsEmailQuery, Integer.class, email);
        return count != null && count > 0;
    }

    public void updateCustomer(Customer customer){
        String updateQuery = sqlQueryProvider.getQuery("customer.update");
        jdbcTemplate.update(
                updateQuery,
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getId()
        );
    }

    public void deleteCustomer(long customerId) {
        String deleteQuery= sqlQueryProvider.getQuery("customer.delete");
        jdbcTemplate.update(deleteQuery, customerId);
    }

    public boolean existsByEmailForOtherCustomer(String email, long customerId) {
        String existsEmailOtherQuery= sqlQueryProvider.getQuery("customer.existsByEmailForOther");

        Integer count = jdbcTemplate.queryForObject(existsEmailOtherQuery, Integer.class, email, customerId);
        return count != null && count > 0;
    }

    public boolean existsByPhoneForOtherCustomer(String phone, long customerId) {
        String existsPhoneOtherQuery= sqlQueryProvider.getQuery("customer.existsByPhoneForOther");
        if (phone == null || phone.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(existsPhoneOtherQuery, Integer.class, phone, customerId);
        return count != null && count > 0;
    }
    public boolean existsById(long customerId) {
        String  existsIdQuery = sqlQueryProvider.getQuery("customer.existsById");
        Integer count = jdbcTemplate.queryForObject(
                existsIdQuery,
                Integer.class,
                customerId
        );
        return count != null && count > 0;
    }


    public Customer findById(long customerId) {

        String findByIdQuery = sqlQueryProvider.getQuery("customer.findById");

        List<Customer> list = jdbcTemplate.query(
                findByIdQuery,
                (rs, rowNum) -> {

                    Customer c = new Customer();

                    c.setId(rs.getLong("customer_id"));
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

        return list.isEmpty() ? null : list.get(0);
    }

}