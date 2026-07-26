// 발주 도메인이 위치하는 패키지
package com.outletops.purchase;

// 입고 시 재고 증가에 사용하는 엔티티와 Repository
import com.outletops.inventory.Inventory;
import com.outletops.inventory.InventoryRepository;

// 발주 상품 조회에 사용하는 엔티티와 Repository
import com.outletops.product.Product;
import com.outletops.product.ProductRepository;

// 발주 요청과 응답 DTO
import com.outletops.purchase.dto.PurchaseOrderCreateRequest;
import com.outletops.purchase.dto.PurchaseOrderItemCreateRequest;
import com.outletops.purchase.dto.PurchaseOrderResponse;

// 발주 매장 조회에 사용하는 엔티티와 Repository
import com.outletops.store.Store;
import com.outletops.store.StoreRepository;

// 목록 반환과 중복 상품 검사에 필요한 클래스
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 생성자 주입과 Service, 트랜잭션, HTTP 오류에 필요한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 발주 등록, 조회, 입고, 취소 업무를 담당하는 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderService {

    // 발주, 매장, 상품, 재고 데이터에 접근하는 Repository
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    // 새로운 발주 등록
    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderCreateRequest request) {
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "매장을 찾을 수 없습니다."
                ));

        if (!store.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "비활성화된 매장에는 발주할 수 없습니다."
            );
        }

        // 한 발주 안에서 동일 상품이 중복되지 않도록 검사
        validateDuplicateProducts(request);

        PurchaseOrder purchaseOrder = new PurchaseOrder(store);

        // 각 요청 항목의 상품을 조회하고 발주에 추가
        for (PurchaseOrderItemCreateRequest itemRequest : request.items()) {
            Product product = productRepository
                    .findById(itemRequest.productId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "상품을 찾을 수 없습니다."
                    ));

            if (!product.isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "비활성화된 상품은 발주할 수 없습니다."
                );
            }

            purchaseOrder.addItem(
                    product,
                    itemRequest.quantity(),
                    itemRequest.unitCost()
            );
        }

        // 발주 저장 시 Cascade에 의해 발주 항목도 함께 저장
        return PurchaseOrderResponse.from(
                purchaseOrderRepository.save(purchaseOrder)
        );
    }

    // 특정 매장의 발주 목록을 최신순으로 조회
    public List<PurchaseOrderResponse> findByStore(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "매장을 찾을 수 없습니다."
            );
        }

        return purchaseOrderRepository
                .findByStore_IdOrderByOrderedAtDesc(storeId)
                .stream()
                .map(PurchaseOrderResponse::from)
                .toList();
    }

    // 발주 입고 완료와 재고 증가를 하나의 트랜잭션으로 처리
    @Transactional
    public PurchaseOrderResponse receive(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = findForUpdate(purchaseOrderId);

        // 이미 처리된 발주는 다시 입고할 수 없음
        ensureOrdered(purchaseOrder);

        // 각 발주 상품에 해당하는 매장 재고를 잠근 뒤 수량 증가
        for (PurchaseOrderItem item : purchaseOrder.getItems()) {
            Inventory inventory = inventoryRepository.findForUpdate(
                            purchaseOrder.getStore().getId(),
                            item.getProduct().getId()
                    )
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            item.getProduct().getName()
                                    + "의 매장 재고가 등록되어 있지 않습니다."
                    ));

            inventory.increase(item.getQuantity());
        }

        // 모든 재고 증가에 성공한 뒤 발주 상태를 RECEIVED로 변경
        purchaseOrder.receive();
        return PurchaseOrderResponse.from(purchaseOrder);
    }

    // 입고 전 발주 취소
    @Transactional
    public PurchaseOrderResponse cancel(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = findForUpdate(purchaseOrderId);
        ensureOrdered(purchaseOrder);

        purchaseOrder.cancel();
        return PurchaseOrderResponse.from(purchaseOrder);
    }

    // 상태 변경을 위해 발주 상세를 잠금 조회
    private PurchaseOrder findForUpdate(Long purchaseOrderId) {
        return purchaseOrderRepository
                .findDetailsForUpdate(purchaseOrderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "발주를 찾을 수 없습니다."
                ));
    }

    // ORDERED 상태가 아니면 입고 또는 취소 차단
    private void ensureOrdered(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.ORDERED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 처리 완료된 발주입니다."
            );
        }
    }

    // 한 발주 요청 안에서 같은 상품 ID가 중복됐는지 확인
    private void validateDuplicateProducts(
            PurchaseOrderCreateRequest request
    ) {
        Set<Long> productIds = new HashSet<>();

        for (PurchaseOrderItemCreateRequest item : request.items()) {
            if (!productIds.add(item.productId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "동일한 상품이 발주 요청에 중복 포함됐습니다."
                );
            }
        }
    }
}
