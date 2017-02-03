package com.ge.predix.labs.data.jpa.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.ejb.HibernatePersistence;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.ge.predix.labs.data.jpa.domain.Customer;

@SuppressWarnings("deprecation")
@Configuration
public class CloudFoundryDataSourceConfiguration extends AbstractCloudConfig  {

    @Bean
    public DataSource dataSource() {
    		return connectionFactory().dataSource();
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean( DataSource dataSource  ) throws Exception {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource( dataSource );
        em.setPackagesToScan(Customer.class.getPackage().getName());
        em.setPersistenceProvider(new HibernatePersistence());
        Map<String, String> p = new HashMap<String, String>();
//        p.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, "create");
//        p.put(org.hibernate.cfg.Environment.HBM2DDL_IMPORT_FILES, "initialCustomers.sql");
        p.put(org.hibernate.cfg.Environment.DIALECT, PostgreSQLDialect.class.getName());
        p.put(org.hibernate.cfg.Environment.SHOW_SQL, "true");
        em.setJpaPropertyMap(p);
        return em;
    }
}

