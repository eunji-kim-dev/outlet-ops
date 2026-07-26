package com.outletops.product.dto;

// 가격의 전체 자릿수와 소수점 자릿수를 검증
import jakarta.validation.constraints.Digits;

// 가격이 지정한 값보다 큰지 검증
import jakarta.validation.constraints.DecimalMin;

// 문자열이 null, 빈 문자열 또는 공백인지 검증
import jakarta.validation.constraints.NotBlank;

// 값이 null인지 검증
import jakarta.validation.constraints.NotNull;

// 문자열 길이를 검증
import jakarta.validation.constraints.Size;

// 금액을 정확하게 표현하기 위한 클래스
import java.math.BigDecimal;

// 상품 등록 요청 데이터를 전달하는 DTO
public record ProductCreateRequest(

        // SKU는 필수이며 최대 50자
        @NotBlank(message = "SKU는 필수입니다.")
        @Size(max = 50, message = "SKU는 50자 이하여야 합니다.")
        String sku,

        // 상품명은 필수이며 최대 100자
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
        String name,

        // 카테고리는 필수이며 최대 50자
        @NotBlank(message = "카테고리는 필수입니다.")
        @Size(max = 50, message = "카테고리는 50자 이하여야 합니다.")
        String category,

        // 판매 가격은 null일 수 없음
        @NotNull(message = "판매 가격은 필수입니다.")

        // 판매 가격은 0보다 커야 함
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "판매 가격은 0보다 커야 합니다."
        )

        // 정수부 최대 13자리, 소수부 최대 2자리
        @Digits(
                integer = 13,
                fraction = 2,
                message = "판매 가격은 정수 13자리, 소수 2자리 이하여야 합니다."
        )
        BigDecimal sellingPrice
) {
}