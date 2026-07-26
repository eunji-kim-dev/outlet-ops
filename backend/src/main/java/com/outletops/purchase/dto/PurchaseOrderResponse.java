// 발주 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.purchase.dto;

// 응답으로 변환할 발주 엔티티와 상태
import com.outletops.purchase.PurchaseOrder;
import com.outletops.purchase.PurchaseOrderStatus;

// 금액, 시각, 발주 항목 목록에 필요한 클래스
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 발주 등록과 조회 결과를 반환하는 응답 DTO
public record PurchaseOrderResponse(
        Long id,
        Long storeId,
        String storeName,
        LocalDateTime orderedAt,
        LocalDateTime receivedAt,
        LocalDateTime cancelledAt,
        PurchaseOrderStatus status,
        BigDecimal totalAmount,
        List<PurchaseOrderItemResponse> items
) {

    // PurchaseOrder와 항목들을 하나의 응답 DTO로 변환
    public static PurchaseOrderResponse from(PurchaseOrder purchaseOrder) {
        List<PurchaseOrderItemResponse> itemResponses =
                purchaseOrder.getItems().stream()
                        .map(PurchaseOrderItemResponse::from)
                        .toList();

        return new PurchaseOrderResponse(
                purchaseOrder.getId(),
                purchaseOrder.getStore().getId(),
                purchaseOrder.getStore().getName(),
                purchaseOrder.getOrderedAt(),
                purchaseOrder.getReceivedAt(),
                purchaseOrder.getCancelledAt(),
                purchaseOrder.getStatus(),
                purchaseOrder.getTotalAmount(),
                itemResponses
        );
    }
}
