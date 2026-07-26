// 판매 도메인이 위치하는 패키지
package com.outletops.sale;

// 판매 등록 요청과 응답 DTO
import com.outletops.sale.dto.SaleCreateRequest;
import com.outletops.sale.dto.SaleResponse;

// Swagger 문서와 요청값 검증에 필요한 클래스
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// 생성자 주입과 HTTP 요청 처리에 필요한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Swagger에서 판매 API를 하나의 그룹으로 표시
@Tag(name = "판매", description = "판매 등록 및 재고 차감 API")

// JSON을 반환하는 REST Controller로 등록
@RestController

// 이 Controller의 모든 API 앞에 /api/sales 경로 적용
@RequestMapping("/api/sales")

// final 필드인 saleService를 받는 생성자를 자동 생성
@RequiredArgsConstructor
public class SaleController {

    // 판매 등록 업무를 실행할 Service
    private final SaleService saleService;

    // Swagger에 판매 등록 API 설명 표시
    @Operation(summary = "판매 등록 및 재고 차감")

    // POST /api/sales 요청을 이 메서드와 연결
    @PostMapping

    // 판매 등록 성공 시 HTTP 201 Created 반환
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(
            @Valid @RequestBody SaleCreateRequest request
    ) {
        return saleService.create(request);
    }
}
