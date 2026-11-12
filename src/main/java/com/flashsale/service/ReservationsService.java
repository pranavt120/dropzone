package com.flashsale.service;

import com.flashsale.domain.Drops;
import com.flashsale.domain.Reservations;
import com.flashsale.domain.Stock;
import com.flashsale.dto.ReservationResponse;
import com.flashsale.enums.ReservationStatusEnum;
import com.flashsale.exceptions.QuantityLimitExceeded;
import com.flashsale.repo.ReservationsRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationsService {

    private final ReservationsRepository reservationsRepository;
    private final StockService stockService;
    private final DropsService dropsService;

    private static final int MAX_ALLOWED_ITEMS = 2;

    public ReservationsService(ReservationsRepository reservationsRepository,
                               StockService stockService, DropsService dropsService) {
        this.reservationsRepository = reservationsRepository;
        this.stockService = stockService;
        this.dropsService = dropsService;
    }

    @Transactional
    public ReservationResponse makeReservation(String idempotencyKey,
                                               String userId, UUID dropId ,
                                               int quantity) throws QuantityLimitExceeded {
        // Step 1: Check if idempotency key already exists
        Optional<Reservations> reserved = this.reservationsRepository.findByIdempotencyKey(idempotencyKey);
        if (reserved.isPresent()) {
            return new ReservationResponse(
                    reserved.get().getId().toString()
            );
        }

        // Step 2: Check per-user limit (max 2 units per user per drop)
        int userTotalQty = this.reservationsRepository.sumUserReservedQuantity(
                userId,
                dropId
        );

        if (userTotalQty + quantity > MAX_ALLOWED_ITEMS) {
            throw new QuantityLimitExceeded(
                    "User already has " + userTotalQty + " units reserved. " +
                    "Cannot reserve " + quantity + " more (limit: " + MAX_ALLOWED_ITEMS + ")"
            );
        }

        // Step 3-5: Reserve stock (lock, check, decrement)
        this.stockService.reserveStock(dropId, quantity);

        Drops drop = dropsService.findById(dropId);

        Reservations reservations = new Reservations();
        reservations.setIdempotencyKey(idempotencyKey);
        reservations.setUserId(userId);
        reservations.setQuantity(quantity);
        reservations.setDrops(drop);
        reservations.setCreatedAt(Instant.now());
        reservations.setState(ReservationStatusEnum.RESERVED);
        reservations.setExpiresAt(Instant.now().plusSeconds(600));

        // Step 6: Insert reservation
        Reservations saved =  this.saveReservations(reservations);
        return new ReservationResponse(saved.getId().toString());
    }

    private Reservations saveReservations(Reservations reservations) {
        try {
            return this.reservationsRepository.save(reservations);
        }catch (DataIntegrityViolationException ex){
            Optional<Reservations> res = this.reservationsRepository.findByIdempotencyKey(reservations.getIdempotencyKey());
            if (res.isPresent()) {
                return res.get();
            }
            throw ex;
        }
    }

    @Transactional
    public void expire(UUID reservationId) {
        Reservations reservation = reservationsRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        if (!reservation.getState().equals(ReservationStatusEnum.RESERVED)) {
            return;
        }

        stockService.releaseStock(reservation.getDrops().getId(), reservation.getQuantity());

        reservation.setState(ReservationStatusEnum.EXPIRED);
        reservationsRepository.save(reservation);
    }
}
