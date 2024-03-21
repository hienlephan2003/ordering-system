package org.ordering.order.service.domain;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = { "org.ordering.order.service.dataaccess"})
@EntityScan(basePackages = {"org.ordering.order.service.dataaccess"})
@SpringBootApplication(scanBasePackages = "org.ordering")
public class OrderServiceApplication {
}
