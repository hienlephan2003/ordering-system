package org.ordering.order.service.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.ordering.domain.valueobject.OrderApprovalStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantApprovalResponse {
    private UUID id;
    private UUID sagaId;
    private UUID orderId;
    private UUID restaurantId;
    private Instant createdAt;
    private OrderApprovalStatus orderApprovalStatus;
    private List<String> failureMessages;
}