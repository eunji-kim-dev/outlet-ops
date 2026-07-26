// 판매 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.sale.dto;

// 요청값과 판매 항목 목록 검증에 필요한 어노테이션
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 여러 판매 항목을 전달하기 위한 List
import java.util.List;

// 판매 한 건을 등록하는 요청 DTO
public record SaleCreateRequest(

        // 판매가 발생한 매장 ID
        @NotNull(message = "매장 ID는 필수입니다.")
        @Positive(message = "매장 ID는 양수여야 합니다.")
        Long storeId,

        // 판매 항목은 최소 한 개 이상 필요
        @NotEmpty(message = "판매 상품은 한 개 이상이어야 합니다.")
        List<@Valid SaleItemCreateRequest> items
) {
}
