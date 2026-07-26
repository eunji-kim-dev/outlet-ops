// 실행 환경에 맞는 백엔드 전체 주소 생성 함수 가져옴
import { apiUrl } from './apiConfig.js'

// 판매 등록 요청
export async function createSale(storeId, productId, quantity) {
  // 판매 데이터를 백엔드에 전달
  const response = await fetch(apiUrl('/api/sales'), {
    // 새로운 판매 데이터를 생성하는 POST 방식 사용
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
        },
      ],
    }),
  })

  // 200번대 응답이 아닌 경우 오류 처리
  if (!response.ok) {
    throw new Error('판매를 등록하지 못함')
  }

  // 등록된 판매 정보를 JavaScript 객체로 변환
  return response.json()
}
