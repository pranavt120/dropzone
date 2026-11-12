package com.flashsale.api;

import com.flashsale.domain.Stock;
import com.flashsale.dto.StockResponseDto;
import com.flashsale.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class StockApi {

    private final StockService stockService;

    public StockApi(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/drops/{dropId}/stock")
    public ResponseEntity<StockResponseDto> getStock(@PathVariable String dropId) {
        return ResponseEntity.ok(this.stockService.getStock(UUID.fromString(dropId)));
    }
}
