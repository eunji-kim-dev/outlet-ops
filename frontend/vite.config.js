// Vite 설정 함수 가져옴
import { defineConfig } from 'vite'

// React JSX 변환과 개발 기능을 제공하는 플러그인 가져옴
import react from '@vitejs/plugin-react'

// Vite 개발 서버 설정
export default defineConfig({
  // React 프로젝트 설정
  plugins: [react()],

  // 로컬 개발 서버 설정
  server: {
    // /api 요청을 Spring Boot 서버로 전달
    proxy: {
      '/api': {
        // 실제 요청을 처리할 백엔드 주소
        target: 'http://localhost:8080',

        // 요청 출처를 백엔드 주소 기준으로 변경
        changeOrigin: true,
      },
    },
  },
})