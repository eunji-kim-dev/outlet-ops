// 입력값과 저장 상태 관리를 위한 useState 가져옴
import { useState } from 'react'

// 재고 수량 변경 API 함수 가져옴
import { updateInventoryQuantity } from '../api/inventoryApi.js'

// 재고 한 건을 표시하고 수정하는 표 행 컴포넌트
function InventoryRow({ inventory, onQuantityUpdated }) {
  // 수량 입력값 저장
  const [quantity, setQuantity] = useState(inventory.quantity)

  // 수량 변경 요청 진행 여부 저장
  const [saving, setSaving] = useState(false)

  // 수량 변경 오류 메시지 저장
  const [error, setError] = useState('')

  // 수량 변경 폼 제출 처리
  async function handleSubmit(event) {
    // form 제출에 따른 페이지 새로고침 방지
    event.preventDefault()

    // 음수 수량 입력 방지
    if (Number(quantity) < 0) {
      setError('재고 수량은 0 이상이어야 함')
      return
    }

    try {
      // 저장 시작 상태 설정
      setSaving(true)

      // 이전 오류 메시지 초기화
      setError('')

      // 백엔드에 변경된 수량 전달
      const updatedInventory = await updateInventoryQuantity(
        inventory.id,
        quantity,
      )

      // 부모 컴포넌트에 변경된 재고 전달
      onQuantityUpdated(updatedInventory)
    } catch (requestError) {
      // API에서 전달한 오류 메시지 저장
      setError(requestError.message)
    } finally {
      // 성공 여부와 관계없이 저장 상태 종료
      setSaving(false)
    }
  }

  return (
    <tr>
      {/* 상품 기본 정보 */}
      <td>{inventory.sku}</td>
      <td>{inventory.productName}</td>

      {/* 재고 수량 입력과 변경 버튼 */}
      <td>
        <form onSubmit={handleSubmit}>
          <input
            type="number"
            min="0"
            value={quantity}
            onChange={(event) => setQuantity(event.target.value)}
            disabled={saving}
          />

          <button type="submit" disabled={saving}>
            {saving ? '변경 중...' : '수량 변경'}
          </button>
        </form>

        {/* 수량 변경 실패 시 오류 표시 */}
        {error && <small>{error}</small>}
      </td>

      {/* 재주문 기준 수량 */}
      <td>{inventory.reorderPoint}</td>

      {/* 현재 수량과 재주문 기준을 비교한 재고 상태 */}
      <td>{inventory.lowStock ? '부족' : '정상'}</td>
    </tr>
  )
}

// 다른 파일에서 InventoryRow를 사용할 수 있도록 내보냄
export default InventoryRow