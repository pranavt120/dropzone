package com.flashsale.repo;

import com.flashsale.domain.Reservations;
import com.flashsale.enums.ReservationStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationsRepository extends JpaRepository<Reservations, UUID> {

    Optional<Reservations> findByIdempotencyKey(String key);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM Reservations r " +
            "WHERE r.userId = :userId AND r.drops.id = :dropId " +
            "AND r.state NOT IN ('CANCELLED', 'EXPIRED')")
    int sumUserReservedQuantity(@Param("userId") String userId, @Param("dropId") UUID dropId);

    @Query("SELECT r FROM Reservations r " +
            "WHERE r.state = 'RESERVED' AND r.expiresAt < :now")
    List<Reservations> findExpired(@Param("now") Instant now);
}
