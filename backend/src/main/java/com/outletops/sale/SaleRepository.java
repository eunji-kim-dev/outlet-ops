// 판매 도메인이 위치하는 패키지
package com.outletops.sale;

// Spring Data JPA의 기본 데이터 접근 기능
import org.springframework.data.jpa.repository.JpaRepository;

// Sale 엔티티의 저장과 조회를 담당하는 Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
}
