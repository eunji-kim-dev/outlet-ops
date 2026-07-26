// React 상태 관리와 화면 실행 시점 처리를 위한 Hook 가져옴
import { useEffect, useState } from 'react'

// 매장 목록과 매장별 재고 조회 API 함수 가져옴
import {
  fetchInventories,
  fetchStores,
} from '../api/inventoryApi.js'

// 재고 한 건의 조회와 수정을 담당하는 표 행 컴포넌트 가져옴
import InventoryRow from '../components/InventoryRow.jsx'

// 재고 관리 화면 컴포넌트
function InventoryPage() {
  // 백엔드에서 받은 매장 목록 저장
  const [stores, setStores] = useState([])

  // 사용자가 선택한 매장 ID 저장
  const [selectedStoreId, setSelectedStoreId] = useState('')

  // API 요청 진행 여부 저장
  const [loading, setLoading] = useState(true)

  // API 요청 중 발생한 오류 메시지 저장
  const [error, setError] = useState('')

  // 선택한 매장의 재고 목록 저장
  const [inventories, setInventories] = useState([])

  // 재고 목록 요청 진행 여부 저장
  const [inventoryLoading, setInventoryLoading] = useState(false)

  // 재고 관리 화면이 처음 나타날 때 매장 목록 조회
  useEffect(() => {
    // 비동기 매장 조회 함수
    async function loadStores() {
      try {
        // 이전 오류 메시지 초기화
        setError('')

        // 백엔드에서 매장 목록 조회
        const storeData = await fetchStores()

        // 조회한 매장 목록을 상태에 저장
        setStores(storeData)
      } catch (requestError) {
        // API에서 전달한 오류 메시지 저장
        setError(requestError.message)
      } finally {
        // 성공 여부와 관계없이 로딩 종료
        setLoading(false)
      }
    }

    // 매장 조회 함수 실행
    loadStores()
  }, [])

  // 선택한 매장이 변경될 때 해당 매장의 재고 조회
useEffect(() => {
  // 선택한 매장이 없으면 재고 목록 초기화
  if (!selectedStoreId) {
    return
  }

  // 비동기 재고 조회 함수
  async function loadInventories() {
    try {
      // 재고 조회 시작 상태 설정
      setInventoryLoading(true)

      // 이전 오류 메시지 초기화
      setError('')

      // 선택한 매장의 재고 목록 조회
      const inventoryData = await fetchInventories(selectedStoreId)

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

  // 재고 조회 함수 실행
  loadInventories()
}, [selectedStoreId])

// 변경된 재고 한 건을 전체 재고 목록에 반영
function handleQuantityUpdated(updatedInventory) {
  // 현재 재고 목록을 새로운 배열로 변경
  setInventories((currentInventories) =>
    currentInventories.map((inventory) =>
      // 변경된 재고와 ID가 같으면 새로운 데이터로 교체
      inventory.id === updatedInventory.id
        ? updatedInventory
        : inventory,
    ),
  )
}

  return (
    <main>
      {/* 재고 관리 화면 제목 */}
      <h1>재고 관리</h1>

      {/* 재고를 조회할 매장 선택 영역 */}
      <section>
        <label htmlFor="store">매장 선택</label>

        <select
          id="store"
          value={selectedStoreId}
          onChange={(event) => setSelectedStoreId(event.target.value)}
          disabled={loading}
        >
          {/* 아무 매장도 선택하지 않은 초기 항목 */}
          <option value="">매장을 선택하세요</option>

          {/* 매장 배열을 option 요소 배열로 변환 */}
          {stores.map((store) => (
            <option key={store.id} value={store.id}>
              {store.name}
            </option>
          ))}
        </select>
      </section>

      {/* 매장 목록 조회 중 표시 */}
      {loading && <p>매장 목록을 불러오는 중...</p>}

      {/* 매장 목록 조회 실패 시 오류 표시 */}
      {error && <p>{error}</p>}

      {/* 재고 목록 조회 중 표시 */}
{inventoryLoading && <p>재고 목록을 불러오는 중...</p>}

{/* 매장이 선택되고 재고 조회가 끝난 경우 목록 표시 */}
{selectedStoreId && !inventoryLoading && (
  <section>
    <h2>상품 재고 목록</h2>

    {/* 등록된 재고가 없는 경우 안내 표시 */}
    {inventories.length === 0 ? (
      <p>등록된 재고가 없습니다.</p>
    ) : (
      // 재고가 있는 경우 표 표시
      <table>
        <thead>
          <tr>
            <th>SKU</th>
            <th>상품명</th>
            <th>현재 수량</th>
            <th>재주문 기준</th>
            <th>재고 상태</th>
          </tr>
        </thead>

        <tbody>
          {/* 재고마다 InventoryRow 컴포넌트 한 개 생성 */}
            {inventories.map((inventory) => (
            <InventoryRow
              key={inventory.id}
              inventory={inventory}
              onQuantityUpdated={handleQuantityUpdated}
            />
          ))}
        </tbody>
      </table>
    )}
  </section>
)}
    </main>
  )
}

// 다른 파일에서 InventoryPage를 사용할 수 있도록 내보냄
export default InventoryPage