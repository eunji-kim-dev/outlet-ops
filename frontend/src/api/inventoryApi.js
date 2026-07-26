// 백엔드에서 전체 매장 목록 조회
export async function fetchStores() {
  // Vite 프록시를 통한 매장 조회 요청
  const response = await fetch('/api/stores')

  // 200번대 응답이 아닌 경우 오류 처리
  if (!response.ok) {
    throw new Error('매장 목록을 불러오지 못함')
  }

  // JSON 응답을 JavaScript 배열로 변환
  return response.json()
}

// 선택한 매장의 재고 목록 조회
export async function fetchInventories(storeId) {
  // 쿼리 파라미터에 매장 ID를 넣어 재고 조회 요청
  const response = await fetch(`/api/inventories?storeId=${storeId}`)

  // 200번대 응답이 아닌 경우 오류 처리
  if (!response.ok) {
    throw new Error('재고 목록을 불러오지 못함')
  }

  // JSON 응답을 JavaScript 배열로 변환
  return response.json()
}

// 특정 재고의 수량 변경
export async function updateInventoryQuantity(inventoryId, quantity) {
  // 재고 ID를 주소에 포함한 수량 변경 요청
  const response = await fetch(
    `/api/inventories/${inventoryId}/quantity`,
    {
      // 일부 데이터만 변경하는 PATCH 방식 사용
      method: 'PATCH',

      // 요청 본문이 JSON 형식임을 백엔드에 전달
      headers: {
        'Content-Type': 'application/json',
      },

      // JavaScript 객체를 JSON 문자열로 변환
      body: JSON.stringify({
        quantity: Number(quantity),
      }),
    },
  )

  // 200번대 응답이 아닌 경우 오류 처리
  if (!response.ok) {
    throw new Error('재고 수량을 변경하지 못함')
  }

  // 변경된 재고 정보를 JavaScript 객체로 변환
  return response.json()
}