// 목록이 비었을 때 표시할 공통 안내 컴포넌트 가져옴
import EmptyState from '../components/EmptyState.jsx'

// 대시보드에서 준비 중인 지표 항목 목록
const PLANNED_STATS = [
  { label: '오늘 매출', unit: '원' },
  { label: '이번 달 매출', unit: '원' },
  { label: '판매 건수', unit: '건' },
  { label: '재고 부족 상품', unit: '개' },
]

// 매출 대시보드 화면 컴포넌트
function DashboardPage() {
  return (
    // 매출 대시보드 화면의 주요 내용
    <main>
      {/* 대시보드 화면 제목과 설명 */}
      <header className="page-header">
        <h1>매출 대시보드</h1>
        <p className="page-description">
          기간별 매출 집계와 차트를 확인하는 화면임
        </p>
      </header>

      {/* 앞으로 표시할 지표의 자리를 미리 배치 */}
      <section className="stat-grid">
        {PLANNED_STATS.map((stat) => (
          <div className="card" key={stat.label}>
            <div className="stat-label">{stat.label}</div>
            <div className="stat-value">-{stat.unit}</div>
          </div>
        ))}
      </section>

      {/* 차트가 들어갈 영역 안내 */}
      <section>
        <h2 className="section-title">기간별 매출 추이</h2>

        <EmptyState
          message="표시할 매출 데이터가 없습니다."
          hint="매출 집계 API를 연결하면 차트가 표시됩니다."
        />
      </section>
    </main>
  )
}

// 다른 파일에서 DashboardPage를 사용할 수 있도록 내보냄
export default DashboardPage
