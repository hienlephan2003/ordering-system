package org.ordering.customer.service.domain.create;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreateCustomerCommand {
    @NotNull
    private final String username;
    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;
}
