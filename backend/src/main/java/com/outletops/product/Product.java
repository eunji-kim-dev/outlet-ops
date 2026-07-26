// 상품 도메인이 위치하는 패키지
package com.outletops.product;

// 이 클래스가 JPA 엔티티임을 선언
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 금액을 오차 없이 저장하기 위한 클래스
import java.math.BigDecimal;

// Getter와 기본 생성자를 자동 생성하는 Lombok
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Product 클래스를 JPA 엔티티로 등록
@Entity

// MySQL의 products 테이블과 연결
@Table(name = "products")

// 모든 필드의 Getter를 자동 생성
@Getter

// JPA가 사용할 기본 생성자를 protected로 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    // products 테이블의 기본 키
    @Id

    // MySQL AUTO_INCREMENT로 ID 자동 생성
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품 코드는 필수이며 중복을 허용하지 않음
    // 최대 길이는 50자
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    // 상품명은 필수이며 최대 길이는 100자
    @Column(nullable = false, length = 100)
    private String name;

    // 상품 분류는 필수이며 최대 길이는 50자
    @Column(nullable = false, length = 50)
    private String category;

    // 판매 가격은 필수
    // 전체 15자리 중 소수점 아래 2자리까지 저장
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal sellingPrice;

    // 새 상품은 기본적으로 판매 가능한 활성 상태
    @Column(nullable = false)
    private boolean active = true;

    // 애플리케이션에서 새 상품을 생성할 때 사용하는 생성자
    public Product(
            String sku,
            String name,
            String category,
            BigDecimal sellingPrice
    ) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.sellingPrice = sellingPrice;
    }
}