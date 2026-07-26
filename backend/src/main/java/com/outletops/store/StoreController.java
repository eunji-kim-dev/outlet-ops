package com.outletops.store;

import com.outletops.store.dto.StoreCreateRequest;
import com.outletops.store.dto.StoreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Swagger에서 매장 API를 하나의 그룹으로 표시
@Tag(name = "매장", description = "매장 등록 및 조회 API")

// Spring이 이 클래스를 REST Controller로 관리
// 메서드 반환값을 JSON 응답으로 자동 변환
@RestController

// 이 Controller의 모든 API 앞에 /api/stores를 적용
@RequestMapping("/api/stores")

// final 필드인 storeService를 받는 생성자를 자동 생성
@RequiredArgsConstructor
public class StoreController {

    // 실제 매장 업무 로직을 실행할 Service
    private final StoreService storeService;

    // Swagger에 매장 등록 API의 설명을 표시
    @Operation(summary = "매장 등록")
    // POST /api/stores 요청을 이 메서드와 연결
    @PostMapping
    // 매장 등록 성공 시 HTTP 201 Created 반환
    @ResponseStatus(HttpStatus.CREATED)
    public StoreResponse create(
            // JSON 요청 본문을 StoreCreateRequest로 변환
            // @Valid를 통해 DTO의 @NotBlank, @Size 검증 실행
            @Valid @RequestBody StoreCreateRequest request) {
        // 매장 등록을 Service에 위임하고 결과 반환
        return storeService.create(request);
    }

    // Swagger에 매장 목록 조회 API의 설명을 표시
    @Operation(summary = "매장 목록 조회")
    // GET /api/stores 요청을 이 메서드와 연결
    @GetMapping
    public List<StoreResponse> findAll() {
        // 매장 전체 조회를 Service에 위임하고 결과 반환
        return storeService.findAll();
    }
}
