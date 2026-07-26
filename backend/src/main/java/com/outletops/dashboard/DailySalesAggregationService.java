// 매출 대시보드 도메인이 위치하는 패키지
package com.outletops.dashboard;

// 집계 응답 DTO와 판매 집계 Projection
import com.outletops.dashboard.dto.DailySalesSummaryResponse;
import com.outletops.sale.SaleRepository;
import com.outletops.sale.projection.DailySalesAggregate;

// 매장 조회에 사용하는 엔티티와 Repository
import com.outletops.store.Store;
import com.outletops.store.StoreRepository;

// 집계 날짜·시간과 결과 목록에 필요한 클래스
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 생성자 주입과 Service, 트랜잭션에 필요한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 판매 원본 데이터를 일별 매출 집계로 변환하는 Service
@Service
@RequiredArgsConstructor
public class DailySalesAggregationService {

    // 판매 원본, 집계 결과, 매장 데이터에 접근하는 Repository
    private final SaleRepository saleRepository;
    private final DailySalesSummaryRepository summaryRepository;
    private final StoreRepository storeRepository;

    // 지정한 날짜의 판매를 매장별로 집계해 저장
    @Transactional
    public List<DailySalesSummaryResponse> aggregate(LocalDate salesDate) {

        // 집계 날짜의 시작 시각과 다음 날 시작 시각 계산
        LocalDateTime startAt = salesDate.atStartOfDay();
        LocalDateTime endAt = salesDate.plusDays(1).atStartOfDay();

        // DB에서 매장별 총매출, 판매 수량, 판매 건수 집계
        List<DailySalesAggregate> aggregates =
                saleRepository.aggregateDailySales(startAt, endAt);

        // 각 매장별 집계 결과를 생성하거나 기존 결과 갱신
        for (DailySalesAggregate aggregate : aggregates) {
            Store store = storeRepository.findById(aggregate.getStoreId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "집계 대상 매장을 찾을 수 없습니다."
                    ));

            DailySalesSummary summary = summaryRepository
                    .findByStore_IdAndSalesDate(
                            aggregate.getStoreId(),
                            salesDate
                    )
                    .orElseGet(() -> new DailySalesSummary(
                            store,
                            salesDate,
                            aggregate.getTotalRevenue(),
                            aggregate.getTotalQuantity(),
                            aggregate.getTransactionCount()
                    ));

            // 기존 집계인 경우 최신 원본 판매 데이터로 값 갱신
            summary.update(
                    aggregate.getTotalRevenue(),
                    aggregate.getTotalQuantity(),
                    aggregate.getTransactionCount()
            );

            summaryRepository.save(summary);
        }

        // 저장된 해당 날짜의 전체 매장 집계 결과 반환
        return findByDate(salesDate);
    }

    // 지정한 날짜의 집계 결과 조회
    @Transactional(readOnly = true)
    public List<DailySalesSummaryResponse> findByDate(LocalDate salesDate) {
        return summaryRepository
                .findBySalesDateOrderByStore_NameAsc(salesDate)
                .stream()
                .map(DailySalesSummaryResponse::from)
                .toList();
    }
}
