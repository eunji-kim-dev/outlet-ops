package com.outletops.product;

// 상품 등록 요청과 응답에 사용하는 DTO
import com.outletops.product.dto.ProductCreateRequest;
import com.outletops.product.dto.ProductResponse;

// SKU를 대문자로 변환할 때 언어 환경의 영향을 받지 않도록 사용
import java.util.List;
import java.util.Locale;

// final 필드를 받는 생성자를 자동 생성하는 Lombok
import lombok.RequiredArgsConstructor;

// HTTP 상태 코드와 Service 등록, 트랜잭션 처리에 필요한 클래스
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 상품 관련 업무 로직을 담당하는 Service
@Service

// final 필드인 productRepository를 받는 생성자를 자동 생성
@RequiredArgsConstructor

// 기본적으로 모든 메서드를 읽기 전용 트랜잭션으로 실행
@Transactional(readOnly = true)
public class ProductService {

    // 상품 데이터를 저장하고 조회하는 Repository
    private final ProductRepository productRepository;

    // 상품 등록은 DB 데이터를 변경하므로 일반 트랜잭션 적용
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {

        // SKU 앞뒤 공백을 제거하고 대문자로 통일
        String normalizedSku = request.sku()
                .strip()
                .toUpperCase(Locale.ROOT);

        // 동일한 SKU가 이미 등록돼 있는지 확인
        if (productRepository.existsBySku(normalizedSku)) {
            // 중복 상품 코드인 경우 HTTP 409 Conflict 반환
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 등록된 SKU입니다."
            );
        }

        // 검증과 정규화를 마친 요청 데이터로 상품 엔티티 생성
        Product product = new Product(
                normalizedSku,
                request.name().strip(),
                request.category().strip(),
                request.sellingPrice()
        );

        // 상품을 DB에 저장하고 응답 DTO로 변환
        return ProductResponse.from(productRepository.save(product));
    }

    // 등록된 전체 상품 조회
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                // 각 Product 엔티티를 ProductResponse로 변환
                .map(ProductResponse::from)
                // 변환된 응답을 List로 생성
                .toList();
    }
}
