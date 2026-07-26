// 매장 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.store.dto;

// 문자열이 null, 빈 문자열, 공백만 있는 값인지 검증
import jakarta.validation.constraints.NotBlank;
// 문자열 길이를 검증
import jakarta.validation.constraints.Size;

// 매장 등록 요청 데이터를 전달하는 DTO
// record는 불변 데이터 전달 객체를 간결하게 만들 수 있는 Java 문법
public record StoreCreateRequest(

        // 매장명은 null, 빈 문자열, 공백만 있는 문자열을 허용하지 않음
        @NotBlank(message = "매장명은 필수입니다.")
        // 매장명의 최대 길이를 100자로 제한
        @Size(max = 100, message = "매장명은 100자 이하여야 합니다.")
        String name,

        // 주소는 null, 빈 문자열, 공백만 있는 문자열을 허용하지 않음
        @NotBlank(message = "주소는 필수입니다.")
        // 주소의 최대 길이를 255자로 제한
        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        String address
) {
}
