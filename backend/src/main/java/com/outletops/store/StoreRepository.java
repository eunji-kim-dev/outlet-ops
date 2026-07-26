// 매장 도메인이 위치하는 패키지
package com.outletops.store;

// Spring Data JPA의 기본 데이터 접근 기능을 제공하는 인터페이스
import org.springframework.data.jpa.repository.JpaRepository;

// Store 엔티티의 데이터베이스 작업을 담당하는 Repository
// JpaRepository<관리할 엔티티, 기본 키 타입>
public interface StoreRepository extends JpaRepository<Store, Long> {

    // 동일한 매장명이 데이터베이스에 존재하는지 확인
    // 메서드 이름을 분석해 Spring Data JPA가 쿼리를 자동 생성
    boolean existsByName(String name);
}
