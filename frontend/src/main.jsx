// React 개발 모드 검사 기능 가져옴
import { StrictMode } from 'react'

// React 화면을 HTML에 연결하는 createRoot 가져옴
import { createRoot } from 'react-dom/client'

// URL을 기준으로 화면을 전환하는 BrowserRouter 가져옴
import { BrowserRouter } from 'react-router-dom'

// 전체 화면 공통 CSS 적용
import './index.css'

// 최상위 화면 컴포넌트 가져옴
import App from './App.jsx'

// index.html의 id="root" 영역을 React 시작점으로 설정
createRoot(document.getElementById('root')).render(
  // 개발 중 잘못된 React 사용 방식 확인
  <StrictMode>
    {/* App 내부에서 주소 기반 화면 이동 사용 설정 */}
    <BrowserRouter>
      {/* 최상위 App 컴포넌트 출력 */}
      <App />
    </BrowserRouter>
  </StrictMode>,
)