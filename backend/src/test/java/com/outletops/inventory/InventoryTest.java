package com.outletops.inventory;

import com.outletops.product.Product;
import com.outletops.store.Store;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Spring 없이 Inventory 자체의 수량 불변식을 빠르게 검증하는 단위 테스트
class InventoryTest {

    private Store store;
    private Product product;

    @BeforeEach
    void setUp() {
        store = new Store("단위 테스트점", "서울");
        product = new Product(
                "UNIT-SKU-001",
                "단위 테스트 상품",
                "상의",
                new BigDecimal("10000.00")
        );
    }

    @Test
    @DisplayName("초기 재고 수량은 음수일 수 없다")
    void rejectNegativeInitialQuantity() {
        assertThatThrownBy(() -> new Inventory(store, product, -1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0 이상");
    }

    @Test
    @DisplayName("재고 수량을 음수로 수정할 수 없다")
    void rejectNegativeUpdateQuantity() {
        Inventory inventory = new Inventory(store, product, 10, 2);

        assertThatThrownBy(() -> inventory.updateQuantity(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(inventory.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("현재 재고보다 많이 차감하면 수량을 유지한다")
    void rejectDecreaseBelowZero() {
        Inventory inventory = new Inventory(store, product, 10, 2);

        assertThatThrownBy(() -> inventory.decrease(11))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("부족");
        assertThat(inventory.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("재고 증가와 차감이 정상 반영된다")
    void increaseAndDecrease() {
        Inventory inventory = new Inventory(store, product, 10, 2);

        inventory.increase(5);
        inventory.decrease(3);

        assertThat(inventory.getQuantity()).isEqualTo(12);
    }

    @Test
    @DisplayName("재고가 재주문 기준 이하이면 부족 상태다")
    void detectLowStock() {
        Inventory inventory = new Inventory(store, product, 2, 2);

        assertThat(inventory.isLowStock()).isTrue();
    }
}
