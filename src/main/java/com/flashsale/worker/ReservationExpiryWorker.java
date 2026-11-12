package com.flashsale.worker;

import com.flashsale.domain.Reservations;
import com.flashsale.repo.ReservationsRepository;
import com.flashsale.service.ReservationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ReservationExpiryWorker {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryWorker.class);

    private final ReservationsRepository reservationsRepository;
    private final ReservationsService reservationsService;

    public ReservationExpiryWorker(ReservationsRepository reservationsRepository,
                                  ReservationsService reservationsService) {
        this.reservationsRepository = reservationsRepository;
        this.reservationsService = reservationsService;
    }

    @Scheduled(fixedDelay = 5000)
    public void expireReservations() {
        try {
            List<Reservations> expired = reservationsRepository.findExpired(Instant.now());
            if (expired.isEmpty()) {
                return;
            }

            log.info("Expiring {} reservations", expired.size());
            for (Reservations reservation : expired) {
                try {
                    reservationsService.expire(reservation.getId());
                    log.debug("Expired reservation: {}", reservation.getId());
                } catch (Exception ex) {
                    log.warn("Failed to expire reservation {}: {}", reservation.getId(), ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error("Error in expiry worker", ex);
        }
    }
}
