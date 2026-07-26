# 트러블슈팅

구현 및 배포 과정에서 실제로 겪은 문제들과 해결 방법을 정리했습니다.

## 1. Spring Boot 4에서 `AutoConfigureMockMvc`를 찾을 수 없음

MockMvc를 이용해 통합 테스트를 작성하려 했으나, import 단계에서부터 컴파일이 되지 않는 문제가 발생했습니다. 이는 Spring Boot 4에서 해당 어노테이션의 패키지 경로가 3.x와 달라진 것이 원인이었습니다.

```java
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
```

경로를 위와 같이 수정하니 문제가 해결되었고, MockMvc 기반의 API 검증을 포함해 백엔드 테스트 41개가 정상적으로 통과했습니다.

## 2. `useEffect` 내에서 바로 상태를 변경해 ESLint 오류 발생

재고 화면에서 아래 코드 때문에 `react-hooks/set-state-in-effect` 규칙에 위반된다는 ESLint 오류가 발생했습니다.

```jsx
if (!selectedStoreId) {
  setInventories([])
  return
}
```

최신 React ESLint 규칙은 Effect 내에서의 즉시 상태 변경을 연속 렌더링을 유발할 수 있는 코드로 판단합니다. 코드를 다시 확인해보니, 매장이 선택되지 않은 경우 이미 조건부 렌더링으로 표 자체가 숨겨지므로 배열을 굳이 비울 필요가 없었습니다.

```jsx
if (!selectedStoreId) {
  return
}
```

Effect는 API 호출만 제어하고, 화면 표시는 기존 조건부 렌더링에 맡기도록 코드를 단순화했습니다. 규칙을 끄는 대신 불필요한 렌더링 자체를 없애는 방향으로 해결했습니다.

## 3. Recharts 추가 후 초기 번들 크기 500KB 초과

대시보드에 Recharts를 추가한 뒤, Vite 빌드 시 번들 크기 경고가 발생했습니다. 재고나 판매 화면을 열 때도 차트 라이브러리 전체가 함께 로드되는 구조였습니다.

이 문제를 해결하기 위해 대시보드 페이지만 `lazy`와 `Suspense`로 코드 분할했습니다.

```jsx
const DashboardPage = lazy(() => import('./pages/DashboardPage.jsx'))
```

이후 실제로 대시보드 메뉴로 진입할 때만 해당 번들을 받아오도록 변경했고, 경고 메시지도 더 이상 나타나지 않았습니다.

```text
초기 화면 번들: 약 249KB
대시보드 번들: 약 382KB
```

## 4. Vercel 배포 시 API 주소 및 새로고침 문제

로컬 환경에서는 Vite 프록시 설정 덕분에 `/api`로 들어오는 요청이 자동으로 `localhost:8080`으로 전달됩니다. 그러나 Vercel에는 별도의 Spring Boot 서버가 없으므로, 같은 설정을 그대로 두면 API 요청이 갈 곳을 잃습니다.  
또, `/dashboard`와 같이 특정 경로로 직접 접속하면, Vercel은 해당 경로의 정적 파일을 찾으려다 404 에러가 발생할 수 있습니다. 정적 호스팅에서는 SPA의 모든 경로를 자동으로 `index.html`에 연결해주지 않기 때문에 생기는 문제입니다.

이에 따라 운영 API 주소를 환경변수로 분리해 Vercel에 등록했습니다. Vite 환경변수는 빌드 시점에 번들 코드로 그대로 들어가기 때문에, 빌드 전에 아래 값을 Vercel에 설정해두어야 합니다.

```text
VITE_API_BASE_URL=https://backend-production-cd6d.up.railway.app
```

새로고침 문제는 `vercel.json`에서 rewrite 규칙을 추가하는 방식으로 해결했습니다.

```json
{
  "rewrites": [
    {
      "source": "/(.*)",
      "destination": "/index.html"
    }
  ]
}
```

또, 백엔드의 CORS 허용 목록에 Vercel 운영 도메인을 정확히 추가했습니다. 실제로 배포 후 번들에는 Railway API 주소가 들어간 것을 확인했고, preflight 응답의 `Access-Control-Allow-Origin` 역시 Vercel 주소와 일치함을 검증했습니다.

## 5. Railway GitHub 소스 배포가 시작되지 않음

이 부분에서 가장 시간이 많이 소요되었습니다. Railway 대시보드에서는 GitHub 저장소가 정상적으로 연결되어 있고, 프로젝트 루트 디렉터리 또한 `/backend`로 올바르게 인식되는 것처럼 보였습니다. 하지만 배포 버튼을 누르면 바로 아래와 같은 에러 메시지만 출력되며 진행이 되지 않았습니다.

```text
There was an error deploying from source.
```

빌드 로그조차 생성되지 않아 원인 파악에 어려움이 있었습니다. 실제 문제는 코드가 아니라 Railway 프로젝트 구조에 있었습니다. MySQL 서비스는 `outlet-ops` 프로젝트에 존재하는데, 백엔드 추가 과정에서 별도의 빈 `backend` 프로젝트가 따로 생성된 상태였습니다. 즉, 두 서비스가 서로 다른 Railway 프로젝트에 분리되어 있었습니다.

CLI를 사용해 올바른 프로젝트에 백엔드 서비스를 다시 등록했습니다.

```powershell
railway.cmd link
railway.cmd add --service backend
```

이후 GitHub 소스 연동 방식 대신, 로컬 폴더에서 직접 파일을 업로드하는 방법으로 변경했습니다.

```powershell
railway.cmd up .\backend --path-as-root --service backend
```

이 과정을 거치자 Railway가 Dockerfile을 정상 인식하게 되었고, Java 17 이미지와 Spring Boot JAR 빌드가 문제없이 완료됐습니다.

## 6. 빌드는 정상이나, 배포 단계에서 컨테이너가 종료됨

Docker 빌드는 문제없이 완료됐으나, 컨테이너가 실행되자마자 곧바로 종료됐습니다.

```text
Communications link failure
Connection refused
```

새로 생성한 `outlet-ops/backend` 서비스에서 데이터베이스 환경변수가 제대로 지정되지 않아, Spring Boot가 기본값인 `localhost:3306`으로 접속을 시도했습니다. 당연히 컨테이너 내부에 MySQL이 존재하지 않으므로 연결이 거부될 수밖에 없었습니다.

우선 CLI를 통해 데이터베이스 환경변수가 실제로 세팅되어 있는지 확인한 뒤, 동일 프로젝트 내 MySQL 인스턴스의 변수값을 참조하는 방식으로 환경설정을 변경했습니다.

```text
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
```

데이터베이스 접속정보 값 자체는 보안상 로그나 문서에 남기지 않고, 설정 여부만 검증했습니다. 수정 후 재배포를 진행했고, 배포 상태가 `SUCCESS`로 변경되는 것을 확인했으며, 운영 중인 API 역시 정상적으로 응답함을 확인했습니다.

```text
GET /api/health  → {"status":"UP"}
GET /v3/api-docs → HTTP 200
```

## 7. PowerShell에서 JDBC URL 내 `&` 문자가 명령 구분자로 인식되는 문제

Railway CLI를 통해 JDBC URL 설정 시, 쿼리스트링 내의 `&` 이후 값들이 각각 별도의 명령어로 처리되는 현상이 발생했습니다.

```text
'allowPublicKeyRetrieval' is not recognized...
'serverTimezone' is not recognized...
```

Windows에서 `.cmd`를 실행할 때 PowerShell이 `&`를 셸 명령 구분자로 해석하는 것이 원인이었습니다. 이스케이프 처리로 해결할 수도 있었지만, Railway 내부 네트워크로 연결되는 환경이라 해당 옵션들이 정말 필요한지부터 점검했습니다.

```text
jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
```

불필요한 옵션 없이 위와 같이 최소 형태의 JDBC URL만 입력해도 MySQL Connector/J의 연결에는 문제가 없었습니다. 환경변수 6개 모두 정상적으로 설정되었는지에 대해서도, 값 노출 없이 세팅 여부만 확인했습니다.

## 최종 점검 내역

```text
Backend tests: 41 passed
Frontend lint: passed
Frontend production build: passed
Railway deployment: SUCCESS
Railway health: UP
Swagger/OpenAPI: HTTP 200
Vercel: HTTP 200
CORS origin: Vercel 운영 주소와 일치
```

모든 주요 항목이 정상적으로 동작함을 최종적으로 확인했습니다.