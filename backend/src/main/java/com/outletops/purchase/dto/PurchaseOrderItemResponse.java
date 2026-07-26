// 발주 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.purchase.dto;

// 응답으로 변환할 발주 항목 엔티티
import com.outletops.purchase.PurchaseOrderItem;

// 발주 금액을 정확하게 표현하기 위한 클래스
import java.math.BigDecimal;

// 개별 발주 상품 정보를 반환하는 응답 DTO
public record PurchaseOrderItemResponse(
        Long productId,
        String sku,
        String productName,
        int quantity,
        BigDecimal unitCost,
        BigDecimal lineAmount
) {

    // PurchaseOrderItem 엔티티를 응답 DTO로 변환
    public static PurchaseOrderItemResponse from(PurchaseOrderItem item) {
        return new PurchaseOrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitCost(),
                item.getLineAmount()
        );
    }
}
