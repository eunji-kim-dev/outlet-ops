// 매출 대시보드 도메인이 위치하는 패키지
package com.outletops.dashboard;

// 대시보드 최종 응답과 차트 응답 DTO
import com.outletops.dashboard.dto.DashboardDailySalesResponse;
import com.outletops.dashboard.dto.DashboardStoreSalesResponse;
import com.outletops.dashboard.dto.SalesDashboardResponse;

// 매장 존재 여부 확인에 사용하는 Repository
import com.outletops.store.StoreRepository;

// 금액, 날짜, 정렬, 그룹화에 필요한 클래스
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 생성자 주입과 Service, 읽기 전용 트랜잭션에 필요한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 대시보드에 필요한 일별·매장별 매출 데이터를 조합하는 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesDashboardService {

    // 일별 집계 데이터와 매장 데이터에 접근하는 Repository
    private final DailySalesSummaryRepository summaryRepository;
    private final StoreRepository storeRepository;

    // 조회 기간과 선택 매장에 맞는 대시보드 데이터 생성
    public SalesDashboardResponse getDashboard(
            LocalDate startDate,
            LocalDate endDate,
            Long storeId
    ) {
        validatePeriod(startDate, endDate);
        validateStore(storeId);

        // 매장 선택 여부에 따라 적절한 집계 결과 조회
        List<DailySalesSummary> summaries = storeId == null
                ? summaryRepository
                .findBySalesDateBetweenOrderBySalesDateAsc(
                        startDate,
                        endDate
                )
                : summaryRepository
                .findByStore_IdAndSalesDateBetweenOrderBySalesDateAsc(
                        storeId,
                        startDate,
                        endDate
                );

        // 전체 핵심 지표 계산
        SalesAccumulator totals = new SalesAccumulator();
        summaries.forEach(totals::add);

        return new SalesDashboardResponse(
                startDate,
                endDate,
                storeId,
                totals.totalRevenue,
                totals.totalQuantity,
                totals.transactionCount,
                buildDailySales(startDate, endDate, summaries),
                buildStoreSales(summaries)
        );
    }

    // 조회 기간의 모든 날짜를 생성하고 매출 없는 날짜는 0으로 유지
    private List<DashboardDailySalesResponse> buildDailySales(
            LocalDate startDate,
            LocalDate endDate,
            List<DailySalesSummary> summaries
    ) {
        Map<LocalDate, SalesAccumulator> dailyMap = new LinkedHashMap<>();

        for (
                LocalDate date = startDate;
                !date.isAfter(endDate);
                date = date.plusDays(1)
        ) {
            dailyMap.put(date, new SalesAccumulator());
        }

        for (DailySalesSummary summary : summaries) {
            dailyMap.get(summary.getSalesDate()).add(summary);
        }

        return dailyMap.entrySet().stream()
                .map(entry -> new DashboardDailySalesResponse(
                        entry.getKey(),
                        entry.getValue().totalRevenue,
                        entry.getValue().totalQuantity,
                        entry.getValue().transactionCount
                ))
                .toList();
    }

    // 매장별 집계 결과를 합산해 총매출 내림차순으로 정렬
    private List<DashboardStoreSalesResponse> buildStoreSales(
            List<DailySalesSummary> summaries
    ) {
        Map<Long, StoreAccumulator> storeMap = new LinkedHashMap<>();

        for (DailySalesSummary summary : summaries) {
            StoreAccumulator accumulator = storeMap.computeIfAbsent(
                    summary.getStore().getId(),
                    ignored -> new StoreAccumulator(
                            summary.getStore().getId(),
                            summary.getStore().getName()
                    )
            );
            accumulator.add(summary);
        }

        List<DashboardStoreSalesResponse> responses = new ArrayList<>();
        storeMap.values().forEach(
                accumulator -> responses.add(accumulator.toResponse())
        );
        responses.sort(
                Comparator.comparing(
                        DashboardStoreSalesResponse::totalRevenue
                ).reversed()
        );
        return responses;
    }

    // 시작일과 종료일 순서 및 최대 조회 기간 검증
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "종료일은 시작일보다 빠를 수 없습니다."
            );
        }

        if (ChronoUnit.DAYS.between(startDate, endDate) > 366) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "대시보드 조회 기간은 최대 366일입니다."
            );
        }
    }

    // 매장이 지정된 경우 실제 존재하는 매장인지 확인
    private void validateStore(Long storeId) {
        if (storeId != null && !storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "매장을 찾을 수 없습니다."
            );
        }
    }

    // 총매출, 판매수량, 거래건수를 누적하는 단순 내부 클래스
    private static final class SalesAccumulator {

        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private long totalQuantity;
        private long transactionCount;

        private void add(DailySalesSummary summary) {
            totalRevenue = totalRevenue.add(summary.getTotalRevenue());
            totalQuantity += summary.getTotalQuantity();
            transactionCount += summary.getTransactionCount();
        }
    }

    // 상속 없이 매장 정보와 매출 누계를 함께 관리
    private static final class StoreAccumulator {

        private final Long storeId;
        private final String storeName;
        private final SalesAccumulator sales = new SalesAccumulator();

        private StoreAccumulator(Long storeId, String storeName) {
            this.storeId = storeId;
            this.storeName = storeName;
        }

        private void add(DailySalesSummary summary) {
            sales.add(summary);
        }

        private DashboardStoreSalesResponse toResponse() {
            return new DashboardStoreSalesResponse(
                    storeId,
                    storeName,
                    sales.totalRevenue,
                    sales.totalQuantity,
                    sales.transactionCount
            );
        }
    }
}
