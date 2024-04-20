package org.ordering.customer.service.domain;

import org.ordering.customer.service.domain.create.CreateCustomerCommand;
import org.ordering.customer.service.domain.entity.Customer;
import org.ordering.customer.service.domain.event.CustomerCreatedEvent;
import org.ordering.customer.service.domain.exception.CustomerDomainException;
import org.ordering.customer.service.domain.mapper.CustomerDataMapper;
import org.ordering.customer.service.domain.ports.output.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
class CustomerCreateCommandHandler {

    private final CustomerDomainService customerDomainService;

    private final CustomerRepository customerRepository;

    private final CustomerDataMapper customerDataMapper;

    public CustomerCreateCommandHandler(CustomerDomainService customerDomainService,
                                        CustomerRepository customerRepository,
                                        CustomerDataMapper customerDataMapper) {
        this.customerDomainService = customerDomainService;
        this.customerRepository = customerRepository;
        this.customerDataMapper = customerDataMapper;
    }

    @Transactional
    public CustomerCreatedEvent createCustomer(CreateCustomerCommand createCustomerCommand) {
        Customer customer = customerDataMapper.createCustomerCommandToCustomer(createCustomerCommand);
        CustomerCreatedEvent customerCreatedEvent = customerDomainService.validateAndInitiateCustomer(customer);
        Customer savedCustomer = customerRepository.createCustomer(customer);
        if (savedCustomer == null) {
            log.error("Could not save customer with id: {}", createCustomerCommand.getUsername());
            throw new CustomerDomainException("Could not save customer with id " +
                    createCustomerCommand.getUsername());
        }
        log.info("Returning CustomerCreatedEvent for customer id: {}", createCustomerCommand.getUsername());
        return customerCreatedEvent;
    }
}
