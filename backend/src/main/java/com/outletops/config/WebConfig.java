// 애플리케이션 공통 설정이 위치하는 패키지
package com.outletops.config;

// CORS 허용 주소 환경변수를 읽기 위한 어노테이션
import org.springframework.beans.factory.annotation.Value;

// Spring 설정 클래스로 등록하기 위한 어노테이션
import org.springframework.context.annotation.Configuration;

// API 경로의 CORS 정책 설정에 필요한 클래스
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 쉼표로 구분된 주소 문자열을 배열로 변환하기 위한 클래스
import java.util.Arrays;

// 프론트엔드와 백엔드의 다른 출처 요청을 허용하는 설정
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 환경변수에서 읽어 변환한 프론트엔드 허용 주소 목록
    private final String[] allowedOrigins;

    // 쉼표로 구분된 허용 주소를 공백 없는 배열로 변환
    public WebConfig(
            @Value("${app.cors.allowed-origins}")
            String allowedOrigins
    ) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);
    }

    // /api 아래의 모든 백엔드 요청에 CORS 정책 적용
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(
                        "GET",
                        "POST",
                        "PATCH",
                        "OPTIONS"
                )
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
