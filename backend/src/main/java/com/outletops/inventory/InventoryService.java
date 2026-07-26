// 재고 도메인이 위치하는 패키지
package com.outletops.inventory;

// 재고 요청과 응답에 사용하는 DTO
import com.outletops.inventory.dto.InventoryCreateRequest;
import com.outletops.inventory.dto.InventoryQuantityUpdateRequest;
import com.outletops.inventory.dto.InventoryResponse;

// 상품과 매장 조회에 사용하는 Repository 및 엔티티
import com.outletops.product.Product;
import com.outletops.product.ProductRepository;
import com.outletops.store.Store;
import com.outletops.store.StoreRepository;

// 여러 재고 응답을 반환하기 위한 List
import java.util.List;

// 생성자 주입과 Spring Service에 필요한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 재고 관련 업무 로직을 담당하는 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    // 재고, 매장, 상품 데이터에 접근하는 Repository
    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    // 매장과 상품을 연결한 초기 재고 생성
    @Transactional
    public InventoryResponse create(InventoryCreateRequest request) {

        // 요청한 매장이 존재하지 않으면 HTTP 404 반환
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "매장을 찾을 수 없습니다."
                ));

        // 요청한 상품이 존재하지 않으면 HTTP 404 반환
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "상품을 찾을 수 없습니다."
                ));

        // 같은 매장과 상품 조합의 재고는 한 번만 생성 가능
        if (inventoryRepository.existsByStore_IdAndProduct_Id(
                request.storeId(),
                request.productId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 생성된 매장별 상품 재고입니다."
            );
        }

        // 검증된 매장, 상품, 수량으로 재고 엔티티 생성
        Inventory inventory = new Inventory(
                store,
                product,
                request.quantity(),
                request.reorderPoint()
        );

        // 재고를 저장하고 응답 DTO로 변환
        return InventoryResponse.from(inventoryRepository.save(inventory));
    }

    // 특정 매장의 전체 재고 조회
    public List<InventoryResponse> findByStore(Long storeId) {

        // 존재하지 않는 매장에 대한 조회는 HTTP 404 반환
        if (!storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "매장을 찾을 수 없습니다."
            );
        }

        return inventoryRepository
                .findByStore_IdOrderByProduct_NameAsc(storeId)
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }

    // 관리 화면에서 재고의 최종 수량 조정
    @Transactional
    public InventoryResponse updateQuantity(
            Long inventoryId,
            InventoryQuantityUpdateRequest request
    ) {
        // 변경할 재고가 존재하지 않으면 HTTP 404 반환
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "재고를 찾을 수 없습니다."
                ));

        // Entity의 상태 변경 메서드를 통해 수량 변경
        inventory.updateQuantity(request.quantity());

        // 트랜잭션 종료 시 JPA 변경 감지로 UPDATE 쿼리 실행
        return InventoryResponse.from(inventory);
    }
}
