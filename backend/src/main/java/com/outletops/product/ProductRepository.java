package com.outletops.product;

// Spring Data JPA의 기본 데이터 접근 기능을 제공
import org.springframework.data.jpa.repository.JpaRepository;

// Product 엔티티의 데이터베이스 작업을 담당하는 Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 동일한 SKU가 데이터베이스에 존재하는지 확인
    // Spring Data JPA가 메서드 이름을 분석해 쿼리를 자동 생성
    boolean existsBySku(String sku);
}