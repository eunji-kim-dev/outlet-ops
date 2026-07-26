// 판매 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.sale.dto;

// 응답으로 변환할 SaleItem 엔티티
import com.outletops.sale.SaleItem;

// 판매 금액을 정확하게 표현하기 위한 클래스
import java.math.BigDecimal;

// 개별 판매 상품 정보를 반환하는 응답 DTO
public record SaleItemResponse(
        Long productId,
        String sku,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineAmount
) {

    // SaleItem 엔티티를 응답 DTO로 변환
    public static SaleItemResponse from(SaleItem item) {
        return new SaleItemResponse(
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineAmount()
        );
    }
}
