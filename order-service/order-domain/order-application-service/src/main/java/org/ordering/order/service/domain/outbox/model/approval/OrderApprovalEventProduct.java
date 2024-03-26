package org.ordering.order.service.domain.outbox.model.approval;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderApprovalEventProduct {
    @JsonProperty
    private String Id;
    @JsonProperty
    private Integer quantity;
}
