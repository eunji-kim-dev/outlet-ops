// 판매 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.sale.dto;

// 판매 상품과 수량 검증에 필요한 어노테이션
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 판매에 포함할 개별 상품 요청 DTO
public record SaleItemCreateRequest(

        // 판매할 상품의 ID
        @NotNull(message = "상품 ID는 필수입니다.")
        @Positive(message = "상품 ID는 양수여야 합니다.")
        Long productId,

        // 판매할 상품 수량
        @NotNull(message = "판매 수량은 필수입니다.")
        @Positive(message = "판매 수량은 0보다 커야 합니다.")
        Integer quantity
) {
}
