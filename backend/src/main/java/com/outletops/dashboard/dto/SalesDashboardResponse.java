// 매출 대시보드 API의 응답 객체가 위치하는 패키지
package com.outletops.dashboard.dto;

// 금액, 조회 기간, 차트 목록을 표현하기 위한 클래스
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// 매출 대시보드 한 화면에 필요한 전체 데이터를 반환하는 DTO
public record SalesDashboardResponse(
        LocalDate startDate,
        LocalDate endDate,
        Long storeId,
        BigDecimal totalRevenue,
        long totalQuantity,
        long transactionCount,
        List<DashboardDailySalesResponse> dailySales,
        List<DashboardStoreSalesResponse> storeSales
) {
}
