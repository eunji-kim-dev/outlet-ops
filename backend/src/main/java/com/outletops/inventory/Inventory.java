// 재고 도메인이 위치하는 패키지
package com.outletops.inventory;

// 재고가 참조할 상품과 매장 엔티티
import com.outletops.product.Product;
import com.outletops.store.Store;

// JPA 엔티티와 연관관계 매핑에 필요한 클래스
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Getter와 기본 생성자를 자동 생성하는 Lombok
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 재고 정보를 JPA 엔티티로 등록
@Entity

// 같은 매장과 상품 조합이 중복 저장되지 않도록 복합 유니크 제약조건 설정
@Table(
        name = "inventories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventory_store_product",
                columnNames = {"store_id", "product_id"}
        )
)

// 모든 필드의 Getter를 자동 생성
@Getter

// JPA가 사용할 기본 생성자를 protected로 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    // inventories 테이블의 기본 키
    @Id

    // MySQL AUTO_INCREMENT로 ID 자동 생성
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 여러 재고가 하나의 매장을 참조하는 다대일 관계
    // 실제 매장 정보가 필요할 때만 조회하도록 지연 로딩 사용
    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    // inventories 테이블의 store_id 외래 키와 연결
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 여러 재고가 하나의 상품을 참조하는 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    // inventories 테이블의 product_id 외래 키와 연결
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 현재 보유 중인 재고 수량
    @Column(nullable = false)
    private int quantity;

    // 재고 부족 여부를 판단하는 기준 수량
    @Column(nullable = false)
    private int reorderPoint;

    // 새로운 매장별 상품 재고를 생성할 때 사용하는 생성자
    public Inventory(
            Store store,
            Product product,
            int quantity,
            int reorderPoint
    ) {
        validateNonNegative(quantity, "재고 수량");
        validateNonNegative(reorderPoint, "재주문 기준 수량");

        this.store = store;
        this.product = product;
        this.quantity = quantity;
        this.reorderPoint = reorderPoint;
    }

    // 관리 화면에서 현재 재고 수량을 조정
    public void updateQuantity(int quantity) {
        validateNonNegative(quantity, "재고 수량");
        this.quantity = quantity;
    }

    // 판매 등록 시 판매 수량만큼 재고 차감
    public void decrease(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 수량은 0보다 커야 합니다.");
        }

        if (quantity < amount) {
            throw new IllegalStateException("재고 수량이 부족합니다.");
        }

        this.quantity -= amount;
    }

    // 발주 상품 입고 시 입고 수량만큼 재고 증가
    public void increase(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가 수량은 0보다 커야 합니다.");
        }

        this.quantity += amount;
    }

    // 현재 재고가 재주문 기준 수량 이하인지 확인
    public boolean isLowStock() {
        return quantity <= reorderPoint;
    }

    // 수량 관련 값이 음수가 아닌지 공통 검증
    private void validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + "은 0 이상이어야 합니다.");
        }
    }
}
