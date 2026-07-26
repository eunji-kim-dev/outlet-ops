// 보여줄 데이터가 없을 때 화면에 표시하는 공통 안내 영역
function EmptyState({ message, hint }) {
  return (
    <div className="empty-state">
      {/* 빈 상자를 뜻하는 안내 아이콘 */}
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <path d="M3 9l2-5h14l2 5M3 9v11h18V9M3 9h18M9 13h6" />
      </svg>

      {/* 비어 있는 이유를 알려주는 문구 */}
      <span>{message}</span>

      {/* 다음에 할 일을 알려주는 보조 문구 */}
      {hint && <small>{hint}</small>}
    </div>
  )
}

// 다른 파일에서 EmptyState를 사용할 수 있도록 내보냄
export default EmptyState
