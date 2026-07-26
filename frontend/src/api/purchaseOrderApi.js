// 실행 환경에 맞는 백엔드 전체 주소 생성 함수 가져옴
import { apiUrl } from './apiConfig.js'

// 매장별 발주 목록 조회
export async function fetchPurchaseOrders(storeId) {
  // 선택한 매장 ID를 쿼리 파라미터로 전달
  const response = await fetch(
    apiUrl(`/api/purchase-orders?storeId=${storeId}`),
  )

  // 200번대 응답이 아닌 경우 오류 처리
  if (!response.ok) {
    throw new Error('발주 목록을 불러오지 못함')
  }

  // JSON 응답을 JavaScript 배열로 변환
  return response.json()
}

// 새로운 상품 발주 등록
export async function createPurchaseOrder(
  storeId,
  productId,
  quantity,
  unitCost,
) {
  // 발주 데이터를 백엔드에 전달
  const response = await fetch(apiUrl('/api/purchase-orders'), {
    // 새로운 발주를 생성하는 POST 방식 사용
    method: 'POST',

    // 요청 본문이 JSON 형식임을 백엔드에 전달
    headers: {
      'Content-Type': 'application/json',
    },

    // 화면 입력값을 백엔드 요청 형식으로 변환
    body: JSON.stringify({
      storeId: Number(storeId),
      items: [
        {
          productId: Number(productId),
          quantity: Number(quantity),
          unitCost: Number(unitCost),
        },
      ],
    }),
  })

  // 200번대 응답이 아닌 경우 오류 처리
  if (!response.ok) {
    throw new Error('발주를 등록하지 못함')
  }

  // 등록된 발주 정보를 JavaScript 객체로 변환
  return response.json()
}

// 발주 상품 입고 완료 처리
export async function receivePurchaseOrder(purchaseOrderId) {
  // 발주 ID를 주소에 포함한 입고 처리 요청
  const response = await fetch(
    apiUrl(`/api/purchase-orders/${purchaseOrderId}/receive`),
    {
      // 발주 상태와 재고 수량을 변경하는 PATCH 방식 사용
      method: 'PATCH',
    },
  )

  // 200번대 응답이 아닌 경우 오류 처리
  if (!response.ok) {
    throw new Error('발주를 입고 처리하지 못함')
  }

  // 변경된 발주 정보를 JavaScript 객체로 변환
  return response.json()
}

// 입고 전 발주 취소 처리
export async function cancelPurchaseOrder(purchaseOrderId) {
  // 발주 ID를 주소에 포함한 취소 처리 요청
  const response = await fetch(
    apiUrl(`/api/purchase-orders/${purchaseOrderId}/cancel`),
    {
      // 발주 상태를 변경하는 PATCH 방식 사용
      method: 'PATCH',
    },
  )

  // 200번대 응답이 아닌 경우 오류 처리
  if (!response.ok) {
    throw new Error('발주를 취소하지 못함')
  }

  // 변경된 발주 정보를 JavaScript 객체로 변환
  return response.json()
}
