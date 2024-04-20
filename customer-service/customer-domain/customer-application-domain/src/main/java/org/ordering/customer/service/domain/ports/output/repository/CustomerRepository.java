package org.ordering.customer.service.domain.ports.output.repository;

import org.ordering.customer.service.domain.entity.Customer;

public interface CustomerRepository {

    Customer createCustomer(Customer customer);
}
