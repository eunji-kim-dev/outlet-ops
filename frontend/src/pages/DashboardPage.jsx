// React 상태 관리와 화면 실행 시점 처리를 위한 Hook 가져옴
import { useEffect, useState } from 'react'

// 매출 추이와 매장 비교 차트를 위한 Recharts 컴포넌트 가져옴
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

// 대시보드와 매장 목록 조회 API 함수 가져옴
import { fetchSalesDashboard } from '../api/dashboardApi.js'
import { fetchStores } from '../api/inventoryApi.js'

// 데이터가 없을 때 사용할 공통 안내 컴포넌트 가져옴
import EmptyState from '../components/EmptyState.jsx'

// Date 객체를 날짜 입력창의 YYYY-MM-DD 형식으로 변환
function formatDateInput(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

// 대시보드 최초 조회 기간 생성
function createInitialDateRange() {
  // 오늘 날짜 생성
  const end = new Date()

  // 오늘을 포함한 최근 30일의 시작 날짜 생성
  const start = new Date()
  start.setDate(start.getDate() - 29)

  return {
    startDate: formatDateInput(start),
    endDate: formatDateInput(end),
  }
}

// 금액을 원화 형식으로 변환
function formatCurrency(amount) {
  return `${Number(amount).toLocaleString('ko-KR')}원`
}

// 큰 금액을 차트 축에 맞는 짧은 형식으로 변환
function formatCompactCurrency(amount) {
  const number = Number(amount)

  if (number >= 100000000) {
    return `${(number / 100000000).toFixed(1)}억`
  }

  if (number >= 10000) {
    return `${Math.round(number / 10000)}만`
  }

  return number.toLocaleString('ko-KR')
}

// YYYY-MM-DD 날짜를 차트용 MM.DD 형식으로 변환
function formatChartDate(date) {
  return `${date.slice(5, 7)}.${date.slice(8, 10)}`
}

// 매출 대시보드 화면 컴포넌트
function DashboardPage() {
  // 최초 조회 기간을 한 번만 생성
  const [initialDateRange] = useState(createInitialDateRange)

  // 백엔드에서 받은 매장 목록 저장
  const [stores, setStores] = useState([])

  // 조회 시작일 저장
  const [startDate, setStartDate] = useState(
    initialDateRange.startDate,
  )

  // 조회 종료일 저장
  const [endDate, setEndDate] = useState(initialDateRange.endDate)

  // 선택한 매장 ID 저장
  const [selectedStoreId, setSelectedStoreId] = useState('')

  // 조회한 대시보드 데이터 저장
  const [dashboard, setDashboard] = useState(null)

  // 대시보드 요청 진행 여부 저장
  const [loading, setLoading] = useState(true)

  // 화면에 표시할 오류 메시지 저장
  const [error, setError] = useState('')

  // 화면 최초 진입 시 매장과 최근 30일 대시보드 동시 조회
  useEffect(() => {
    // 최초 화면 데이터 조회 함수
    async function loadInitialData() {
      try {
        // 매장 목록과 전체 매출 데이터를 동시에 조회
        const [storeData, dashboardData] = await Promise.all([
          fetchStores(),
          fetchSalesDashboard(
            initialDateRange.startDate,
            initialDateRange.endDate,
            '',
          ),
        ])

        // 조회한 매장과 대시보드 데이터를 상태에 저장
        setStores(storeData)
        setDashboard(dashboardData)
      } catch (requestError) {
        // API에서 전달한 오류 메시지 저장
        setError(requestError.message)
      } finally {
        // 성공 여부와 관계없이 로딩 종료
        setLoading(false)
      }
    }

    // 최초 화면 데이터 조회 실행
    loadInitialData()
  }, [initialDateRange])

  // 기간과 매장 조건에 맞는 대시보드 조회
  async function handleSearch(event) {
    // form 제출에 따른 페이지 새로고침 방지
    event.preventDefault()

    // 종료일이 시작일보다 빠른 입력 방지
    if (endDate < startDate) {
      setError('종료일은 시작일보다 빠를 수 없음')
      return
    }

    try {
      // 대시보드 조회 시작 상태 설정
      setLoading(true)

      // 이전 오류 메시지 초기화
      setError('')

      // 선택한 필터로 매출 대시보드 조회
      const dashboardData = await fetchSalesDashboard(
        startDate,
        endDate,
        selectedStoreId,
      )

      // 조회한 대시보드 데이터를 상태에 저장
      setDashboard(dashboardData)
    } catch (requestError) {
      // API에서 전달한 오류 메시지 저장
      setError(requestError.message)
    } finally {
      // 성공 여부와 관계없이 로딩 종료
      setLoading(false)
    }
  }

  // 일별 API 응답을 Recharts 데이터 형식으로 변환
  const dailyChartData =
    dashboard?.dailySales.map((dailySale) => ({
      date: dailySale.date,
      dateLabel: formatChartDate(dailySale.date),
      revenue: Number(dailySale.totalRevenue),
      quantity: dailySale.totalQuantity,
      transactions: dailySale.transactionCount,
    })) ?? []

  // 매장별 API 응답을 Recharts 데이터 형식으로 변환
  const storeChartData =
    dashboard?.storeSales.map((storeSale) => ({
      storeName: storeSale.storeName,
      revenue: Number(storeSale.totalRevenue),
    })) ?? []

  // 실제 판매 집계 데이터 존재 여부 확인
  const hasSales = dashboard && dashboard.transactionCount > 0

  // 거래 한 건당 평균 매출 계산
  const averageTransactionAmount =
    dashboard && dashboard.transactionCount > 0
      ? Number(dashboard.totalRevenue) / dashboard.transactionCount
      : 0

  return (
    <main>
      {/* 대시보드 화면 제목과 설명 */}
      <header className="page-header">
        <h1>매출 대시보드</h1>
        <p className="page-description">
          기간별 핵심 매출 지표와 추이를 확인하는 화면임
        </p>
      </header>

      {/* 조회 기간과 매장 필터 */}
      <section className="card">
        <form className="form-row dashboard-filter" onSubmit={handleSearch}>
          {/* 조회 시작 날짜 입력 */}
          <div className="field">
            <label htmlFor="dashboard-start-date">시작일</label>
            <input
              id="dashboard-start-date"
              type="date"
              value={startDate}
              onChange={(event) => setStartDate(event.target.value)}
              disabled={loading}
            />
          </div>

          {/* 조회 종료 날짜 입력 */}
          <div className="field">
            <label htmlFor="dashboard-end-date">종료일</label>
            <input
              id="dashboard-end-date"
              type="date"
              value={endDate}
              onChange={(event) => setEndDate(event.target.value)}
              disabled={loading}
            />
          </div>

          {/* 전체 매장 또는 특정 매장 선택 */}
          <div className="field">
            <label htmlFor="dashboard-store">매장</label>
            <select
              id="dashboard-store"
              value={selectedStoreId}
              onChange={(event) =>
                setSelectedStoreId(event.target.value)
              }
              disabled={loading}
            >
              <option value="">전체 매장</option>

              {/* 활성화된 매장만 선택 항목으로 변환 */}
              {stores
                .filter((store) => store.active)
                .map((store) => (
                  <option key={store.id} value={store.id}>
                    {store.name}
                  </option>
                ))}
            </select>
          </div>

          {/* 대시보드 조회 요청 버튼 */}
          <div className="field field-action">
            <button
              className="btn btn-primary"
              type="submit"
              disabled={loading}
            >
              {loading ? '조회 중...' : '조회'}
            </button>
          </div>
        </form>
      </section>

      {/* 대시보드 조회 실패 메시지 */}
      {error && <p className="alert alert-error">{error}</p>}

      {/* 대시보드 조회 중 표시 */}
      {loading && (
        <p className="loading-note">
          매출 데이터를 불러오는 중...
        </p>
      )}

      {/* 조회 완료 후 핵심 지표와 차트 표시 */}
      {!loading && dashboard && (
        <>
          {/* 조회 기간의 핵심 매출 지표 */}
          <section className="stat-grid">
            <article className="card stat-card">
              <div className="stat-label">총매출</div>
              <div className="stat-value stat-value-brand">
                {formatCurrency(dashboard.totalRevenue)}
              </div>
            </article>

            <article className="card stat-card">
              <div className="stat-label">판매 수량</div>
              <div className="stat-value">
                {dashboard.totalQuantity.toLocaleString('ko-KR')}개
              </div>
            </article>

            <article className="card stat-card">
              <div className="stat-label">거래 건수</div>
              <div className="stat-value">
                {dashboard.transactionCount.toLocaleString('ko-KR')}건
              </div>
            </article>

            <article className="card stat-card">
              <div className="stat-label">건당 평균 매출</div>
              <div className="stat-value">
                {formatCurrency(
                  Math.round(averageTransactionAmount),
                )}
              </div>
            </article>
          </section>

          {/* 집계된 매출이 없는 경우 안내 */}
          {!hasSales ? (
            <EmptyState
              message="선택한 기간에 집계된 매출이 없습니다."
              hint="판매 집계가 완료된 날짜와 매장을 확인해 주세요."
            />
          ) : (
            <>
              {/* 날짜별 매출 추이 영역 차트 */}
              <section className="card chart-card">
                <div className="chart-header">
                  <div>
                    <h2 className="card-title">일별 매출 추이</h2>
                    <p>선택한 기간의 날짜별 매출 변화</p>
                  </div>
                </div>

                <div
                  className="chart-container"
                  role="img"
                  aria-label="일별 매출 추이 차트"
                >
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart
                      data={dailyChartData}
                      margin={{ top: 10, right: 12, left: 4, bottom: 0 }}
                    >
                      <defs>
                        {/* 그래프 아래쪽을 채우는 옅은 그라데이션 */}
                        <linearGradient
                          id="revenueFill"
                          x1="0"
                          y1="0"
                          x2="0"
                          y2="1"
                        >
                          <stop
                            offset="5%"
                            stopColor="var(--brand-from)"
                            stopOpacity={0.35}
                          />
                          <stop
                            offset="95%"
                            stopColor="var(--brand-to)"
                            stopOpacity={0.02}
                          />
                        </linearGradient>

                        {/* 그래프 선 자체에 들어가는 좌우 그라데이션 */}
                        <linearGradient
                          id="revenueStroke"
                          x1="0"
                          y1="0"
                          x2="1"
                          y2="0"
                        >
                          <stop offset="0%" stopColor="var(--brand-from)" />
                          <stop offset="100%" stopColor="var(--brand-to)" />
                        </linearGradient>
                      </defs>
                      <CartesianGrid
                        stroke="var(--border)"
                        strokeDasharray="3 3"
                        vertical={false}
                      />
                      <XAxis
                        dataKey="dateLabel"
                        stroke="var(--text-3)"
                        tickLine={false}
                        axisLine={false}
                        minTickGap={24}
                      />
                      <YAxis
                        stroke="var(--text-3)"
                        tickLine={false}
                        axisLine={false}
                        tickFormatter={formatCompactCurrency}
                        width={58}
                      />
                      <Tooltip
                        formatter={(value) => [
                          formatCurrency(value),
                          '매출',
                        ]}
                        labelFormatter={(_, payload) =>
                          payload[0]?.payload.date ?? ''
                        }
                        contentStyle={{
                          borderColor: 'var(--border)',
                          borderRadius: '8px',
                          backgroundColor: 'var(--surface)',
                          color: 'var(--text)',
                        }}
                      />
                      <Area
                        type="monotone"
                        dataKey="revenue"
                        stroke="url(#revenueStroke)"
                        strokeWidth={2.5}
                        fill="url(#revenueFill)"
                        activeDot={{ r: 5, fill: 'var(--brand-to)' }}
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              </section>

              {/* 전체 매장 조회 시 매장별 매출 비교 표시 */}
              {!selectedStoreId && storeChartData.length > 0 && (
                <section className="card chart-card">
                  <div className="chart-header">
                    <div>
                      <h2 className="card-title">매장별 매출 비교</h2>
                      <p>조회 기간 동안의 매장별 총매출</p>
                    </div>
                  </div>

                  <div
                    className="chart-container chart-container-small"
                    role="img"
                    aria-label="매장별 매출 비교 차트"
                  >
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart
                        data={storeChartData}
                        margin={{ top: 10, right: 12, left: 4, bottom: 0 }}
                      >
                        <defs>
                          {/* 막대마다 위에서 아래로 이어지는 그라데이션 */}
                          <linearGradient
                            id="storeBarFill"
                            x1="0"
                            y1="0"
                            x2="0"
                            y2="1"
                          >
                            <stop offset="0%" stopColor="var(--brand-from)" />
                            <stop offset="100%" stopColor="var(--brand-to)" />
                          </linearGradient>
                        </defs>
                        <CartesianGrid
                          stroke="var(--border)"
                          strokeDasharray="3 3"
                          vertical={false}
                        />
                        <XAxis
                          dataKey="storeName"
                          stroke="var(--text-3)"
                          tickLine={false}
                          axisLine={false}
                        />
                        <YAxis
                          stroke="var(--text-3)"
                          tickLine={false}
                          axisLine={false}
                          tickFormatter={formatCompactCurrency}
                          width={58}
                        />
                        <Tooltip
                          formatter={(value) => [
                            formatCurrency(value),
                            '매출',
                          ]}
                          contentStyle={{
                            borderColor: 'var(--border)',
                            borderRadius: '8px',
                            backgroundColor: 'var(--surface)',
                            color: 'var(--text)',
                          }}
                        />
                        <Bar
                          dataKey="revenue"
                          fill="url(#storeBarFill)"
                          radius={[5, 5, 0, 0]}
                          maxBarSize={64}
                        />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </section>
              )}
            </>
          )}
        </>
      )}
    </main>
  )
}

// 다른 파일에서 DashboardPage를 사용할 수 있도록 내보냄
export default DashboardPage
