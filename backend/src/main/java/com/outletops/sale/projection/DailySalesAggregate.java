// 판매 집계용 Projection이 위치하는 패키지
package com.outletops.sale.projection;

// 총매출 금액을 정확하게 표현하기 위한 클래스
import java.math.BigDecimal;

// DB 집계 쿼리 결과를 받는 인터페이스 기반 Projection
public interface DailySalesAggregate {

    // 집계 대상 매장 ID
    Long getStoreId();

    // 해당 날짜의 총매출
    BigDecimal getTotalRevenue();

    // 해당 날짜의 전체 판매 수량
    Long getTotalQuantity();

    // 해당 날짜의 판매 거래 건수
    Long getTransactionCount();
}
