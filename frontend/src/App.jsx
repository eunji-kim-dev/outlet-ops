// 주소별 화면 설정에 필요한 컴포넌트 가져옴
import { Navigate, Route, Routes } from 'react-router-dom'

// 모든 화면에서 사용할 공통 메뉴 가져옴
import Navigation from './components/Navigation.jsx'

// 주소별로 표시할 페이지 컴포넌트 가져옴
import InventoryPage from './pages/InventoryPage.jsx'
import SalesPage from './pages/SalesPage.jsx'
import PurchaseOrderPage from './pages/PurchaseOrderPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'


// 애플리케이션의 전체 화면 구조와 주소 설정
function App() {
  return (
    // 메뉴와 Routes를 하나로 묶기 위한 Fragment
    <>
      {/* 모든 페이지 위에 공통 메뉴 출력 */}
      <Navigation />

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
          element={<DashboardPage />}
        />
      </Routes>
    </>
  )
}

// 다른 파일에서 App 컴포넌트를 사용할 수 있도록 내보냄
export default App