// 발주 도메인이 위치하는 패키지
package com.outletops.purchase;

// 발주의 현재 처리 상태
public enum PurchaseOrderStatus {

    // 발주가 등록되어 입고를 기다리는 상태
    ORDERED,

    // 상품 입고가 완료되어 재고에 반영된 상태
    RECEIVED,

    // 입고 전 발주가 취소된 상태
    CANCELLED
}
