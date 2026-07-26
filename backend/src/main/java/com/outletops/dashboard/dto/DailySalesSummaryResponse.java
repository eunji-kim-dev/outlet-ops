// 매출 대시보드 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.dashboard.dto;

// 응답 DTO로 변환할 일별 매출 집계 엔티티
import com.outletops.dashboard.DailySalesSummary;

// 금액과 날짜를 표현하기 위한 클래스
import java.math.BigDecimal;
import java.time.LocalDate;

// 일별·매장별 매출 집계 결과를 반환하는 DTO
public record DailySalesSummaryResponse(
        Long id,
        LocalDate salesDate,
        Long storeId,
        String storeName,
        BigDecimal totalRevenue,
        long totalQuantity,
        long transactionCount
) {

    // DailySalesSummary 엔티티를 응답 DTO로 변환
    public static DailySalesSummaryResponse from(
            DailySalesSummary summary
    ) {
        return new DailySalesSummaryResponse(
                summary.getId(),
                summary.getSalesDate(),
                summary.getStore().getId(),
                summary.getStore().getName(),
                summary.getTotalRevenue(),
                summary.getTotalQuantity(),
                summary.getTransactionCount()
        );
    }
}
