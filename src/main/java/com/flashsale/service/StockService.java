package com.flashsale.service;

import com.flashsale.domain.Drops;
import com.flashsale.domain.Stock;
import com.flashsale.dto.StockResponseDto;
import com.flashsale.exceptions.NoStockRemaining;
import com.flashsale.repo.StockRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class StockService {

    private final StockRepository stockRepository;
    private final DropsService dropsService;
    private final Sleeper sleeper;

    public StockService(StockRepository stockRepository,
                        DropsService dropsService,
                        Sleeper sleeper) {
        this.stockRepository = stockRepository;
        this.dropsService = dropsService;
        this.sleeper = sleeper;
    }

    public void reserveStock(UUID dropId, int quantity) {
        int maxRetries = 10;
        int retryCount = 0;
        Drops drop = dropsService.findById(dropId);

        while (retryCount < maxRetries) {
            try {
                doReserveStock(drop, quantity);
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw ex;
                }
                long backoffMs = Math.min(100, (long) Math.pow(2, Math.min(retryCount / 5, 5)) + (long)(Math.random() * 10));
                try {
                    sleeper.sleep((int) backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
    }

    @Transactional
    protected void doReserveStock(Drops drop, int quantity) {
        int rowsUpdated = this.stockRepository.decrementStock(drop.getId(), quantity);
        if(rowsUpdated == 0) {
            throw new NoStockRemaining("Insufficient Stock available!");
        }
    }

    public StockResponseDto getStock(UUID dropId){
        Drops drop = dropsService.findById(dropId);
        Stock stock =  this.stockRepository.findStockByDrop(drop)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with Drop ID: " + dropId));

        return Stock.toDto(stock);
    }

    public void releaseStock(UUID dropId, int quantity) {
        int maxRetries = 100;
        int retryCount = 0;
        Drops drop = dropsService.findById(dropId);

        while (retryCount < maxRetries) {
            try {
                doReleaseStock(drop, quantity);
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw ex;
                }
                long backoffMs = Math.min(100, (long) Math.pow(2, Math.min(retryCount / 5, 5)) + (long)(Math.random() * 10));
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
    }

    @Transactional
    protected void doReleaseStock(Drops drop, int quantity) {
        Stock stock = this.stockRepository.findStockToUpdate(drop)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with Drop: " + drop.getId()));

        stock.setRemaining(stock.getRemaining() + quantity);
        stockRepository.save(stock);
    }
}
