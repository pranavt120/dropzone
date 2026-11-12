package com.flashsale.domain;

import com.flashsale.enums.ReservationStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Data
public class Reservations {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "drop_id")
    private Drops drops;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    private String userId;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private ReservationStatusEnum state;

    private Instant createdAt;

    private Instant expiresAt;

    private Instant paidAt;

}
