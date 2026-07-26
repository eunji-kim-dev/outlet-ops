// Vercel 빌드 환경에 설정된 백엔드 기본 주소 확인
const configuredBaseUrl =
  import.meta.env.VITE_API_BASE_URL?.trim() ?? ''

// 주소 마지막의 불필요한 슬래시 제거
const API_BASE_URL = configuredBaseUrl.replace(/\/+$/, '')

// 로컬에서는 상대 주소, 운영에서는 Railway 전체 주소 생성
export function apiUrl(path) {
  return `${API_BASE_URL}${path}`
}
