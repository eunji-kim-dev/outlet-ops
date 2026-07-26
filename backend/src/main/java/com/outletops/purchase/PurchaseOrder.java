// 발주 도메인이 위치하는 패키지
package com.outletops.purchase;

// 발주 상품과 발주 매장이 참조할 엔티티
import com.outletops.product.Product;
import com.outletops.store.Store;

// JPA 엔티티와 연관관계 매핑에 필요한 클래스
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

// 금액, 시각, 발주 항목 목록에 필요한 클래스
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Getter와 기본 생성자를 자동 생성하는 Lombok
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 발주 한 건을 나타내는 JPA 엔티티
@Entity
@Table(name = "purchase_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrder {

    // purchase_orders 테이블의 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 발주 상품을 입고할 매장
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 발주를 등록한 시각
    @Column(nullable = false)
    private LocalDateTime orderedAt;

    // 입고를 완료한 시각
    @Column
    private LocalDateTime receivedAt;

    // 발주를 취소한 시각
    @Column
    private LocalDateTime cancelledAt;

    // 발주의 현재 상태를 문자열로 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseOrderStatus status;

    // 발주 항목 금액을 모두 합한 총 발주 금액
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // 발주에 포함된 여러 상품 항목
    // 발주 저장 시 발주 항목도 함께 저장
    @OneToMany(
            mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<PurchaseOrderItem> items = new ArrayList<>();

    // 새로운 발주를 생성할 때 매장, 등록 시각, 초기 상태 설정
    public PurchaseOrder(Store store) {
        this.store = store;
        this.orderedAt = LocalDateTime.now();
        this.status = PurchaseOrderStatus.ORDERED;
    }

    // 발주 상품을 추가하고 총 발주 금액 계산
    public void addItem(
            Product product,
            int quantity,
            BigDecimal unitCost
    ) {
        ensureOrdered();

        PurchaseOrderItem item = new PurchaseOrderItem(
                this,
                product,
                quantity,
                unitCost
        );

        items.add(item);
        totalAmount = totalAmount.add(item.getLineAmount());
    }

    // 발주를 입고 완료 상태로 변경
    public void receive() {
        ensureOrdered();
        this.status = PurchaseOrderStatus.RECEIVED;
        this.receivedAt = LocalDateTime.now();
    }

    // 입고 전 발주를 취소 상태로 변경
    public void cancel() {
        ensureOrdered();
        this.status = PurchaseOrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    // 외부에서 발주 항목 목록을 직접 변경하지 못하도록 읽기 전용 목록 반환
    public List<PurchaseOrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    // ORDERED 상태에서만 입고·취소·항목 추가를 허용
    private void ensureOrdered() {
        if (status != PurchaseOrderStatus.ORDERED) {
            throw new IllegalStateException(
                    "처리 완료된 발주는 변경할 수 없습니다."
            );
        }
    }
}
