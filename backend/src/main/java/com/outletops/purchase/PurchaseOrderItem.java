// 발주 도메인이 위치하는 패키지
package com.outletops.purchase;

// 발주 항목이 참조할 상품 엔티티
import com.outletops.product.Product;

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

// 금액 계산을 위한 클래스
import java.math.BigDecimal;

// Getter와 기본 생성자를 자동 생성하는 Lombok
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 발주 한 건에 포함된 개별 상품을 나타내는 JPA 엔티티
@Entity
@Table(name = "purchase_order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrderItem {

    // purchase_order_items 테이블의 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이 발주 항목이 소속된 발주
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    // 발주한 상품
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 발주 수량
    @Column(nullable = false)
    private int quantity;

    // 발주 당시의 상품 매입 단가
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitCost;

    // 매입 단가와 발주 수량을 곱한 항목별 금액
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal lineAmount;

    // PurchaseOrder.addItem()에서만 발주 항목을 생성
    PurchaseOrderItem(
            PurchaseOrder purchaseOrder,
            Product product,
            int quantity,
            BigDecimal unitCost
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("발주 수량은 0보다 커야 합니다.");
        }

        if (unitCost.signum() <= 0) {
            throw new IllegalArgumentException("매입 단가는 0보다 커야 합니다.");
        }

        this.purchaseOrder = purchaseOrder;
        this.product = product;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.lineAmount = unitCost.multiply(BigDecimal.valueOf(quantity));
    }
}
