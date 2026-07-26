// 발주 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.purchase.dto;

// 요청값과 발주 항목 목록 검증에 필요한 어노테이션
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 여러 발주 항목을 전달하기 위한 List
import java.util.List;

// 발주 한 건을 등록하는 요청 DTO
public record PurchaseOrderCreateRequest(

        // 상품을 입고할 매장 ID
        @NotNull(message = "매장 ID는 필수입니다.")
        @Positive(message = "매장 ID는 양수여야 합니다.")
        Long storeId,

        // 발주 항목은 최소 한 개 이상 필요
        @NotEmpty(message = "발주 상품은 한 개 이상이어야 합니다.")
        List<@Valid PurchaseOrderItemCreateRequest> items
) {
}
