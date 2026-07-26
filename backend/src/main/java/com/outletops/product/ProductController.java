package com.outletops.product;

// 상품 등록 요청과 응답에 사용하는 DTO
import com.outletops.product.dto.ProductCreateRequest;
import com.outletops.product.dto.ProductResponse;

// Swagger 문서에 API 설명을 표시하는 어노테이션
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// 요청 DTO의 검증 규칙을 실행
import jakarta.validation.Valid;

// 여러 상품 응답을 반환하기 위한 List
import java.util.List;

// final 필드를 받는 생성자를 자동 생성하는 Lombok
import lombok.RequiredArgsConstructor;

// HTTP 요청과 응답 처리에 필요한 Spring MVC 클래스
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Swagger에서 상품 API를 하나의 그룹으로 표시
@Tag(name = "상품", description = "상품 등록 및 조회 API")

// JSON을 반환하는 REST Controller로 등록
@RestController

// 이 Controller의 모든 API 앞에 /api/products 경로 적용
@RequestMapping("/api/products")

// final 필드인 productService를 받는 생성자를 자동 생성
@RequiredArgsConstructor
public class ProductController {

    // 상품 업무 로직을 실행할 Service
    private final ProductService productService;

    // Swagger에 상품 등록 API 설명 표시
    @Operation(summary = "상품 등록")

    // POST /api/products 요청을 이 메서드와 연결
    @PostMapping

    // 상품 등록 성공 시 HTTP 201 Created 반환
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            // JSON 요청을 DTO로 변환하고 입력값 검증 실행
            @Valid @RequestBody ProductCreateRequest request
    ) {
        // 상품 등록 업무를 Service에 위임
        return productService.create(request);
    }

    // Swagger에 상품 목록 조회 API 설명 표시
    @Operation(summary = "상품 목록 조회")

    // GET /api/products 요청을 이 메서드와 연결
    @GetMapping
    public List<ProductResponse> findAll() {
        // 상품 전체 조회 업무를 Service에 위임
        return productService.findAll();
    }
}
