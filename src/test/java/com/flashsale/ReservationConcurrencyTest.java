package com.flashsale;

import com.flashsale.domain.Drops;
import com.flashsale.domain.Stock;
import com.flashsale.dto.ReservationResponse;
import com.flashsale.exceptions.NoStockRemaining;
import com.flashsale.repo.DropsRepository;
import com.flashsale.repo.StockRepository;
import com.flashsale.service.ReservationsService;
import com.flashsale.service.Sleeper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class ReservationConcurrencyTest {

    @Autowired
    private ReservationsService reservationsService;

    @Autowired
    private DropsRepository dropsRepository;

    @Autowired
    private StockRepository stockRepository;

    private static final int TOTAL_REQUEST = 20_000;
    private static final int AVAILABLE_STOCK = 10_000;

    private UUID testDropId;

    @MockBean
    private Sleeper sleeper;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("flashsale_test")
            .withUsername("postgres")
            .withPassword("postgres");


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setupMocks() throws InterruptedException {
        // Mock sleeper to do nothing (instant retry without delay)
        doNothing().when(sleeper).sleep((int) anyLong());
    }

    @Test
    public void test20KConcurrentRequestsYield10KSuccess() throws InterruptedException {
        setupTestData();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        ConcurrentHashMap<String, Exception> errors = new ConcurrentHashMap<>();

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        for(int i = 0; i < TOTAL_REQUEST; i++) {
            final int userId = i;
            executorService.submit(() -> {
                try {
                    ReservationResponse res = reservationsService.makeReservation(
                            UUID.randomUUID().toString(),
                            "user-" + userId,
                            testDropId,
                            1
                    );
                    successCount.incrementAndGet();
                } catch (NoStockRemaining ex) {
                    failCount.incrementAndGet();
                } catch (Exception ex) {
                    errors.put("user-" + userId, ex);
                }
            });
        }

        executorService.shutdown();
        boolean completed = executorService.awaitTermination(2, TimeUnit.MINUTES);

        System.out.println("Success: " + successCount.get() + ", Fail: " + failCount.get() + ", Errors: " + errors.size());
        if (!errors.isEmpty()) {
            errors.values().stream().findFirst().ifPresent(ex -> {
                System.out.println("Sample error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            });
        }

        Assertions.assertThat(completed).isTrue();
        Assertions.assertThat(successCount.get()).as("Success count should equal available stock").isEqualTo(AVAILABLE_STOCK);
        Assertions.assertThat(failCount.get()).as("Fail count should equal requests - stock").isEqualTo(TOTAL_REQUEST - AVAILABLE_STOCK);
        Assertions.assertThat(errors.size()).as("Should have no errors (all requests succeeded or failed with NoStockRemaining)").isEqualTo(0);

        Drops drop = dropsRepository.findById(testDropId).orElseThrow();
        Stock finalStock = stockRepository.findStockByDrop(drop).orElseThrow();
        Assertions.assertThat(finalStock.getRemaining()).isZero();
    }

    @Transactional
    private void setupTestData() {
        Drops drop = createTestDrop();
        drop = dropsRepository.save(drop);
        testDropId = drop.getId();

        // Reload drop to ensure it's attached to the session
        drop = dropsRepository.findById(testDropId).orElseThrow();

        Stock stock = new Stock();
        stock.setDrop(drop);
        stock.setRemaining(AVAILABLE_STOCK);
        stock.setAsOf(Instant.now());
        stockRepository.save(stock);
    }

    private Drops createTestDrop(){
        Drops drop = new Drops();
        drop.setSku("Test-SKU");
        drop.setTotalUnits(AVAILABLE_STOCK);
        drop.setStartedAt(Instant.now());
        drop.setEndsAt(Instant.now().plusSeconds(1000));

        return  drop;
    }
}
