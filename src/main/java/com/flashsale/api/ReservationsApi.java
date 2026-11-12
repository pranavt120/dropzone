package com.flashsale.api;

import com.flashsale.domain.Reservations;
import com.flashsale.dto.ReservationRequest;
import com.flashsale.dto.ReservationResponse;
import com.flashsale.service.ReservationsService;
import com.flashsale.service.StockService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController()
public class ReservationsApi {

    private final ReservationsService reservationsService;

    private static final Logger log = LoggerFactory.getLogger(ReservationsApi.class);

    public ReservationsApi(ReservationsService reservationsService) {
        this.reservationsService = reservationsService;
    }


    @PostMapping("/drops/{dropId}/reservations")
    public ResponseEntity<?> makeReservations(@PathVariable("dropId") String dropId,
                                              @RequestBody  @Valid ReservationRequest reservationRequest,
                                              HttpServletRequest request) {
        log.info("REST request to make reservations for dropId: {}", dropId);

        String idempotencyKey = request.getAttribute("idempotency-key").toString();
        ReservationResponse reservations =  this.reservationsService.makeReservation(
                idempotencyKey,
                reservationRequest.userId(),
                UUID.fromString(dropId),
                reservationRequest.quantity()
        );
        return ResponseEntity.
                status(HttpStatusCode.valueOf(201))
                .body(reservations);
    }
}
