package org.ordering.order.service.domain.outbox.model.approval;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class OrderApprovalEventProduct {
    @JsonProperty
    private String Id;
    @JsonProperty
    private Integer quantity;
}
