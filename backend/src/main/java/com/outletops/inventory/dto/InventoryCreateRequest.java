// 재고 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.inventory.dto;

// 입력값 검증에 필요한 어노테이션
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

// 매장별 상품 재고 생성 요청 DTO
public record InventoryCreateRequest(

        // 존재하는 매장 ID를 양수로 입력
        @NotNull(message = "매장 ID는 필수입니다.")
        @Positive(message = "매장 ID는 양수여야 합니다.")
        Long storeId,

        // 존재하는 상품 ID를 양수로 입력
        @NotNull(message = "상품 ID는 필수입니다.")
        @Positive(message = "상품 ID는 양수여야 합니다.")
        Long productId,

        // 초기 재고 수량은 0 이상
        @NotNull(message = "초기 재고 수량은 필수입니다.")
        @PositiveOrZero(message = "초기 재고 수량은 0 이상이어야 합니다.")
        Integer quantity,

        // 재주문 기준 수량은 0 이상
        @NotNull(message = "재주문 기준 수량은 필수입니다.")
        @PositiveOrZero(message = "재주문 기준 수량은 0 이상이어야 합니다.")
        Integer reorderPoint
) {
}
