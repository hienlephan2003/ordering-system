package org.ordering.order.service.dataaccess.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers", schema = "ordering")
@Entity
public class CustomerEntity{
    @Id
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;

}