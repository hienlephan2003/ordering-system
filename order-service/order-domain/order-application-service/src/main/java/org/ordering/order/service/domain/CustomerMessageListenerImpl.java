package org.ordering.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.ordering.order.service.domain.dto.message.CustomerModel;
import org.ordering.order.service.domain.entity.Customer;
import org.ordering.order.service.domain.exception.OrderDomainException;
import org.ordering.order.service.domain.mapper.OrderDataMapper;
import org.ordering.order.service.domain.ports.input.message.listener.customer.CustomerMessageListener;
import org.ordering.order.service.domain.repository.CustomerRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerMessageListenerImpl implements CustomerMessageListener {
    private final CustomerRepository customerRepository;
    private final OrderDataMapper orderDataMapper;

    public CustomerMessageListenerImpl(CustomerRepository customerRepository, OrderDataMapper orderDataMapper) {
        this.customerRepository = customerRepository;
        this.orderDataMapper = orderDataMapper;
    }

    @Override
    public void customerCreated(CustomerModel model) {
        Customer customer = customerRepository.save(orderDataMapper.customerModelToCustomer(model));
        if (customer == null) {
            log.error("Customer could not be created in order database with id: {}", model.getId());
            throw new OrderDomainException("Customer could not be created in order database with id " +
                    model.getId());
        }
        log.info("Customer is created in order database with id: {}", customer.getId());

    }
}
