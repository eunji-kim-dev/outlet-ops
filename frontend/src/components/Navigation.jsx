// 새로고침 없이 주소를 이동하기 위한 NavLink 가져옴
import { NavLink } from 'react-router-dom'

// 메뉴에 표시할 주소·이름·아이콘 경로를 한곳에 모아둠
const MENU_ITEMS = [
  {
    to: '/inventory',
    label: '재고 관리',
    icon: 'M3 7l9-4 9 4-9 4-9-4zm0 5l9 4 9-4M3 17l9 4 9-4',
  },
  {
    to: '/sales',
    label: '판매 등록',
    icon: 'M3 3h2l2.4 12h10.2L20 7H6M9 20a1 1 0 100-2 1 1 0 000 2zm8 0a1 1 0 100-2 1 1 0 000 2z',
  },
  {
    to: '/purchase-orders',
    label: '발주 관리',
    icon: 'M9 4h9l3 3v13H9V4zM9 8H3v12h6M13 12h4M13 16h4',
  },
  {
    to: '/dashboard',
    label: '매출 대시보드',
    icon: 'M4 20V10M10 20V4M16 20v-7M22 20H2',
  },
]

// 모든 화면에서 공통으로 사용하는 사이드 메뉴
function Navigation() {
  return (
    // 주요 화면 이동 링크를 묶는 내비게이션 영역
    <nav className="app-nav">
      {/* 서비스 이름 영역 */}
      <div className="nav-brand">
        <span className="nav-logo">OO</span>
        <span>
          <span className="nav-title">OutletOps</span>
          <span className="nav-subtitle">매장 운영 관리</span>
        </span>
      </div>

      {/* 메뉴 목록을 배열에서 한 번에 생성 */}
      <div>
        <div className="nav-section-label">메뉴</div>

        <div className="nav-links">
          {MENU_ITEMS.map((item) => (
            <NavLink key={item.to} to={item.to} className="nav-link">
              {/* 메뉴를 구분하는 선 그림 아이콘 */}
              <svg
                className="nav-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <path d={item.icon} />
              </svg>

              {item.label}
            </NavLink>
          ))}
        </div>
      </div>
    </nav>
  )
}

// 다른 파일에서 Navigation을 사용할 수 있도록 내보냄
export default Navigation
