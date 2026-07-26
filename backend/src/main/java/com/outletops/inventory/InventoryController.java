// 재고 도메인이 위치하는 패키지
package com.outletops.inventory;

// 재고 요청과 응답에 사용하는 DTO
import com.outletops.inventory.dto.InventoryCreateRequest;
import com.outletops.inventory.dto.InventoryQuantityUpdateRequest;
import com.outletops.inventory.dto.InventoryResponse;

// Swagger 문서와 입력값 검증에 필요한 클래스
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// 여러 재고 응답을 반환하기 위한 List
import java.util.List;

// 생성자 주입과 HTTP 요청 처리에 필요한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Swagger에서 재고 API를 하나의 그룹으로 표시
@Tag(name = "재고", description = "매장별 상품 재고 생성, 조회 및 수량 조정 API")

// JSON을 반환하는 REST Controller로 등록
@RestController

// 이 Controller의 모든 API 앞에 /api/inventories 경로 적용
@RequestMapping("/api/inventories")

// final 필드인 inventoryService를 받는 생성자를 자동 생성
@RequiredArgsConstructor
public class InventoryController {

    // 재고 업무 로직을 실행할 Service
    private final InventoryService inventoryService;

    // Swagger에 매장별 상품 재고 생성 API 설명 표시
    @Operation(summary = "매장별 상품 재고 생성")

    // POST /api/inventories 요청을 이 메서드와 연결
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse create(
            @Valid @RequestBody InventoryCreateRequest request
    ) {
        return inventoryService.create(request);
    }

    // Swagger에 매장별 재고 목록 조회 API 설명 표시
    @Operation(summary = "매장별 재고 목록 조회")

    // GET /api/inventories?storeId={storeId} 요청 처리
    @GetMapping
    public List<InventoryResponse> findByStore(
            @RequestParam Long storeId
    ) {
        return inventoryService.findByStore(storeId);
    }

    // Swagger에 재고 수량 조정 API 설명 표시
    @Operation(summary = "재고 수량 조정")

    // PATCH /api/inventories/{inventoryId}/quantity 요청 처리
    @PatchMapping("/{inventoryId}/quantity")
    public InventoryResponse updateQuantity(
            @PathVariable Long inventoryId,
            @Valid @RequestBody InventoryQuantityUpdateRequest request
    ) {
        return inventoryService.updateQuantity(inventoryId, request);
    }
}
