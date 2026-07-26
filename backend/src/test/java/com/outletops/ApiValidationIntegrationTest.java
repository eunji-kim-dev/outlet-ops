package com.outletops;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Controller에 전달된 잘못된 JSON 요청이 HTTP 400으로 차단되는지 검증
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("빈 매장명으로 등록하면 400 오류가 발생한다")
    void rejectBlankStoreName() throws Exception {
        mockMvc.perform(
                post("/api/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "address": "서울"
                                }
                                """)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("판매 가격이 0원이면 상품 등록에서 400 오류가 발생한다")
    void rejectZeroProductPrice() throws Exception {
        mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "INVALID-SKU",
                                  "name": "잘못된 상품",
                                  "category": "상의",
                                  "sellingPrice": 0
                                }
                                """)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("초기 재고가 음수이면 400 오류가 발생한다")
    void rejectNegativeInventoryQuantity() throws Exception {
        mockMvc.perform(
                post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeId": 1,
                                  "productId": 1,
                                  "quantity": -1,
                                  "reorderPoint": 2
                                }
                                """)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("판매 상품 목록이 비어 있으면 400 오류가 발생한다")
    void rejectEmptySaleItems() throws Exception {
        mockMvc.perform(
                post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeId": 1,
                                  "items": []
                                }
                                """)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("매입 단가가 0원이면 발주 등록에서 400 오류가 발생한다")
    void rejectZeroPurchaseUnitCost() throws Exception {
        mockMvc.perform(
                post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeId": 1,
                                  "items": [
                                    {
                                      "productId": 1,
                                      "quantity": 5,
                                      "unitCost": 0
                                    }
                                  ]
                                }
                                """)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("대시보드 종료일이 시작일보다 빠르면 400 오류가 발생한다")
    void rejectReversedDashboardPeriod() throws Exception {
        mockMvc.perform(
                get("/api/dashboard/sales")
                        .param("startDate", "2026-07-26")
                        .param("endDate", "2026-07-25")
        ).andExpect(status().isBadRequest());
    }
}
