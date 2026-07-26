// 매출 대시보드 API의 응답 객체가 위치하는 패키지
package com.outletops.dashboard.dto;

// 매장별 매출 금액을 표현하기 위한 클래스
import java.math.BigDecimal;

// Recharts 매장별 매출 비교 차트에 사용할 응답 DTO
public record DashboardStoreSalesResponse(
        Long storeId,
        String storeName,
        BigDecimal totalRevenue,
        long totalQuantity,
        long transactionCount
) {
}
