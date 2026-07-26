// 새로고침 없이 주소를 이동하기 위한 NavLink 가져옴
import { NavLink } from 'react-router-dom'

// 모든 화면에서 공통으로 사용하는 상단 메뉴
function Navigation() {
  return (
    // 주요 화면 이동 링크를 묶는 내비게이션 영역
    <nav>
      {/* 재고 관리 화면 이동 */}
      <NavLink to="/inventory">재고 관리</NavLink>

      {/* 판매 등록 화면 이동 */}
      <NavLink to="/sales">판매 등록</NavLink>

      {/* 발주 관리 화면 이동 */}
      <NavLink to="/purchase-orders">발주 관리</NavLink>

      {/* 매출 대시보드 화면 이동 */}
      <NavLink to="/dashboard">매출 대시보드</NavLink>
    </nav>
  )
}

// 다른 파일에서 Navigation을 사용할 수 있도록 내보냄
export default Navigation