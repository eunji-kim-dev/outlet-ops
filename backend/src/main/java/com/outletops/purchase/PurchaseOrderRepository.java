// 발주 도메인이 위치하는 패키지
package com.outletops.purchase;

// 조회 결과와 잠금에 필요한 클래스
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

// Spring Data JPA 조회와 잠금에 필요한 클래스
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// PurchaseOrder 엔티티의 저장과 조회를 담당하는 Repository
public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    // 특정 매장의 발주를 최신순으로 조회
    // 매장, 발주 항목, 상품을 함께 불러와 추가 조회를 방지
    @EntityGraph(attributePaths = {"store", "items", "items.product"})
    List<PurchaseOrder> findByStore_IdOrderByOrderedAtDesc(Long storeId);

    // 입고 또는 취소 처리 중 같은 발주가 동시에 변경되지 않도록 잠금
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select distinct purchaseOrder
            from PurchaseOrder purchaseOrder
            join fetch purchaseOrder.store
            left join fetch purchaseOrder.items item
            left join fetch item.product
            where purchaseOrder.id = :purchaseOrderId
            """)
    Optional<PurchaseOrder> findDetailsForUpdate(
            @Param("purchaseOrderId") Long purchaseOrderId
    );
}
