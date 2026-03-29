package com.ordermanagement.customer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SqlQueryProvider {
    @Autowired
    private Environment environment;
    public String getQuery(String key){
        String query=environment.getProperty(key);
        if(query==null){
            throw new RuntimeException("Query is not found"+key);

        }
        return query;
    }
}
