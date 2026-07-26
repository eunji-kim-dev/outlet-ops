// 재고 도메인이 위치하는 패키지
package com.outletops.inventory;

// 조회 시 매장과 상품을 함께 불러오기 위한 EntityGraph
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

// Inventory 엔티티의 데이터베이스 작업을 담당하는 Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // 동일한 매장과 상품 조합의 재고가 이미 존재하는지 확인
    boolean existsByStore_IdAndProduct_Id(Long storeId, Long productId);

    // 특정 매장의 재고를 상품명 순서로 조회
    // EntityGraph를 사용해 Store와 Product를 한 번의 조회 흐름에서 함께 로딩
    @EntityGraph(attributePaths = {"store", "product"})
    List<Inventory> findByStore_IdOrderByProduct_NameAsc(Long storeId);
}
