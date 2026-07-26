// React 상태 관리와 화면 실행 시점 처리를 위한 Hook 가져옴
import { useEffect, useState } from 'react'

// 매장 목록과 매장별 재고 조회 API 함수 가져옴
import {
  fetchInventories,
  fetchStores,
} from '../api/inventoryApi.js'

// 판매 등록 API 함수 가져옴
import { createSale } from '../api/saleApi.js'

// 판매 등록 화면 컴포넌트
function SalesPage() {
  // 백엔드에서 받은 매장 목록 저장
  const [stores, setStores] = useState([])

  // 선택한 매장의 재고 목록 저장
  const [inventories, setInventories] = useState([])

  // 사용자가 선택한 매장 ID 저장
  const [selectedStoreId, setSelectedStoreId] = useState('')

  // 사용자가 선택한 상품 ID 저장
  const [selectedProductId, setSelectedProductId] = useState('')

  // 판매 수량 입력값 저장
  const [quantity, setQuantity] = useState(1)

  // 매장 목록 요청 진행 여부 저장
  const [storeLoading, setStoreLoading] = useState(true)

  // 재고 목록 요청 진행 여부 저장
  const [inventoryLoading, setInventoryLoading] = useState(false)

  // 판매 등록 요청 진행 여부 저장
  const [submitting, setSubmitting] = useState(false)

  // 화면에 표시할 오류 메시지 저장
  const [error, setError] = useState('')

  // 등록 완료된 판매 정보 저장
  const [saleResult, setSaleResult] = useState(null)

  // 선택한 상품의 현재 재고 정보 검색
  const selectedInventory = inventories.find(
    (inventory) => String(inventory.productId) === selectedProductId,
  )

  // 판매 등록 화면이 처음 나타날 때 매장 목록 조회
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

    // 이전 상품 선택과 판매 결과 초기화
    setSelectedProductId('')
    setQuantity(1)
    setInventories([])
    setSaleResult(null)
    setError('')

    // 매장 미선택 시 재고 조회 중단
    if (!storeId) {
      return
    }

    try {
      // 재고 조회 시작 상태 설정
      setInventoryLoading(true)

      // 선택한 매장의 재고 목록 조회
      const inventoryData = await fetchInventories(storeId)

      // 조회한 재고 목록을 상태에 저장
      setInventories(inventoryData)
    } catch (requestError) {
      // API에서 전달한 오류 메시지 저장
      setError(requestError.message)
    } finally {
      // 성공 여부와 관계없이 재고 로딩 종료
      setInventoryLoading(false)
    }
  }

  // 판매 등록 폼 제출 처리
  async function handleSubmit(event) {
    // form 제출에 따른 페이지 새로고침 방지
    event.preventDefault()

    // 매장과 상품 필수 선택 확인
    if (!selectedStoreId || !selectedProductId) {
      setError('매장과 상품을 모두 선택해야 함')
      return
    }

    // 판매 수량이 양수인지 확인
    if (Number(quantity) <= 0) {
      setError('판매 수량은 1개 이상이어야 함')
      return
    }

    // 현재 재고보다 많은 판매 수량 입력 방지
    if (selectedInventory && Number(quantity) > selectedInventory.quantity) {
      setError('판매 수량이 현재 재고보다 많음')
      return
    }

    try {
      // 판매 등록 시작 상태 설정
      setSubmitting(true)

      // 이전 오류와 판매 결과 초기화
      setError('')
      setSaleResult(null)

      // 백엔드에 판매 데이터 등록
      const createdSale = await createSale(
        selectedStoreId,
        selectedProductId,
        quantity,
      )

      // 등록 완료된 판매 정보 저장
      setSaleResult(createdSale)

      // 입력값을 다음 판매 등록을 위한 초기 상태로 변경
      setSelectedProductId('')
      setQuantity(1)

      try {
        // 판매 후 차감된 최신 재고 목록 조회
        const inventoryData = await fetchInventories(selectedStoreId)

        // 최신 재고 목록을 화면에 반영
        setInventories(inventoryData)
      } catch {
        // 판매 성공 후 재고 목록 갱신만 실패한 경우 안내
        setError('판매는 완료됐지만 최신 재고를 불러오지 못함')
      }
    } catch (requestError) {
      // 판매 등록 실패 메시지 저장
      setError(requestError.message)
    } finally {
      // 성공 여부와 관계없이 판매 등록 상태 종료
      setSubmitting(false)
    }
  }

  return (
    <main>
      {/* 판매 등록 화면 제목과 설명 */}
      <h1>판매 등록</h1>
      <p className="page-description">
        매장과 상품을 선택해 판매를 등록하고 재고를 차감하는 화면임
      </p>

      {/* 판매 정보 입력 영역 */}
      <section className="form-card">
        <form className="sale-form" onSubmit={handleSubmit}>
          {/* 판매가 발생한 매장 선택 */}
          <div className="field">
            <label htmlFor="sale-store">매장</label>
            <select
              id="sale-store"
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

          {/* 선택한 매장에 등록된 상품 선택 */}
          <div className="field">
            <label htmlFor="sale-product">상품</label>
            <select
              id="sale-product"
              value={selectedProductId}
              onChange={(event) =>
                setSelectedProductId(event.target.value)
              }
              disabled={
                !selectedStoreId ||
                inventoryLoading ||
                submitting
              }
            >
              <option value="">상품을 선택하세요</option>

              {/* 매장의 재고 목록을 상품 선택 항목으로 변환 */}
              {inventories.map((inventory) => (
                <option
                  key={inventory.id}
                  value={inventory.productId}
                  disabled={inventory.quantity === 0}
                >
                  {inventory.productName} ({inventory.sku}) - 재고{' '}
                  {inventory.quantity}개
                </option>
              ))}
            </select>
          </div>

          {/* 판매할 상품 수량 입력 */}
          <div className="field">
            <label htmlFor="sale-quantity">판매 수량</label>
            <input
              id="sale-quantity"
              type="number"
              min="1"
              max={
                selectedInventory
                  ? selectedInventory.quantity
                  : undefined
              }
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
              disabled={!selectedProductId || submitting}
            />
          </div>

          {/* 판매 등록 요청 버튼 */}
          <button
            className="primary-button"
            type="submit"
            disabled={submitting}
          >
            {submitting ? '등록 중...' : '판매 등록'}
          </button>
        </form>

        {/* 선택 상품의 현재 재고 표시 */}
        {selectedInventory && (
          <p className="stock-hint">
            현재 재고: {selectedInventory.quantity}개
          </p>
        )}

        {/* 선택한 매장의 재고 조회 중 표시 */}
        {inventoryLoading && (
          <p className="info-message">상품 재고를 불러오는 중...</p>
        )}

        {/* 선택한 매장에 등록된 재고가 없는 경우 표시 */}
        {selectedStoreId &&
          !inventoryLoading &&
          inventories.length === 0 && (
            <p className="info-message">
              이 매장에 등록된 상품 재고가 없습니다.
            </p>
          )}
      </section>

      {/* 요청 처리 중 발생한 오류 표시 */}
      {error && <p className="error-message">{error}</p>}

      {/* 판매 등록 성공 결과 표시 */}
      {saleResult && (
        <section className="result-card">
          <h2>판매 등록 완료</h2>
          <dl>
            <div>
              <dt>판매 ID</dt>
              <dd>{saleResult.id}</dd>
            </div>
            <div>
              <dt>매장</dt>
              <dd>{saleResult.storeName}</dd>
            </div>
            <div>
              <dt>결제 금액</dt>
              <dd>
                {Number(saleResult.totalAmount).toLocaleString('ko-KR')}원
              </dd>
            </div>
          </dl>
        </section>
      )}
    </main>
  )
}

// 다른 파일에서 SalesPage를 사용할 수 있도록 내보냄
export default SalesPage
