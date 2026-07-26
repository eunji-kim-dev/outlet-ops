// 애플리케이션 공통 설정이 위치하는 패키지
package com.outletops.config;

// 상태 확인 응답 생성에 필요한 Map
import java.util.Map;

// HTTP GET 요청과 JSON 응답에 필요한 어노테이션
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 배포된 백엔드의 실행 상태를 확인하는 API
@RestController
@RequestMapping("/api/health")
public class HealthController {

    // GET /api/health 요청에 정상 상태 반환
    @GetMapping
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
