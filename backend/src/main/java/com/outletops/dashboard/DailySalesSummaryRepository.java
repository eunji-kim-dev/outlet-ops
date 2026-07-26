// 매출 대시보드 도메인이 위치하는 패키지
package com.outletops.dashboard;

// 날짜별 집계 조회 결과에 필요한 클래스
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Spring Data JPA의 기본 데이터 접근 기능
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

// 일별 매출 집계 데이터의 저장과 조회를 담당하는 Repository
public interface DailySalesSummaryRepository
        extends JpaRepository<DailySalesSummary, Long> {

    // 같은 매장과 날짜의 기존 집계 결과 조회
    Optional<DailySalesSummary> findByStore_IdAndSalesDate(
            Long storeId,
            LocalDate salesDate
    );

    // 특정 날짜의 모든 매장 집계를 매장명 순서로 조회
    @EntityGraph(attributePaths = "store")
    List<DailySalesSummary> findBySalesDateOrderByStore_NameAsc(
            LocalDate salesDate
    );
}
