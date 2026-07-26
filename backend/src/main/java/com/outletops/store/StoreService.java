package com.outletops.store;

import com.outletops.store.dto.StoreCreateRequest;
import com.outletops.store.dto.StoreResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 매장 관련 업무 로직을 담당하는 Service
@Service
// final 필드인 storeRepository를 받는 생성자를 자동 생성
@RequiredArgsConstructor
// 기본적으로 Service의 모든 메서드를 읽기 전용 트랜잭션으로 실행
@Transactional(readOnly = true)
public class StoreService {

    // 매장 데이터를 저장하고 조회하는 Repository
    private final StoreRepository storeRepository;

    // 매장 등록은 DB 데이터를 변경하므로 일반 트랜잭션 적용
    @Transactional
    public StoreResponse create(StoreCreateRequest request) {
        
        // 요청받은 매장명과 같은 이름이 이미 존재하는지 확인
        if (storeRepository.existsByName(request.name())) {
            // 중복 매장인 경우 HTTP 409 Conflict 예외 발생
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 등록된 매장명입니다."
            );
        }

        // 검증이 끝난 요청 데이터로 새로운 Store 엔티티 생성
        Store store = new Store(request.name(), request.address());
        // Store를 DB에 저장하고, 저장 결과를 응답 DTO로 변환
        return StoreResponse.from(storeRepository.save(store));
    }
    // 모든 매장을 조회하는 메서드
    public List<StoreResponse> findAll() {
        // DB에서 모든 Store 엔티티 조회
        return storeRepository.findAll().stream()
                // 각각의 Store를 StoreResponse로 변환
                .map(StoreResponse::from)
                // 변환 결과를 List로 생성
                .toList();
    }
}
