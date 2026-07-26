// 매출 대시보드 도메인이 위치하는 패키지
package com.outletops.dashboard;

// 매출 대시보드 최종 응답 DTO
import com.outletops.dashboard.dto.SalesDashboardResponse;

// Swagger 문서에 API 설명을 표시하는 어노테이션
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// 날짜 요청값을 표현하기 위한 클래스
import java.time.LocalDate;

// 생성자 주입과 HTTP 요청 처리에 필요한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Swagger에서 매출 대시보드 API를 하나의 그룹으로 표시
@Tag(name = "매출 대시보드", description = "기간별 핵심 지표와 차트 데이터 API")

// JSON을 반환하는 REST Controller로 등록
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class SalesDashboardController {

    // 대시보드 데이터를 생성할 Service
    private final SalesDashboardService dashboardService;

    // GET /api/dashboard/sales 요청으로 기간별 매출 조회
    @Operation(summary = "기간별 매출 대시보드 조회")
    @GetMapping("/sales")
    public SalesDashboardResponse getSalesDashboard(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            // 생략하면 전체 매장, 입력하면 해당 매장만 조회
            @RequestParam(required = false)
            Long storeId
    ) {
        return dashboardService.getDashboard(
                startDate,
                endDate,
                storeId
        );
    }
}
