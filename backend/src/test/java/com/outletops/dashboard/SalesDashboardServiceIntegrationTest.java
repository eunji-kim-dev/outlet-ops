package com.outletops.dashboard;

import com.outletops.dashboard.dto.SalesDashboardResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 일별 집계 결과가 대시보드 핵심 지표와 차트 데이터로 조합되는지 검증
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SalesDashboardServiceIntegrationTest {

    @Autowired
    private SalesDashboardService dashboardService;

    @Autowired
    private DailySalesAggregationService aggregationService;

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
                new Store("대시보드 1호점", "서울 강남구")
        );
        secondStore = storeRepository.save(
                new Store("대시보드 2호점", "서울 마포구")
        );
        product = productRepository.save(
                new Product(
                        "DASHBOARD-SKU-001",
                        "대시보드 상품",
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
    @DisplayName("전체 매장의 핵심 지표와 매장별 매출을 합산한다")
    void getAllStoreDashboard() {
        createSale(firstStore, 2);
        createSale(secondStore, 3);
        aggregationService.aggregate(today);

        SalesDashboardResponse response = dashboardService.getDashboard(
                today,
                today,
                null
        );

        assertThat(response.totalRevenue())
                .isEqualByComparingTo("100000.00");
        assertThat(response.totalQuantity()).isEqualTo(5);
        assertThat(response.transactionCount()).isEqualTo(2);
        assertThat(response.dailySales()).hasSize(1);
        assertThat(response.storeSales()).hasSize(2);
        assertThat(response.storeSales().get(0).storeId())
                .isEqualTo(secondStore.getId());
    }

    @Test
    @DisplayName("매장 ID를 지정하면 해당 매장 매출만 반환한다")
    void filterByStore() {
        createSale(firstStore, 2);
        createSale(secondStore, 3);
        aggregationService.aggregate(today);

        SalesDashboardResponse response = dashboardService.getDashboard(
                today,
                today,
                firstStore.getId()
        );

        assertThat(response.storeId()).isEqualTo(firstStore.getId());
        assertThat(response.totalRevenue())
                .isEqualByComparingTo("40000.00");
        assertThat(response.totalQuantity()).isEqualTo(2);
        assertThat(response.transactionCount()).isEqualTo(1);
        assertThat(response.storeSales()).hasSize(1);
    }

    @Test
    @DisplayName("매출이 없는 날짜도 0원 데이터로 포함한다")
    void fillDateWithoutSalesWithZero() {
        createSale(firstStore, 2);
        aggregationService.aggregate(today);

        SalesDashboardResponse response = dashboardService.getDashboard(
                today.minusDays(1),
                today,
                null
        );

        assertThat(response.dailySales()).hasSize(2);
        assertThat(response.dailySales().get(0).date())
                .isEqualTo(today.minusDays(1));
        assertThat(response.dailySales().get(0).totalRevenue())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.dailySales().get(1).totalRevenue())
                .isEqualByComparingTo("40000.00");
    }

    @Test
    @DisplayName("종료일이 시작일보다 빠르면 400 오류가 발생한다")
    void rejectReversedPeriod() {
        assertThatThrownBy(
                () -> dashboardService.getDashboard(
                        today,
                        today.minusDays(1),
                        null
                )
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("조회 기간이 366일을 초과하면 400 오류가 발생한다")
    void rejectPeriodOverLimit() {
        assertThatThrownBy(
                () -> dashboardService.getDashboard(
                        today.minusDays(367),
                        today,
                        null
                )
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("존재하지 않는 매장을 지정하면 404 오류가 발생한다")
    void rejectUnknownStore() {
        assertThatThrownBy(
                () -> dashboardService.getDashboard(
                        today,
                        today,
                        999999L
                )
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private void createSale(Store store, int quantity) {
        saleService.create(
                new SaleCreateRequest(
                        store.getId(),
                        List.of(
                                new SaleItemCreateRequest(
                                        product.getId(),
                                        quantity
                                )
                        )
                )
        );
    }
}
