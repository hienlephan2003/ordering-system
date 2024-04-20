package org.ordering.customer.service.domain;


import org.ordering.customer.service.domain.entity.Customer;
import org.ordering.customer.service.domain.event.CustomerCreatedEvent;

public interface CustomerDomainService {

    CustomerCreatedEvent validateAndInitiateCustomer(Customer customer);

}
