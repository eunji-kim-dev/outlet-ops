package com.outletops.product.dto;

// 응답 DTO로 변환할 Product 엔티티
import com.outletops.product.Product;

// 판매 가격을 정확하게 표현하기 위한 클래스
import java.math.BigDecimal;

// 상품 정보를 외부에 반환하는 응답 DTO
public record ProductResponse(

        // 데이터베이스에서 생성한 상품 식별자
        Long id,

        // 상품 고유 코드
        String sku,

        // 상품명
        String name,

        // 상품 카테고리
        String category,

        // 상품 판매 가격
        BigDecimal sellingPrice,

        // 상품 활성 상태
        boolean active
) {

    // Product 엔티티를 ProductResponse DTO로 변환
    public static ProductResponse from(Product product) {

        // 외부 응답에 필요한 값만 선택해 DTO 생성
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getSellingPrice(),
                product.isActive()
        );
    }
}
