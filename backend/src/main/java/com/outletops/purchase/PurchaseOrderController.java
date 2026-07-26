// 발주 도메인이 위치하는 패키지
package com.outletops.purchase;

// 발주 등록 요청과 응답 DTO
import com.outletops.purchase.dto.PurchaseOrderCreateRequest;
import com.outletops.purchase.dto.PurchaseOrderResponse;

// Swagger 문서와 요청 검증에 필요한 클래스
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// 여러 발주 응답을 반환하기 위한 List
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

// Swagger에서 발주 API를 하나의 그룹으로 표시
@Tag(name = "발주", description = "발주 등록, 조회, 입고 및 취소 API")

// JSON을 반환하는 REST Controller로 등록
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    // 발주 업무 로직을 실행할 Service
    private final PurchaseOrderService purchaseOrderService;

    // POST /api/purchase-orders 요청으로 새 발주 등록
    @Operation(summary = "발주 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(
            @Valid @RequestBody PurchaseOrderCreateRequest request
    ) {
        return purchaseOrderService.create(request);
    }

    // GET /api/purchase-orders?storeId={storeId} 요청으로 매장별 조회
    @Operation(summary = "매장별 발주 목록 조회")
    @GetMapping
    public List<PurchaseOrderResponse> findByStore(
            @RequestParam Long storeId
    ) {
        return purchaseOrderService.findByStore(storeId);
    }

    // PATCH /api/purchase-orders/{id}/receive 요청으로 입고 완료
    @Operation(summary = "발주 입고 완료 및 재고 증가")
    @PatchMapping("/{purchaseOrderId}/receive")
    public PurchaseOrderResponse receive(
            @PathVariable Long purchaseOrderId
    ) {
        return purchaseOrderService.receive(purchaseOrderId);
    }

    // PATCH /api/purchase-orders/{id}/cancel 요청으로 발주 취소
    @Operation(summary = "발주 취소")
    @PatchMapping("/{purchaseOrderId}/cancel")
    public PurchaseOrderResponse cancel(
            @PathVariable Long purchaseOrderId
    ) {
        return purchaseOrderService.cancel(purchaseOrderId);
    }
}
