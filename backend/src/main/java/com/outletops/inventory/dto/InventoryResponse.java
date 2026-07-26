// 재고 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.inventory.dto;

// 응답 DTO로 변환할 Inventory 엔티티
import com.outletops.inventory.Inventory;

// 매장별 상품 재고 정보를 외부에 반환하는 응답 DTO
public record InventoryResponse(

        // 재고 식별자
        Long id,

        // 매장 식별자와 매장명
        Long storeId,
        String storeName,

        // 상품 식별자, SKU, 상품명
        Long productId,
        String sku,
        String productName,

        // 현재 재고 수량과 재주문 기준
        int quantity,
        int reorderPoint,

        // 재고 부족 여부
        boolean lowStock
) {

    // Inventory 엔티티를 응답 DTO로 변환
    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getStore().getId(),
                inventory.getStore().getName(),
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                inventory.getProduct().getName(),
                inventory.getQuantity(),
                inventory.getReorderPoint(),
                inventory.isLowStock()
        );
    }
}
