package com.outletops.product;

import com.outletops.product.dto.ProductCreateRequest;
import com.outletops.product.dto.ProductResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 상품 등록, SKU 정규화, 조회, 중복 검사를 통합 검증
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("상품을 등록하면 SKU가 정규화되고 목록에서 조회된다")
    void createAndFindAll() {
        ProductResponse created = productService.create(
                request("  test-sku-001  ")
        );

        List<ProductResponse> products = productService.findAll();

        assertThat(created.id()).isNotNull();
        assertThat(created.sku()).isEqualTo("TEST-SKU-001");
        assertThat(products)
                .extracting(ProductResponse::sku)
                .contains("TEST-SKU-001");
    }

    @Test
    @DisplayName("대소문자와 공백만 다른 SKU도 중복으로 차단한다")
    void rejectNormalizedDuplicateSku() {
        productService.create(request("test-sku-002"));

        assertThatThrownBy(
                () -> productService.create(request(" TEST-SKU-002 "))
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.CONFLICT));
    }

    private ProductCreateRequest request(String sku) {
        return new ProductCreateRequest(
                sku,
                "테스트 상품",
                "상의",
                new BigDecimal("19900.00")
        );
    }
}
