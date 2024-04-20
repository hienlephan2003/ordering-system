package org.ordering.customer.service;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableJpaRepositories(basePackages = { "org.ordering.customer.service.dataaccess", "org.ordering.dataaccess"})
@EntityScan(basePackages = { "org.ordering.customer.service.dataaccess", "org.ordering.dataaccess" })
@SpringBootApplication(scanBasePackages =  "org.ordering")
public class CustomerServiceApplication {
    public static void main(String[] args) {
        System.out.println("Server is running");
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}