package org.ordering.order.service.dataaccess.outbox.approval.exception;

public class OrderApprovalOutboxException extends RuntimeException {
    public OrderApprovalOutboxException(String message) {
        super(message);
    }

}
