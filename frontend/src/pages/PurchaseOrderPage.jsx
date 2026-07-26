// React 상태 관리와 화면 실행 시점 처리를 위한 Hook 가져옴
import { useEffect, useState } from 'react'

// 매장 목록과 매장별 재고 조회 API 함수 가져옴
import {
  fetchInventories,
  fetchStores,
} from '../api/inventoryApi.js'

// 발주 등록·조회·입고·취소 API 함수 가져옴
import {
  cancelPurchaseOrder,
  createPurchaseOrder,
  fetchPurchaseOrders,
  receivePurchaseOrder,
} from '../api/purchaseOrderApi.js'

// 목록이 비었을 때 표시할 공통 안내 컴포넌트 가져옴
import EmptyState from '../components/EmptyState.jsx'

// 백엔드 발주 상태에 대응하는 한글 이름
const STATUS_LABELS = {
  ORDERED: '발주 완료',
  RECEIVED: '입고 완료',
  CANCELLED: '취소',
}

// 발주 상태마다 사용할 배지 색상 클래스
const STATUS_BADGE_CLASSES = {
  ORDERED: 'badge-info',
  RECEIVED: 'badge-success',
  CANCELLED: 'badge-neutral',
}

// 금액을 원화 형식으로 변환
function formatCurrency(amount) {
  return `${Number(amount).toLocaleString('ko-KR')}원`
}

// 날짜와 시간을 한국 형식으로 변환
function formatDateTime(dateTime) {
  return new Date(dateTime).toLocaleString('ko-KR')
}

// 발주 관리 화면 컴포넌트
function PurchaseOrderPage() {
  // 백엔드에서 받은 매장 목록 저장
  const [stores, setStores] = useState([])

  // 선택한 매장에 등록된 재고 상품 목록 저장
  const [inventories, setInventories] = useState([])

  // 선택한 매장의 발주 목록 저장
  const [purchaseOrders, setPurchaseOrders] = useState([])

  // 사용자가 선택한 매장 ID 저장
  const [selectedStoreId, setSelectedStoreId] = useState('')

  // 사용자가 선택한 상품 ID 저장
  const [selectedProductId, setSelectedProductId] = useState('')

  // 발주 수량 입력값 저장
  const [quantity, setQuantity] = useState(1)

  // 상품 한 개의 매입 단가 입력값 저장
  const [unitCost, setUnitCost] = useState('')

  // 최초 매장 목록 요청 진행 여부 저장
  const [storeLoading, setStoreLoading] = useState(true)

  // 선택한 매장 데이터 요청 진행 여부 저장
  const [dataLoading, setDataLoading] = useState(false)

  // 발주 등록 요청 진행 여부 저장
  const [submitting, setSubmitting] = useState(false)

  // 입고 또는 취소 처리 중인 발주 ID 저장
  const [processingOrderId, setProcessingOrderId] = useState(null)

  // 화면에 표시할 오류 메시지 저장
  const [error, setError] = useState('')

  // 화면에 표시할 성공 메시지 저장
  const [successMessage, setSuccessMessage] = useState('')

  // 발주 관리 화면이 처음 나타날 때 매장 목록 조회
  useEffect(() => {
    // 비동기 매장 조회 함수
    async function loadStores() {
      try {
        // 백엔드에서 매장 목록 조회
        const storeData = await fetchStores()

        // 조회한 매장 목록을 상태에 저장
        setStores(storeData)
      } catch (requestError) {
        // API에서 전달한 오류 메시지 저장
        setError(requestError.message)
      } finally {
        // 성공 여부와 관계없이 매장 로딩 종료
        setStoreLoading(false)
      }
    }

    // 매장 조회 함수 실행
    loadStores()
  }, [])

  // 매장 선택 변경 처리
  async function handleStoreChange(event) {
    // select에서 선택한 매장 ID 확인
    const storeId = event.target.value

    // 변경된 매장 ID 저장
    setSelectedStoreId(storeId)

    // 이전 입력값과 조회 결과 초기화
    setSelectedProductId('')
    setQuantity(1)
    setUnitCost('')
    setInventories([])
    setPurchaseOrders([])
    setError('')
    setSuccessMessage('')

    // 매장 미선택 시 데이터 조회 중단
    if (!storeId) {
      return
    }

    try {
      // 매장별 데이터 조회 시작 상태 설정
      setDataLoading(true)

      // 재고 상품 목록과 발주 목록을 동시에 조회
      const [inventoryData, purchaseOrderData] = await Promise.all([
        fetchInventories(storeId),
        fetchPurchaseOrders(storeId),
      ])

      // 조회 결과를 각 상태에 저장
      setInventories(inventoryData)
      setPurchaseOrders(purchaseOrderData)
    } catch (requestError) {
      // API에서 전달한 오류 메시지 저장
      setError(requestError.message)
    } finally {
      // 성공 여부와 관계없이 데이터 로딩 종료
      setDataLoading(false)
    }
  }

  // 발주 등록 폼 제출 처리
  async function handleSubmit(event) {
    // form 제출에 따른 페이지 새로고침 방지
    event.preventDefault()

    // 매장과 상품 필수 선택 확인
    if (!selectedStoreId || !selectedProductId) {
      setError('매장과 상품을 모두 선택해야 함')
      return
    }

    // 발주 수량이 양수인지 확인
    if (Number(quantity) <= 0) {
      setError('발주 수량은 1개 이상이어야 함')
      return
    }

    // 매입 단가가 양수인지 확인
    if (Number(unitCost) <= 0) {
      setError('매입 단가는 0원보다 커야 함')
      return
    }

    try {
      // 발주 등록 시작 상태 설정
      setSubmitting(true)

      // 이전 안내 메시지 초기화
      setError('')
      setSuccessMessage('')

      // 백엔드에 새로운 발주 등록
      const createdPurchaseOrder = await createPurchaseOrder(
        selectedStoreId,
        selectedProductId,
        quantity,
        unitCost,
      )

      // 최신 발주를 목록 가장 위에 추가
      setPurchaseOrders((currentPurchaseOrders) => [
        createdPurchaseOrder,
        ...currentPurchaseOrders,
      ])

      // 다음 발주 등록을 위한 입력값 초기화
      setSelectedProductId('')
      setQuantity(1)
      setUnitCost('')

      // 발주 등록 성공 메시지 설정
      setSuccessMessage(
        `발주 ${createdPurchaseOrder.id}번이 등록됨`,
      )
    } catch (requestError) {
      // 발주 등록 실패 메시지 저장
      setError(requestError.message)
    } finally {
      // 성공 여부와 관계없이 발주 등록 상태 종료
      setSubmitting(false)
    }
  }

  // 발주 목록의 특정 항목을 변경된 응답으로 교체
  function replacePurchaseOrder(updatedPurchaseOrder) {
    setPurchaseOrders((currentPurchaseOrders) =>
      currentPurchaseOrders.map((purchaseOrder) =>
        purchaseOrder.id === updatedPurchaseOrder.id
          ? updatedPurchaseOrder
          : purchaseOrder,
      ),
    )
  }

  // 발주 입고 처리
  async function handleReceive(purchaseOrderId) {
    // 사용자의 입고 처리 의사 확인
    const confirmed = window.confirm(
      '입고 처리하면 발주 수량만큼 재고가 증가합니다. 계속할까요?',
    )

    // 확인하지 않은 경우 입고 처리 중단
    if (!confirmed) {
      return
    }

    try {
      // 처리 중인 발주 ID 저장
      setProcessingOrderId(purchaseOrderId)

      // 이전 안내 메시지 초기화
      setError('')
      setSuccessMessage('')

      // 백엔드에 입고 완료 요청
      const receivedPurchaseOrder =
        await receivePurchaseOrder(purchaseOrderId)

      // 변경된 발주 상태를 목록에 반영
      replacePurchaseOrder(receivedPurchaseOrder)

      // 입고로 증가한 최신 재고 목록 조회
      const inventoryData =
        await fetchInventories(selectedStoreId)

      // 최신 재고 목록을 상태에 저장
      setInventories(inventoryData)

      // 입고 완료 메시지 설정
      setSuccessMessage(
        `발주 ${purchaseOrderId}번의 입고가 완료됨`,
      )
    } catch (requestError) {
      // 입고 처리 실패 메시지 저장
      setError(requestError.message)
    } finally {
      // 발주 처리 상태 종료
      setProcessingOrderId(null)
    }
  }

  // 발주 취소 처리
  async function handleCancel(purchaseOrderId) {
    // 사용자의 발주 취소 의사 확인
    const confirmed = window.confirm(
      '이 발주를 취소할까요?',
    )

    // 확인하지 않은 경우 취소 처리 중단
    if (!confirmed) {
      return
    }

    try {
      // 처리 중인 발주 ID 저장
      setProcessingOrderId(purchaseOrderId)

      // 이전 안내 메시지 초기화
      setError('')
      setSuccessMessage('')

      // 백엔드에 발주 취소 요청
      const cancelledPurchaseOrder =
        await cancelPurchaseOrder(purchaseOrderId)

      // 변경된 발주 상태를 목록에 반영
      replacePurchaseOrder(cancelledPurchaseOrder)

      // 취소 완료 메시지 설정
      setSuccessMessage(
        `발주 ${purchaseOrderId}번이 취소됨`,
      )
    } catch (requestError) {
      // 취소 처리 실패 메시지 저장
      setError(requestError.message)
    } finally {
      // 발주 처리 상태 종료
      setProcessingOrderId(null)
    }
  }

  return (
    <main>
      {/* 발주 관리 화면 제목과 설명 */}
      <header className="page-header">
        <h1>발주 관리</h1>
        <p className="page-description">
          상품 발주를 등록하고 입고 또는 취소 상태를 관리하는 화면임
        </p>
      </header>

      {/* 발주 대상 매장 선택 */}
      <section className="card">
        <div className="field field-narrow">
          <label htmlFor="purchase-store">매장</label>
          <select
            id="purchase-store"
            value={selectedStoreId}
            onChange={handleStoreChange}
            disabled={storeLoading || submitting}
          >
            <option value="">매장을 선택하세요</option>

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

        {/* 매장 선택 후 발주 입력 폼 표시 */}
        {selectedStoreId && (
          <form className="form-row card-divider" onSubmit={handleSubmit}>
            {/* 발주할 재고 상품 선택 */}
            <div className="field">
              <label htmlFor="purchase-product">상품</label>
              <select
                id="purchase-product"
                value={selectedProductId}
                onChange={(event) =>
                  setSelectedProductId(event.target.value)
                }
                disabled={dataLoading || submitting}
              >
                <option value="">상품을 선택하세요</option>

                {/* 매장에 등록된 재고를 상품 선택 항목으로 변환 */}
                {inventories.map((inventory) => (
                  <option
                    key={inventory.id}
                    value={inventory.productId}
                  >
                    {inventory.productName} ({inventory.sku})
                  </option>
                ))}
              </select>
            </div>

            {/* 발주 상품 수량 입력 */}
            <div className="field">
              <label htmlFor="purchase-quantity">발주 수량</label>
              <input
                id="purchase-quantity"
                type="number"
                min="1"
                value={quantity}
                onChange={(event) =>
                  setQuantity(event.target.value)
                }
                disabled={submitting}
              />
            </div>

            {/* 상품 한 개의 매입 단가 입력 */}
            <div className="field">
              <label htmlFor="purchase-unit-cost">매입 단가</label>
              <input
                id="purchase-unit-cost"
                type="number"
                min="1"
                step="0.01"
                value={unitCost}
                onChange={(event) =>
                  setUnitCost(event.target.value)
                }
                placeholder="예: 15000"
                disabled={submitting}
              />
            </div>

            {/* 발주 등록 요청 버튼 */}
            <div className="field field-action">
              <button
                className="btn btn-primary"
                type="submit"
                disabled={submitting || dataLoading}
              >
                {submitting ? '등록 중...' : '발주 등록'}
              </button>
            </div>
          </form>
        )}

        {/* 매장 데이터 조회 중 표시 */}
        {dataLoading && (
          <p className="loading-note hint">
            발주 정보를 불러오는 중...
          </p>
        )}
      </section>

      {/* 요청 처리 중 발생한 오류 표시 */}
      {error && <p className="alert alert-error">{error}</p>}

      {/* 발주 등록과 상태 변경 성공 메시지 표시 */}
      {successMessage && (
        <p className="alert alert-success">{successMessage}</p>
      )}

      {/* 매장 선택 후 발주 목록 영역 표시 */}
      {selectedStoreId && !dataLoading && (
        <section>
          <h2 className="section-title">발주 목록</h2>

          {/* 등록된 발주가 없는 경우 안내 표시 */}
          {purchaseOrders.length === 0 ? (
            <EmptyState
              message="등록된 발주가 없습니다."
              hint="위 폼에서 첫 발주를 등록해 보세요."
            />
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>발주 ID</th>
                    <th>발주 시각</th>
                    <th>상품</th>
                    <th>총금액</th>
                    <th>상태</th>
                    <th>처리</th>
                  </tr>
                </thead>

                <tbody>
                  {/* 발주 배열의 각 항목을 표의 행으로 변환 */}
                  {purchaseOrders.map((purchaseOrder) => (
                    <tr key={purchaseOrder.id}>
                      <td className="cell-number" data-label="발주 ID">
                        {purchaseOrder.id}
                      </td>
                      <td className="cell-tight" data-label="발주 시각">
                        {formatDateTime(purchaseOrder.orderedAt)}
                      </td>
                      <td data-label="상품">
                        {purchaseOrder.items.map((item) => (
                          <div
                            className="order-item"
                            key={item.productId}
                          >
                            {item.productName} {item.quantity}개
                            {' × '}
                            {formatCurrency(item.unitCost)}
                          </div>
                        ))}
                      </td>
                      <td className="cell-number cell-tight" data-label="총금액">
                        {formatCurrency(purchaseOrder.totalAmount)}
                      </td>
                      <td data-label="상태">
                        <span
                          className={`badge ${STATUS_BADGE_CLASSES[purchaseOrder.status]}`}
                        >
                          {STATUS_LABELS[purchaseOrder.status]}
                        </span>
                      </td>
                      <td data-label="처리">
                        {/* 처리 전 발주에만 입고와 취소 버튼 표시 */}
                        {purchaseOrder.status === 'ORDERED' ? (
                          <div className="button-group">
                            <button
                              className="btn btn-success btn-sm"
                              type="button"
                              onClick={() =>
                                handleReceive(purchaseOrder.id)
                              }
                              disabled={
                                processingOrderId === purchaseOrder.id
                              }
                            >
                              입고
                            </button>
                            <button
                              className="btn btn-danger btn-sm"
                              type="button"
                              onClick={() =>
                                handleCancel(purchaseOrder.id)
                              }
                              disabled={
                                processingOrderId === purchaseOrder.id
                              }
                            >
                              취소
                            </button>
                          </div>
                        ) : (
                          <span className="cell-empty">-</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}
    </main>
  )
}

// 다른 파일에서 PurchaseOrderPage를 사용할 수 있도록 내보냄
export default PurchaseOrderPage
