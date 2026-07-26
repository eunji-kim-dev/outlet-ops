package com.outletops.inventory;

import com.outletops.inventory.dto.InventoryCreateRequest;
import com.outletops.inventory.dto.InventoryQuantityUpdateRequest;
import com.outletops.inventory.dto.InventoryResponse;
import com.outletops.product.Product;
import com.outletops.product.ProductRepository;
import com.outletops.store.Store;
import com.outletops.store.StoreRepository;
import java.math.BigDecimal;
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

// 매장별 재고 생성, 조회, 수정, 중복 방지를 JPA와 함께 검증
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InventoryServiceIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    private Store store;
    private Product product;

    @BeforeEach
    void setUp() {
        store = storeRepository.save(
                new Store("재고 테스트점", "서울 마포구")
        );
        product = productRepository.save(
                new Product(
                        "INVENTORY-SKU-001",
                        "재고 테스트 상품",
                        "신발",
                        new BigDecimal("50000.00")
                )
        );
    }

    @Test
    @DisplayName("재고를 생성하면 해당 매장 목록에서 조회된다")
    void createAndFindByStore() {
        InventoryResponse created = inventoryService.create(
                createRequest()
        );

        List<InventoryResponse> inventories =
                inventoryService.findByStore(store.getId());

        assertThat(created.id()).isNotNull();
        assertThat(inventories).hasSize(1);
        assertThat(inventories.get(0).productId())
                .isEqualTo(product.getId());
        assertThat(inventories.get(0).quantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("같은 매장과 상품의 재고를 중복 생성하면 409 오류가 발생한다")
    void rejectDuplicateInventory() {
        inventoryService.create(createRequest());

        assertThatThrownBy(
                () -> inventoryService.create(createRequest())
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("재고 수량을 0으로 수정할 수 있다")
    void updateQuantityToZero() {
        InventoryResponse created = inventoryService.create(
                createRequest()
        );

        InventoryResponse updated = inventoryService.updateQuantity(
                created.id(),
                new InventoryQuantityUpdateRequest(0)
        );

        assertThat(updated.quantity()).isZero();
        assertThat(updated.lowStock()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 매장의 재고 조회는 404 오류가 발생한다")
    void rejectUnknownStore() {
        assertThatThrownBy(() -> inventoryService.findByStore(999999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private InventoryCreateRequest createRequest() {
        return new InventoryCreateRequest(
                store.getId(),
                product.getId(),
                10,
                2
        );
    }
}
