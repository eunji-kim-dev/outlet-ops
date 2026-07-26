// 실행 환경에 맞는 백엔드 전체 주소 생성 함수 가져옴
import { apiUrl } from './apiConfig.js'

// 기간과 매장 조건에 맞는 매출 대시보드 조회
export async function fetchSalesDashboard(
  startDate,
  endDate,
  storeId,
) {
  // 쿼리 파라미터 생성을 위한 객체 설정
  const searchParams = new URLSearchParams({
    startDate,
    endDate,
  })

  // 특정 매장이 선택된 경우에만 매장 ID 추가
  if (storeId) {
    searchParams.set('storeId', storeId)
  }

  // 완성된 조회 조건을 대시보드 API에 전달
  const response = await fetch(
    apiUrl(`/api/dashboard/sales?${searchParams.toString()}`),
  )

  // 200번대 응답이 아닌 경우 오류 처리
  if (!response.ok) {
    throw new Error('매출 대시보드를 불러오지 못함')
  }

  // JSON 응답을 JavaScript 객체로 변환
  return response.json()
}
