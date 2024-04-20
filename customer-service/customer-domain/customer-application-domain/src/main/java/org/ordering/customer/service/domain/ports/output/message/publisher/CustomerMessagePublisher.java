package org.ordering.customer.service.domain.ports.output.message.publisher;

import org.ordering.customer.service.domain.event.CustomerCreatedEvent;

public interface CustomerMessagePublisher {
    void publish(CustomerCreatedEvent customerCreatedEvent);

}