package com.flashsale;

import com.flashsale.domain.Drops;
import com.flashsale.domain.Stock;
import com.flashsale.repo.DropsRepository;
import com.flashsale.repo.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class SimpleReservationTest {

    @Autowired
    private DropsRepository dropsRepository;

    @Autowired
    private StockRepository stockRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("flashsale")
            .withUsername("flashsale")
            .withPassword("flashsale");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void testBasicSave() {
        Drops drop = new Drops();
        drop.setSku("TEST-SKU");
        drop.setTotalUnits(100);
        drop.setStartedAt(Instant.now());
        drop.setEndsAt(Instant.now().plusSeconds(600));

        dropsRepository.save(drop);

        Stock stock = new Stock();
        stock.setDrop(drop);
        stock.setRemaining(100);
        stock.setAsOf(Instant.now());

        stockRepository.save(stock);

        assertThat(stockRepository.count()).isEqualTo(1);
    }
}
