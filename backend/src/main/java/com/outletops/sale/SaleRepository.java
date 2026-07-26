// 판매 도메인이 위치하는 패키지
package com.outletops.sale;

// 일별 매출 집계 결과를 받는 Projection
import com.outletops.sale.projection.DailySalesAggregate;

// 집계 대상 시간 범위와 결과 목록에 필요한 클래스
import java.time.LocalDateTime;
import java.util.List;

// Spring Data JPA의 기본 데이터 접근 기능
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Sale 엔티티의 저장과 조회를 담당하는 Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // 지정한 시간 범위의 판매 데이터를 매장별로 집계
    @Query("""
            select sale.store.id as storeId,
                   sum(item.lineAmount) as totalRevenue,
                   sum(item.quantity) as totalQuantity,
                   count(distinct sale.id) as transactionCount
            from Sale sale
            join sale.items item
            where sale.soldAt >= :startAt
              and sale.soldAt < :endAt
            group by sale.store.id
            """)
    List<DailySalesAggregate> aggregateDailySales(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
