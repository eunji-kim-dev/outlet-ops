// 재고 도메인이 위치하는 패키지
package com.outletops.inventory;

// 조회 시 매장과 상품을 함께 불러오기 위한 EntityGraph
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Inventory 엔티티의 데이터베이스 작업을 담당하는 Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // 동일한 매장과 상품 조합의 재고가 이미 존재하는지 확인
    boolean existsByStore_IdAndProduct_Id(Long storeId, Long productId);

    // 특정 매장의 재고를 상품명 순서로 조회
    // EntityGraph를 사용해 Store와 Product를 한 번의 조회 흐름에서 함께 로딩
    @EntityGraph(attributePaths = {"store", "product"})
    List<Inventory> findByStore_IdOrderByProduct_NameAsc(Long storeId);

    // 판매 처리 중 동일 재고가 동시에 변경되지 않도록 쓰기 잠금 적용
    @Lock(LockModeType.PESSIMISTIC_WRITE)

    // 매장과 상품에 해당하는 재고를 Store, Product와 함께 조회
    @Query("""
            select inventory
            from Inventory inventory
            join fetch inventory.store
            join fetch inventory.product
            where inventory.store.id = :storeId
              and inventory.product.id = :productId
            """)
    Optional<Inventory> findForUpdate(
            @Param("storeId") Long storeId,
            @Param("productId") Long productId
    );
}
