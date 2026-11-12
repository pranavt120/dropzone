package com.flashsale.repo;

import com.flashsale.domain.Drops;
import com.flashsale.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s where s.drop = :drop")
    Optional<Stock> findStockToUpdate(@Param("drop") Drops drop);

    Optional<Stock> findStockByDrop(Drops drop);

    @Modifying
    @Query(value = "UPDATE stock SET remaining = remaining - :quantity WHERE drop_id = :dropId AND remaining >= :quantity", nativeQuery = true)
    int decrementStock(@Param("dropId") UUID dropId, @Param("quantity") int quantity);
}
