// 판매 도메인이 위치하는 패키지
package com.outletops.sale;

// 판매 상품과 판매 매장이 참조할 엔티티
import com.outletops.product.Product;
import com.outletops.store.Store;

// JPA 엔티티와 연관관계 매핑에 필요한 클래스
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

// 금액, 판매 시각, 판매 항목 목록에 필요한 클래스
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Getter와 기본 생성자를 자동 생성하는 Lombok
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판매 한 건을 나타내는 JPA 엔티티
@Entity
@Table(name = "sales")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sale {

    // sales 테이블의 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 판매가 발생한 매장
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 실제 판매가 등록된 시각
    @Column(nullable = false)
    private LocalDateTime soldAt;

    // 판매 항목 금액을 모두 합한 총매출
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // 판매 한 건에 포함된 여러 판매 항목
    // Sale 저장 시 SaleItem도 함께 저장
    @OneToMany(
            mappedBy = "sale",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<SaleItem> items = new ArrayList<>();

    // 새로운 판매를 생성할 때 매장과 현재 시각 설정
    public Sale(Store store) {
        this.store = store;
        this.soldAt = LocalDateTime.now();
    }

    // 판매 상품을 추가하고 총 판매 금액을 함께 계산
    public void addItem(
            Product product,
            int quantity,
            BigDecimal unitPrice
    ) {
        SaleItem item = new SaleItem(
                this,
                product,
                quantity,
                unitPrice
        );

        items.add(item);
        totalAmount = totalAmount.add(item.getLineAmount());
    }

    // 외부에서 판매 항목 목록을 직접 변경하지 못하도록 읽기 전용 목록 반환
    public List<SaleItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
