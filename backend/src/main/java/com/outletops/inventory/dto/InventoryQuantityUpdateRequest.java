// 재고 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.inventory.dto;

// 수량 입력값 검증에 필요한 어노테이션
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

// 재고 수량 조정 요청 DTO
public record InventoryQuantityUpdateRequest(

        // 변경할 최종 재고 수량은 0 이상
        @NotNull(message = "재고 수량은 필수입니다.")
        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        Integer quantity
) {
}
