package com.outletops.store;

import com.outletops.store.dto.StoreCreateRequest;
import com.outletops.store.dto.StoreResponse;
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

// 매장 등록과 조회 흐름을 실제 JPA Repository까지 연결해 검증
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StoreServiceIntegrationTest {

    @Autowired
    private StoreService storeService;

    @Test
    @DisplayName("매장을 등록하면 목록에서 조회된다")
    void createAndFindAll() {
        StoreResponse created = storeService.create(
                new StoreCreateRequest("강남 테스트점", "서울 강남구")
        );

        List<StoreResponse> stores = storeService.findAll();

        assertThat(created.id()).isNotNull();
        assertThat(stores)
                .extracting(StoreResponse::name)
                .contains("강남 테스트점");
    }

    @Test
    @DisplayName("같은 이름의 매장을 중복 등록하면 409 오류가 발생한다")
    void rejectDuplicateName() {
        StoreCreateRequest request =
                new StoreCreateRequest("중복 테스트점", "서울 송파구");
        storeService.create(request);

        assertThatThrownBy(() -> storeService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.CONFLICT));
    }
}
