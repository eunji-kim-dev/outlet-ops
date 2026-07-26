// 발주 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.purchase.dto;

// 상품·수량·금액 검증에 필요한 어노테이션
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 금액을 정확하게 표현하기 위한 클래스
import java.math.BigDecimal;

// 발주에 포함할 개별 상품 요청 DTO
public record PurchaseOrderItemCreateRequest(

        // 발주할 상품 ID
        @NotNull(message = "상품 ID는 필수입니다.")
        @Positive(message = "상품 ID는 양수여야 합니다.")
        Long productId,

        // 발주 수량
        @NotNull(message = "발주 수량은 필수입니다.")
        @Positive(message = "발주 수량은 0보다 커야 합니다.")
        Integer quantity,

        // 발주 당시 상품의 매입 단가
        @NotNull(message = "매입 단가는 필수입니다.")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "매입 단가는 0보다 커야 합니다."
        )
        @Digits(
                integer = 13,
                fraction = 2,
                message = "매입 단가는 정수 13자리, 소수 2자리 이하여야 합니다."
        )
        BigDecimal unitCost
) {
}
