// 매출 대시보드 도메인이 위치하는 패키지
package com.outletops.dashboard;

// 일별 매출 집계 응답 DTO
import com.outletops.dashboard.dto.DailySalesSummaryResponse;

// Swagger 문서에 API 설명을 표시하는 어노테이션
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// 날짜와 결과 목록에 필요한 클래스
import java.time.LocalDate;
import java.util.List;

// 생성자 주입과 HTTP 요청 처리에 필요한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Swagger에서 일별 매출 집계 API를 하나의 그룹으로 표시
@Tag(name = "일별 매출 집계", description = "일별 매출 수동 집계 및 조회 API")

// JSON을 반환하는 REST Controller로 등록
@RestController
@RequestMapping("/api/daily-sales")
@RequiredArgsConstructor
public class DailySalesSummaryController {

    // 일별 판매 집계와 조회를 실행할 Service
    private final DailySalesAggregationService aggregationService;

    // 자정을 기다리지 않고 지정한 날짜를 수동 집계하는 API
    @Operation(summary = "지정 날짜 판매 데이터 수동 집계")
    @PostMapping("/aggregate")
    public List<DailySalesSummaryResponse> aggregate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return aggregationService.aggregate(date);
    }

    // 지정한 날짜의 매장별 집계 결과 조회 API
    @Operation(summary = "지정 날짜 매장별 매출 집계 조회")
    @GetMapping
    public List<DailySalesSummaryResponse> findByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return aggregationService.findByDate(date);
    }
}
