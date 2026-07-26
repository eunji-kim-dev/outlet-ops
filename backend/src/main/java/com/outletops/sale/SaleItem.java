// 판매 도메인이 위치하는 패키지
package com.outletops.sale;

// 판매 항목이 참조할 상품 엔티티
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

// 판매 한 건에 포함된 개별 상품을 나타내는 JPA 엔티티
@Entity
@Table(name = "sale_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SaleItem {

    // sale_items 테이블의 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이 판매 항목이 소속된 판매
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    // 판매된 상품
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 판매 수량
    @Column(nullable = false)
    private int quantity;

    // 판매 당시의 상품 단가
    // 이후 상품 가격이 변경돼도 과거 매출을 유지하기 위해 별도 저장
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    // 단가와 수량을 곱한 항목별 판매 금액
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal lineAmount;

    // Sale.addItem()에서만 판매 항목을 생성
    SaleItem(
            Sale sale,
            Product product,
            int quantity,
            BigDecimal unitPrice
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("판매 수량은 0보다 커야 합니다.");
        }

        this.sale = sale;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
