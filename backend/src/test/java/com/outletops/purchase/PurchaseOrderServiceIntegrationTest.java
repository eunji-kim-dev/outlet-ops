// 발주 Service 통합 테스트가 위치하는 패키지
package com.outletops.purchase;

// 테스트에 필요한 재고, 상품, 매장 엔티티와 Repository
import com.outletops.inventory.Inventory;
import com.outletops.inventory.InventoryRepository;
import com.outletops.product.Product;
import com.outletops.product.ProductRepository;
import com.outletops.purchase.dto.PurchaseOrderCreateRequest;
import com.outletops.purchase.dto.PurchaseOrderItemCreateRequest;
import com.outletops.purchase.dto.PurchaseOrderResponse;
import com.outletops.store.Store;
import com.outletops.store.StoreRepository;

// 금액과 발주 항목 목록 생성에 필요한 클래스
import java.math.BigDecimal;
import java.util.List;

// JUnit 테스트 생명주기와 테스트 선언
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Spring 통합 테스트와 트랜잭션에 필요한 클래스
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// AssertJ의 읽기 쉬운 검증 메서드
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 실제 Spring Bean과 JPA를 함께 실행하는 통합 테스트
@SpringBootTest

// 운영 MySQL 대신 테스트 전용 H2 설정 사용
@ActiveProfiles("test")

// 각 테스트 종료 후 DB 변경사항을 자동 롤백
@Transactional
class PurchaseOrderServiceIntegrationTest {

    // 테스트 대상 Service
    @Autowired
    private PurchaseOrderService purchaseOrderService;

    // 테스트 데이터 준비와 결과 확인에 사용하는 Repository
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    // 각 테스트에서 사용할 공통 매장, 상품, 재고
    private Store store;
    private Product product;
    private Inventory inventory;

    // 각 테스트 실행 전에 독립적인 기본 데이터 생성
    @BeforeEach
    void setUp() {
        store = storeRepository.save(
                new Store("테스트 매장", "서울특별시 테스트구")
        );

        product = productRepository.save(
                new Product(
                        "TEST-SKU-001",
                        "테스트 상품",
                        "테스트 카테고리",
                        new BigDecimal("20000.00")
                )
        );

        inventory = inventoryRepository.save(
                new Inventory(store, product, 10, 2)
        );
    }

    @Test
    @DisplayName("발주를 등록하면 ORDERED 상태와 총금액이 저장된다")
    void createPurchaseOrder() {
        // 발주 수량 5개, 매입 단가 12,000원인 요청 생성
        PurchaseOrderCreateRequest request = createRequest(5, "12000.00");

        // 발주 등록 실행
        PurchaseOrderResponse response = purchaseOrderService.create(request);

        // 발주 기본 정보와 총금액 검증
        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(
                PurchaseOrderStatus.ORDERED
        );
        assertThat(response.totalAmount())
                .isEqualByComparingTo("60000.00");
        assertThat(response.items()).hasSize(1);
    }

    @Test
    @DisplayName("등록한 발주는 매장별 목록에서 조회된다")
    void findPurchaseOrdersByStore() {
        PurchaseOrderResponse created = purchaseOrderService.create(
                createRequest(5, "12000.00")
        );

        List<PurchaseOrderResponse> orders =
                purchaseOrderService.findByStore(store.getId());

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).id()).isEqualTo(created.id());
        assertThat(orders.get(0).storeId()).isEqualTo(store.getId());
        assertThat(orders.get(0).status())
                .isEqualTo(PurchaseOrderStatus.ORDERED);
    }

    @Test
    @DisplayName("발주를 입고 완료하면 발주 수량만큼 재고가 증가한다")
    void receivePurchaseOrder() {
        // 수량 5개인 발주 등록
        PurchaseOrderResponse created = purchaseOrderService.create(
                createRequest(5, "12000.00")
        );

        // 발주 입고 완료 처리
        PurchaseOrderResponse received = purchaseOrderService.receive(
                created.id()
        );

        // 발주 상태와 입고 시각 검증
        assertThat(received.status()).isEqualTo(
                PurchaseOrderStatus.RECEIVED
        );
        assertThat(received.receivedAt()).isNotNull();

        // 기존 재고 10개에 발주 수량 5개가 더해졌는지 검증
        Inventory updatedInventory = inventoryRepository
                .findById(inventory.getId())
                .orElseThrow();
        assertThat(updatedInventory.getQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("입고 완료된 발주를 다시 입고하면 409 오류가 발생한다")
    void cannotReceiveTwice() {
        PurchaseOrderResponse created = purchaseOrderService.create(
                createRequest(5, "12000.00")
        );
        purchaseOrderService.receive(created.id());

        // 같은 발주를 두 번째로 입고할 때 Conflict 오류 검증
        assertThatThrownBy(
                () -> purchaseOrderService.receive(created.id())
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseException =
                            (ResponseStatusException) exception;
                    assertThat(responseException.getStatusCode())
                            .isEqualTo(HttpStatus.CONFLICT);
                });

        // 중복 입고가 차단되어 재고가 한 번만 증가했는지 검증
        assertThat(inventory.getQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("발주를 취소하면 재고는 증가하지 않는다")
    void cancelPurchaseOrder() {
        PurchaseOrderResponse created = purchaseOrderService.create(
                createRequest(5, "12000.00")
        );

        // 입고 전 발주 취소
        PurchaseOrderResponse cancelled = purchaseOrderService.cancel(
                created.id()
        );

        // 취소 상태와 취소 시각 검증
        assertThat(cancelled.status()).isEqualTo(
                PurchaseOrderStatus.CANCELLED
        );
        assertThat(cancelled.cancelledAt()).isNotNull();

        // 발주 취소 시 기존 재고가 그대로 유지되는지 검증
        assertThat(inventory.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("취소된 발주를 입고하면 409 오류가 발생한다")
    void cannotReceiveCancelledOrder() {
        PurchaseOrderResponse created = purchaseOrderService.create(
                createRequest(5, "12000.00")
        );
        purchaseOrderService.cancel(created.id());

        // 취소된 발주의 입고 처리가 차단되는지 검증
        assertThatThrownBy(
                () -> purchaseOrderService.receive(created.id())
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseException =
                            (ResponseStatusException) exception;
                    assertThat(responseException.getStatusCode())
                            .isEqualTo(HttpStatus.CONFLICT);
                });

        // 취소 후 잘못된 입고 요청에도 재고가 변하지 않는지 검증
        assertThat(inventory.getQuantity()).isEqualTo(10);
    }

    // 반복되는 발주 요청 생성을 한 곳으로 분리
    private PurchaseOrderCreateRequest createRequest(
            int quantity,
            String unitCost
    ) {
        PurchaseOrderItemCreateRequest item =
                new PurchaseOrderItemCreateRequest(
                        product.getId(),
                        quantity,
                        new BigDecimal(unitCost)
                );

        return new PurchaseOrderCreateRequest(
                store.getId(),
                List.of(item)
        );
    }
}
