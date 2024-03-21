package org.ordering.outbox;

public interface OutboxScheduler {
    void processOutboxMessage();
}
