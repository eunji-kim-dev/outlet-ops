package com.outletops.sale;

import com.outletops.dashboard.DailySalesSummaryRepository;
import com.outletops.inventory.Inventory;
import com.outletops.inventory.InventoryRepository;
import com.outletops.product.Product;
import com.outletops.product.ProductRepository;
import com.outletops.purchase.PurchaseOrderRepository;
import com.outletops.sale.dto.SaleCreateRequest;
import com.outletops.sale.dto.SaleItemCreateRequest;
import com.outletops.sale.dto.SaleResponse;
import com.outletops.store.Store;
import com.outletops.store.StoreRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 판매 저장과 재고 차감의 실제 트랜잭션 및 롤백을 검증
@SpringBootTest
@ActiveProfiles("test")
class SaleServiceIntegrationTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    // 테스트 클래스 간 데이터 충돌을 막기 위한 정리용 Repository
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private DailySalesSummaryRepository summaryRepository;

    private Store store;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        clearDatabase();

        store = storeRepository.save(
                new Store("판매 테스트점", "서울 영등포구")
        );
        product = productRepository.save(
                new Product(
                        "SALE-SKU-001",
                        "판매 테스트 상품",
                        "상의",
                        new BigDecimal("20000.00")
                )
        );
        inventory = inventoryRepository.save(
                new Inventory(store, product, 10, 2)
        );
    }

    // 트랜잭션 롤백 테스트가 다른 테스트 클래스에 데이터를 남기지 않도록 정리
    @AfterEach
    void tearDown() {
        clearDatabase();
    }

    @Test
    @DisplayName("판매를 등록하면 판매가 저장되고 재고가 차감된다")
    void createSaleAndDecreaseInventory() {
        SaleResponse response = saleService.create(
                request(new SaleItemCreateRequest(product.getId(), 3))
        );

        Inventory updated = inventoryRepository
                .findById(inventory.getId())
                .orElseThrow();

        assertThat(response.id()).isNotNull();
        assertThat(response.totalAmount())
                .isEqualByComparingTo("60000.00");
        assertThat(saleRepository.count()).isEqualTo(1);
        assertThat(updated.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고보다 많이 판매하면 판매를 저장하지 않고 재고를 유지한다")
    void rollbackWhenInventoryIsInsufficient() {
        assertThatThrownBy(
                () -> saleService.create(
                        request(
                                new SaleItemCreateRequest(
                                        product.getId(),
                                        11
                                )
                        )
                )
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.CONFLICT));

        Inventory unchanged = inventoryRepository
                .findById(inventory.getId())
                .orElseThrow();

        assertThat(unchanged.getQuantity()).isEqualTo(10);
        assertThat(saleRepository.count()).isZero();
    }

    @Test
    @DisplayName("두 번째 상품 재고가 부족하면 첫 번째 상품 차감도 롤백한다")
    void rollbackAllItemsWhenLaterItemFails() {
        Product secondProduct = productRepository.save(
                new Product(
                        "SALE-SKU-002",
                        "재고 부족 상품",
                        "하의",
                        new BigDecimal("30000.00")
                )
        );
        Inventory secondInventory = inventoryRepository.save(
                new Inventory(store, secondProduct, 1, 1)
        );

        SaleCreateRequest request = request(
                new SaleItemCreateRequest(product.getId(), 2),
                new SaleItemCreateRequest(secondProduct.getId(), 2)
        );

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.CONFLICT));

        Inventory firstAfterRollback = inventoryRepository
                .findById(inventory.getId())
                .orElseThrow();
        Inventory secondAfterRollback = inventoryRepository
                .findById(secondInventory.getId())
                .orElseThrow();

        assertThat(firstAfterRollback.getQuantity()).isEqualTo(10);
        assertThat(secondAfterRollback.getQuantity()).isEqualTo(1);
        assertThat(saleRepository.count()).isZero();
    }

    @Test
    @DisplayName("한 판매 요청에 같은 상품이 중복되면 400 오류가 발생한다")
    void rejectDuplicateProductItems() {
        SaleCreateRequest request = request(
                new SaleItemCreateRequest(product.getId(), 1),
                new SaleItemCreateRequest(product.getId(), 2)
        );

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThat(inventoryRepository.findById(inventory.getId())
                .orElseThrow()
                .getQuantity()).isEqualTo(10);
        assertThat(saleRepository.count()).isZero();
    }

    @Test
    @DisplayName("매장에 등록되지 않은 상품 재고로 판매하면 404 오류가 발생한다")
    void rejectProductWithoutInventory() {
        Product noInventoryProduct = productRepository.save(
                new Product(
                        "SALE-SKU-003",
                        "재고 미등록 상품",
                        "잡화",
                        new BigDecimal("10000.00")
                )
        );

        assertThatThrownBy(
                () -> saleService.create(
                        request(
                                new SaleItemCreateRequest(
                                        noInventoryProduct.getId(),
                                        1
                                )
                        )
                )
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.NOT_FOUND));

        assertThat(saleRepository.count()).isZero();
    }

    private SaleCreateRequest request(SaleItemCreateRequest... items) {
        return new SaleCreateRequest(store.getId(), List.of(items));
    }

    // 외래 키 순서를 고려해 자식 테이블부터 정리
    private void clearDatabase() {
        summaryRepository.deleteAll();
        purchaseOrderRepository.deleteAll();
        saleRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        storeRepository.deleteAll();
    }
}
