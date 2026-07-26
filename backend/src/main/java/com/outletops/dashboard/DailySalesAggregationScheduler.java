// 매출 대시보드 도메인이 위치하는 패키지
package com.outletops.dashboard;

// 서울 시간 기준 전날 날짜를 계산하기 위한 클래스
import java.time.LocalDate;
import java.time.ZoneId;

// 생성자 주입과 스케줄러 등록에 필요한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 매일 자동으로 전날 판매 데이터를 집계하는 스케줄러
@Component
@RequiredArgsConstructor
public class DailySalesAggregationScheduler {

    // 일별 판매 집계를 실행할 Service
    private final DailySalesAggregationService aggregationService;

    // 매일 한국 시간 자정에 실행
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void aggregatePreviousDay() {

        // 자정 실행 시 방금 끝난 전날 날짜 계산
        LocalDate previousDay = LocalDate.now(
                ZoneId.of("Asia/Seoul")
        ).minusDays(1);

        aggregationService.aggregate(previousDay);
    }
}
