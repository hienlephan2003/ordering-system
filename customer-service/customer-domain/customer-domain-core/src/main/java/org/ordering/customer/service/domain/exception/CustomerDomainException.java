package org.ordering.customer.service.domain.exception;


import org.ordering.domain.exception.DomainException;

public class CustomerDomainException extends DomainException {

    public CustomerDomainException(String message) {
        super(message);
    }
}
