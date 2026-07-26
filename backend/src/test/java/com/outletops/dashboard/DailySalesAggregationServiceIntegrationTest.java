package com.outletops.dashboard;

import com.outletops.dashboard.dto.DailySalesSummaryResponse;
import com.outletops.inventory.Inventory;
import com.outletops.inventory.InventoryRepository;
import com.outletops.product.Product;
import com.outletops.product.ProductRepository;
import com.outletops.sale.SaleService;
import com.outletops.sale.dto.SaleCreateRequest;
import com.outletops.sale.dto.SaleItemCreateRequest;
import com.outletops.store.Store;
import com.outletops.store.StoreRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// 판매 원본 데이터가 일별·매장별 집계로 정확히 변환되는지 검증
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DailySalesAggregationServiceIntegrationTest {

    @Autowired
    private DailySalesAggregationService aggregationService;

    @Autowired
    private DailySalesSummaryRepository summaryRepository;

    @Autowired
    private SaleService saleService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private Store firstStore;
    private Store secondStore;
    private Product product;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();

        firstStore = storeRepository.save(
                new Store("집계 테스트 1호점", "서울 강남구")
        );
        secondStore = storeRepository.save(
                new Store("집계 테스트 2호점", "서울 마포구")
        );
        product = productRepository.save(
                new Product(
                        "SUMMARY-SKU-001",
                        "집계 테스트 상품",
                        "상의",
                        new BigDecimal("20000.00")
                )
        );

        inventoryRepository.save(
                new Inventory(firstStore, product, 100, 10)
        );
        inventoryRepository.save(
                new Inventory(secondStore, product, 100, 10)
        );
    }

    @Test
    @DisplayName("판매 데이터를 총매출, 수량, 거래 건수로 정확히 집계한다")
    void aggregateDailyTotals() {
        createSale(firstStore, 2);
        createSale(firstStore, 1);

        List<DailySalesSummaryResponse> responses =
                aggregationService.aggregate(today);

        assertThat(responses).hasSize(1);
        DailySalesSummaryResponse summary = responses.get(0);
        assertThat(summary.storeId()).isEqualTo(firstStore.getId());
        assertThat(summary.totalRevenue())
                .isEqualByComparingTo("60000.00");
        assertThat(summary.totalQuantity()).isEqualTo(3);
        assertThat(summary.transactionCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("여러 매장의 판매는 매장별로 분리해 집계한다")
    void aggregateByStore() {
        createSale(firstStore, 2);
        createSale(secondStore, 3);

        List<DailySalesSummaryResponse> responses =
                aggregationService.aggregate(today);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .filteredOn(
                        response -> response.storeId()
                                .equals(firstStore.getId())
                )
                .singleElement()
                .extracting(DailySalesSummaryResponse::totalQuantity)
                .isEqualTo(2L);
        assertThat(responses)
                .filteredOn(
                        response -> response.storeId()
                                .equals(secondStore.getId())
                )
                .singleElement()
                .extracting(DailySalesSummaryResponse::totalQuantity)
                .isEqualTo(3L);
    }

    @Test
    @DisplayName("같은 날짜를 다시 집계해도 중복 행이 생성되지 않는다")
    void aggregateIdempotently() {
        createSale(firstStore, 2);

        aggregationService.aggregate(today);
        aggregationService.aggregate(today);

        assertThat(summaryRepository.count()).isEqualTo(1);
        DailySalesSummaryResponse summary =
                aggregationService.findByDate(today).get(0);
        assertThat(summary.totalRevenue())
                .isEqualByComparingTo("40000.00");
        assertThat(summary.totalQuantity()).isEqualTo(2);
        assertThat(summary.transactionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("판매가 없는 날짜를 집계하면 빈 목록을 반환한다")
    void returnEmptyForDateWithoutSales() {
        List<DailySalesSummaryResponse> responses =
                aggregationService.aggregate(today.minusDays(1));

        assertThat(responses).isEmpty();
    }

    private void createSale(Store store, int quantity) {
        SaleItemCreateRequest item =
                new SaleItemCreateRequest(product.getId(), quantity);
        saleService.create(
                new SaleCreateRequest(store.getId(), List.of(item))
        );
    }
}
