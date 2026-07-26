// 판매 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.sale.dto;

// 응답으로 변환할 Sale 엔티티
import com.outletops.sale.Sale;

// 판매 금액, 판매 시각, 판매 항목 목록을 표현하는 클래스
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 판매 등록 결과를 반환하는 응답 DTO
public record SaleResponse(
        Long id,
        Long storeId,
        String storeName,
        LocalDateTime soldAt,
        BigDecimal totalAmount,
        List<SaleItemResponse> items
) {

    // Sale과 SaleItem 엔티티를 하나의 판매 응답으로 변환
    public static SaleResponse from(Sale sale) {
        List<SaleItemResponse> itemResponses = sale.getItems().stream()
                .map(SaleItemResponse::from)
                .toList();

        return new SaleResponse(
                sale.getId(),
                sale.getStore().getId(),
                sale.getStore().getName(),
                sale.getSoldAt(),
                sale.getTotalAmount(),
                itemResponses
        );
    }
}
