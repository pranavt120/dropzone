package com.flashsale.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "drops")
@Data
public class Drops {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String sku;

    private int totalUnits;

    private Instant startedAt;

    private Instant endsAt;

    @OneToMany(mappedBy = "drops",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservations> reservations;

    @OneToOne(mappedBy = "drop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Stock stock;

}