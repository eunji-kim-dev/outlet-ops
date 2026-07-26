# OutletOps

아울렛 매장의 상품, 재고, 판매, 발주, 그리고 매출까지 한 곳에서 통합 관리할 수 있는 운영 시스템입니다.

각 매장의 재고 현황을 한눈에 파악할 수 있으며, 판매가 발생하면 해당 수량만큼 자동으로 재고가 차감됩니다. 발주가 입고 처리되면 재고가 다시 보충됩니다. 판매 데이터는 하루 단위로 집계되어, 대시보드에서 기간별로 간편하게 매출 현황을 확인할 수 있습니다.

특히, 재고 수량이 다양한 거래에서 동시에 변경될 수 있는 구조인 만큼, 어떠한 상황에서도 재고가 음수로 떨어지지 않도록 처리를 안정적으로 관리하는 데 중점을 두었습니다. 또한, 중간에 실패한 작업이 일부만 반영되어 데이터 일관성이 깨지는 일이 없도록 세밀하게 설계하였습니다.

## 배포 주소

- 프론트엔드: [https://outlet-ops-gamma.vercel.app](https://outlet-ops-gamma.vercel.app)
- 백엔드 Health: [https://backend-production-cd6d.up.railway.app/api/health](https://backend-production-cd6d.up.railway.app/api/health)
- Swagger UI: [https://backend-production-cd6d.up.railway.app/swagger-ui.html](https://backend-production-cd6d.up.railway.app/swagger-ui.html)

## 주요 흐름

```text
매장 및 상품 등록
→ 매장별 재고 등록·조회
→ 판매 등록
→ 판매 수량만큼 재고 자동 차감
→ 매일 자정 판매 데이터 집계
→ 대시보드에서 기간별 매출 확인
```

발주의 경우, 단순 등록 시에는 재고에 변동이 없습니다. 실제 재고에 반영되는 시점은 입고 처리 완료 후입니다.

```text
발주 등록(ORDERED)
→ 입고 처리(RECEIVED)
→ 발주 수량만큼 재고 증가

발주 등록(ORDERED)
→ 취소 처리(CANCELLED)
→ 재고에는 영향 없음
```

## 주요 기능

### 재고 관리

매장을 선택하면 해당 매장의 재고 현황을 확인하고, 직접 수량을 조정할 수 있습니다. 각 상품에는 재주문 기준이 설정되어 있어, 현재 수량이 그 기준 이하로 떨어지면 ‘부족’으로 표시됩니다. 수량은 어떤 경로로 변경되더라도 음수로 내려갈 수 없도록 처리하였습니다.

### 판매 등록

매장과 상품을 선택해서 판매를 등록하면 금액이 자동으로 계산되고, 해당 수량만큼 재고가 차감됩니다. 여러 상품을 한 번에 판매할 때는, 한 품목이라도 재고가 부족하면 전체 판매를 롤백하여, 일부만 판매된 상태로 남지 않도록 하였습니다.

### 발주 관리

매장별로 발주를 등록하고, 목록에서 상태를 관리할 수 있습니다. 입고 처리 시 발주 수량만큼 재고가 증가하며, 입고 전에는 발주를 취소할 수도 있습니다. 이미 입고된 발주를 다시 입고하거나, 취소된 발주를 입고하려는 요청은 차단됩니다.

### 매출 대시보드

기간과 매장을 선택하면 총매출, 판매 수량, 거래 건수, 건당 평균 매출을 확인할 수 있습니다. 일별 매출 추이와 매장별 매출 비교는 Recharts를 사용해 시각화하였습니다. 판매가 없는 날도 0원으로 처리하여, 그래프상에서 날짜가 건너뛰지 않도록 구현하였습니다.

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Backend | Java 17, Spring Boot 4.0.7, Spring Data JPA, Gradle |
| Database | MySQL 8 |
| Frontend | React 19, Vite 8 |
| Chart | Recharts |
| API Docs | springdoc-openapi, Swagger UI |
| Batch | Spring `@Scheduled` |
| Test | JUnit 5, Spring Boot Test, MockMvc, H2 |
| Backend Deploy | Railway, Docker |
| Frontend Deploy | Vercel |
| AI Assistant | OpenAI Codex, Claude Code(UI·CSS 디자인만) |

## 프로젝트 구조

```text
outlet-ops
├─ backend
│  ├─ src/main/java/com/outletops
│  │  ├─ store
│  │  ├─ product
│  │  ├─ inventory
│  │  ├─ sale
│  │  ├─ purchase
│  │  ├─ dashboard
│  │  └─ config
│  ├─ src/test
│  ├─ Dockerfile
│  └─ build.gradle
├─ frontend
│  ├─ src
│  │  ├─ api
│  │  ├─ components
│  │  └─ pages
│  ├─ vercel.json
│  └─ package.json
└─ docs
   ├─ AI_USAGE.md
   └─ TROUBLESHOOTING.md
```

백엔드는 계층별 구분 대신 도메인 단위로 코드를 정리했습니다. 예를 들어, `inventory` 폴더 안에는 Controller부터 DTO까지 재고 관련된 모든 코드가 모여 있어, 특정 기능을 파악할 때 여러 폴더를 이동할 필요가 없습니다. 프론트엔드는 API 호출, 재사용 컴포넌트, 그리고 주소별 페이지로 크게 세 부분으로 나누어 구성했습니다.

## 주요 API

| 기능 | Method | Endpoint |
| --- | --- | --- |
| 매장 등록·조회 | `POST`, `GET` | `/api/stores` |
| 상품 등록·조회 | `POST`, `GET` | `/api/products` |
| 재고 등록·조회 | `POST`, `GET` | `/api/inventories` |
| 재고 수량 변경 | `PATCH` | `/api/inventories/{inventoryId}/quantity` |
| 판매 등록 | `POST` | `/api/sales` |
| 발주 등록·조회 | `POST`, `GET` | `/api/purchase-orders` |
| 발주 입고 | `PATCH` | `/api/purchase-orders/{id}/receive` |
| 발주 취소 | `PATCH` | `/api/purchase-orders/{id}/cancel` |
| 판매 수동 집계 | `POST` | `/api/daily-sales/aggregate` |
| 매출 대시보드 | `GET` | `/api/dashboard/sales` |
| 서버 상태 | `GET` | `/api/health` |

자세한 요청과 응답 형식은 Swagger UI에서 확인할 수 있습니다.

## 로컬 실행

### 1. MySQL 준비

MySQL에 `outlet_ops` 데이터베이스와 애플리케이션 사용자를 준비합니다.

`backend/.env.properties.example`을 참고해 `backend/.env.properties`를 만듭니다.

```properties
DB_URL=jdbc:mysql://localhost:3306/outlet_ops?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=outletops
DB_PASSWORD=your-password
PORT=8080
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
```

실제 비밀번호가 들어간 `.env.properties`는 Git에 포함되지 않습니다.

### 2. 백엔드 실행

```powershell
cd backend
.\gradlew.bat bootRun
```

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

### 3. 프론트엔드 실행

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

- Frontend: `http://localhost:5173`
- 로컬 `/api` 요청은 Vite 프록시가 `localhost:8080`으로 전달

## 테스트

백엔드에서는 총 41개의 테스트를 진행했습니다. 단위 테스트도 일부 포함되어 있지만, 대부분은 판매, 발주, 집계 등 실제 업무 프로세스가 정상적으로 동작하는지 확인하는 통합 테스트입니다.

```powershell
cd backend
.\gradlew.bat test
```

프론트엔드는 정적 검사와 운영 빌드로 검증합니다.

```powershell
cd frontend
npm.cmd run lint
npm.cmd run build
```

테스트 외에도, 실제 MySQL 환경에서 다음과 같은 시나리오를 직접 검증했습니다.

- 정상적으로 판매가 이루어진 후 재고가 차감됨
- 재고보다 많은 수량을 판매하면 HTTP 409가 반환되며, 재고는 변하지 않음
- 발주만 등록한 경우에는 재고에 변화가 없음
- 입고 처리를 완료한 시점에만 재고가 증가함
- 중복 입고나 취소된 발주를 입고 시도하면 HTTP 409가 발생함
- 같은 날짜로 집계를 다시 실행해도 집계 데이터가 중복해서 추가되지 않음
- 집계 API 응답 합계와 대시보드의 합계가 일치함

## 배치

매일 자정(`Asia/Seoul`)을 기준으로, 전날 매장별 판매 데이터를 집계합니다. 개발 시에는 자정을 기다릴 필요 없이 아래 API를 활용해 원하는 날짜의 데이터를 즉시 집계했습니다.

```http
POST /api/daily-sales/aggregate?date=YYYY-MM-DD
```

같은 날짜로 여러 번 요청해도 집계 데이터가 중복 저장되지 않습니다.

## 배포 구성

### Railway

- MySQL과 Spring Boot를 하나의 Railway 프로젝트에 배치
- 서비스 간에는 내부 MySQL 환경변수를 참조해 연결
- Java 17 기반 멀티스테이지 Docker 빌드
- Railway 공개 도메인에 8080 포트 연결
- `/api/health` 엔드포인트로 실행 상태 확인

### Vercel

- Root Directory: `frontend`
- Framework Preset: `Vite`
- Environment Variable:

```text
VITE_API_BASE_URL=https://backend-production-cd6d.up.railway.app
```

- `vercel.json`의 SPA rewrite로 `/dashboard` 같은 주소의 직접 접속 지원

## 문서

- [AI 도구 사용 기록](docs/AI_USAGE.md)
- [트러블슈팅](docs/TROUBLESHOOTING.md)

## 미구현 항목

- 로그인 및 권한 관리 기능이 없습니다. 현재는 누구나 모든 화면을 자유롭게 볼 수 있습니다.
- 최초 매장 및 상품 등록 시 Swagger에서 API를 직접 호출해야 하며, 아직 관리 화면은 제공되지 않습니다.
- 프론트엔드 자동화 테스트가 마련되어 있지 않습니다. 현재는 lint 및 빌드만 실행하고, 화면 확인은 수동으로 하고 있습니다.
- Railway MySQL의 백업 기능은 별도로 설정하지 않은 상태입니다.