package org.ordering.customer.service.domain.mapper;

import org.ordering.customer.service.domain.create.CreateCustomerCommand;
import org.ordering.customer.service.domain.create.CreateCustomerResponse;
import org.ordering.customer.service.domain.entity.Customer;
import org.ordering.domain.valueobject.CustomerId;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomerDataMapper {

    public Customer createCustomerCommandToCustomer(CreateCustomerCommand createCustomerCommand) {
        return new Customer(new CustomerId(UUID.randomUUID()),
                createCustomerCommand.getUsername(),
                createCustomerCommand.getFirstName(),
                createCustomerCommand.getLastName());
    }

    public CreateCustomerResponse customerToCreateCustomerResponse(Customer customer, String message) {
        return new CreateCustomerResponse(customer.getId().getValue(), message);
    }
}
