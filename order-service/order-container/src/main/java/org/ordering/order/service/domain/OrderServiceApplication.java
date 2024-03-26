package org.ordering.order.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = { "org.ordering.order.service.dataaccess", "org.ordering.dataaccess"})
@EntityScan(basePackages = {"org.ordering.order.service.dataaccess", "org.ordering.dataaccess"})
@SpringBootApplication(scanBasePackages = "org.ordering")
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
