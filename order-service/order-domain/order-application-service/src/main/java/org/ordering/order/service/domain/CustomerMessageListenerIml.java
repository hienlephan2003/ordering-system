package org.ordering.order.service.domain;

import org.ordering.order.service.domain.dto.message.CustomerModel;
import org.ordering.order.service.domain.entity.Customer;
import org.ordering.order.service.domain.mapper.OrderDataMapper;
import org.ordering.order.service.domain.ports.input.message.listener.customer.CustomerMessageListener;
import org.ordering.order.service.domain.repository.CustomerRepository;

public class CustomerMessageListenerIml implements CustomerMessageListener {
    private final CustomerRepository customerRepository;
    private final OrderDataMapper orderDataMapper;

    public CustomerMessageListenerIml(CustomerRepository customerRepository, OrderDataMapper orderDataMapper) {
        this.customerRepository = customerRepository;
        this.orderDataMapper = orderDataMapper;
    }

    @Override
    public void customerCreated(CustomerModel customer) {
        customerRepository.save(orderDataMapper.customerModelToCustomer(customer));
    }
}
