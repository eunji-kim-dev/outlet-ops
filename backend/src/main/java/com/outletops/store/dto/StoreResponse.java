// 매장 API의 요청·응답 객체가 위치하는 패키지
package com.outletops.store.dto;

// 응답 DTO로 변환할 Store 엔티티
import com.outletops.store.Store;

// 매장 정보를 외부에 반환하기 위한 응답 DTO
public record StoreResponse(
        
    // 데이터베이스에서 생성된 매장 식별자
    Long id,
    // 매장명
    String name,
    // 매장 주소
    String address,
    // 매장 활성 상태
    boolean active
) {
    // Store 엔티티를 StoreResponse DTO로 변환하는 정적 팩토리 메서드
    public static StoreResponse from(Store store) {
       
        // Store에서 응답에 필요한 값만 꺼내 새로운 DTO 생성
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.isActive()
        );
    }
}
