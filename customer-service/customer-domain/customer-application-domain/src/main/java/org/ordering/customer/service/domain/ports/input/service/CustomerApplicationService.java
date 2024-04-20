package org.ordering.customer.service.domain.ports.input.service;

import jakarta.validation.Valid;
import org.ordering.customer.service.domain.create.CreateCustomerCommand;
import org.ordering.customer.service.domain.create.CreateCustomerResponse;


public interface CustomerApplicationService {

    CreateCustomerResponse createCustomer(@Valid CreateCustomerCommand createCustomerCommand);

}
