// 대시보드 지연 로딩에 필요한 React 기능 가져옴
import { lazy, Suspense } from 'react'

// 주소별 화면 설정에 필요한 컴포넌트 가져옴
import { Navigate, Route, Routes } from 'react-router-dom'

// 모든 화면에서 사용할 공통 메뉴 가져옴
import Navigation from './components/Navigation.jsx'

// 주소별로 표시할 페이지 컴포넌트 가져옴
import InventoryPage from './pages/InventoryPage.jsx'
import SalesPage from './pages/SalesPage.jsx'
import PurchaseOrderPage from './pages/PurchaseOrderPage.jsx'

// Recharts가 포함된 대시보드를 해당 메뉴 접속 시점에 불러옴
const DashboardPage = lazy(() => import('./pages/DashboardPage.jsx'))

// 애플리케이션의 전체 화면 구조와 주소 설정
function App() {
  return (
    // 왼쪽 메뉴와 오른쪽 본문을 나누는 전체 레이아웃
    <div className="app-shell">
      {/* 모든 페이지 옆에 공통 메뉴 출력 */}
      <Navigation />

      {/* 선택한 메뉴의 페이지가 표시되는 본문 영역 */}
      <div className="app-content">
        {/* 브라우저 주소에 맞는 페이지 출력 */}
        <Routes>
          {/* 기본 주소 접속 시 재고 관리 주소로 이동 */}
          <Route
            path="/"
            element={<Navigate to="/inventory" replace />}
          />

          {/* 재고 관리 화면 연결 */}
          <Route
            path="/inventory"
            element={<InventoryPage />}
          />

          {/* 판매 등록 화면 연결 */}
          <Route
            path="/sales"
            element={<SalesPage />}
          />

          {/* 발주 관리 화면 연결 */}
          <Route
            path="/purchase-orders"
            element={<PurchaseOrderPage />}
          />

          {/* 매출 대시보드 화면 연결 */}
          <Route
            path="/dashboard"
            element={
              // 대시보드 파일을 불러오는 동안 안내 문구 표시
              <Suspense
                fallback={
                  <p className="loading-note">
                    대시보드 화면을 불러오는 중...
                  </p>
                }
              >
                <DashboardPage />
              </Suspense>
            }
          />
        </Routes>
      </div>
    </div>
  )
}

// 다른 파일에서 App 컴포넌트를 사용할 수 있도록 내보냄
export default App
