// 판매 도메인이 위치하는 패키지
package com.outletops.sale;

// 재고 차감에 사용하는 Inventory와 Repository
import com.outletops.inventory.Inventory;
import com.outletops.inventory.InventoryRepository;

// 상품 조회에 사용하는 Product와 Repository
import com.outletops.product.Product;
import com.outletops.product.ProductRepository;

// 판매 요청과 응답 DTO
import com.outletops.sale.dto.SaleCreateRequest;
import com.outletops.sale.dto.SaleItemCreateRequest;
import com.outletops.sale.dto.SaleResponse;

// 매장 조회에 사용하는 Store와 Repository
import com.outletops.store.Store;
import com.outletops.store.StoreRepository;

// 요청 내부의 중복 상품을 검사하는 Set
import java.util.HashSet;
import java.util.Set;

// 생성자 주입과 Spring Service, 트랜잭션에 필요한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 판매 등록과 재고 차감을 담당하는 Service
@Service
@RequiredArgsConstructor
public class SaleService {

    // 판매, 매장, 상품, 재고에 접근하는 Repository
    private final SaleRepository saleRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    // 판매 저장과 모든 재고 차감을 하나의 트랜잭션으로 처리
    @Transactional
    public SaleResponse create(SaleCreateRequest request) {

        // 판매 매장이 존재하지 않으면 HTTP 404 반환
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "매장을 찾을 수 없습니다."
                ));

        // 비활성화된 매장에서는 판매할 수 없음
        if (!store.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "비활성화된 매장에서는 판매할 수 없습니다."
            );
        }

        // 한 요청에서 같은 상품이 여러 번 들어오는 것을 방지
        validateDuplicateProducts(request);

        // 판매 기본 정보 생성
        Sale sale = new Sale(store);

        // 각 판매 항목의 상품과 재고를 확인
        for (SaleItemCreateRequest itemRequest : request.items()) {
            Product product = findActiveProduct(itemRequest.productId());

            // 동시에 같은 재고를 판매하지 못하도록 쓰기 잠금 후 조회
            Inventory inventory = inventoryRepository.findForUpdate(
                            request.storeId(),
                            itemRequest.productId()
                    )
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "해당 매장에 등록된 상품 재고가 없습니다."
                    ));

            // 재고가 충분한 경우에만 판매 수량만큼 차감
            try {
                inventory.decrease(itemRequest.quantity());
            } catch (IllegalStateException exception) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        product.getName() + "의 재고가 부족합니다."
                );
            }

            // 판매 당시 가격과 수량을 판매 항목에 저장
            sale.addItem(
                    product,
                    itemRequest.quantity(),
                    product.getSellingPrice()
            );
        }

        // Sale 저장 시 Cascade에 의해 SaleItem도 함께 저장
        return SaleResponse.from(saleRepository.save(sale));
    }

    // 상품 존재 여부와 활성 상태 확인
    private Product findActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "상품을 찾을 수 없습니다."
                ));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "비활성화된 상품은 판매할 수 없습니다."
            );
        }

        return product;
    }

    // 한 판매 요청 안에서 같은 상품 ID가 중복됐는지 확인
    private void validateDuplicateProducts(SaleCreateRequest request) {
        Set<Long> productIds = new HashSet<>();

        for (SaleItemCreateRequest item : request.items()) {
            if (!productIds.add(item.productId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "동일한 상품이 판매 요청에 중복 포함됐습니다."
                );
            }
        }
    }
}
