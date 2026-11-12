package com.flashsale.domain;

import com.flashsale.dto.StockResponseDto;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock")
@Data
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "drop_id", referencedColumnName = "id")
    private Drops drop;

    private int remaining;

    private Instant asOf;


    public static StockResponseDto toDto(Stock stock){
        return new StockResponseDto(
                stock.getDrop().getId().toString(),
                stock.getRemaining()
        );
    }
}
