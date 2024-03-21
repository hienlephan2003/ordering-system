package org.ordering.order.service.domain.ports.input.message.listener.customer;

import org.ordering.order.service.domain.dto.message.CustomerModel;
import org.ordering.order.service.domain.entity.Customer;

public interface CustomerMessageListener {
    void customerCreated(CustomerModel customer);
}
