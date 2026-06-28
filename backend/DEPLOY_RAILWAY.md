# Railway 배포 가이드 (합격각 백엔드)

> 로컬에서만 돌던 백엔드를 **항상 켜진 클라우드 서버**로 올려, 폰/실기기에서도 접속하게 만든다.
> 코드·설정(Dockerfile, 환경변수 기반 datasource)은 이미 준비됨. 아래는 **브라우저에서 직접** 할 단계.

## 0. 큰 그림
```
GitHub(jeongsi-pass) ──▶ Railway 자동 빌드
                          ├─ 백엔드 서비스 (backend/Dockerfile)
                          └─ PostgreSQL (관리형 DB) ← 백엔드가 DATABASE_URL로 연결
```
- DB는 Railway **관리형 Postgres** 버튼으로 생성. 부팅 시 Flyway(V1~V6)가 더미데이터 자동 적재.
- 비밀값은 코드가 아니라 Railway **환경변수**에.
- 요금: 가입 무료 크레딧 → 이후 Hobby(월 $5 사용량 포함). 소규모면 대개 그 안.

## 1. 프로젝트 + DB
1. https://railway.com → **Login with GitHub**.
2. **New Project → Deploy from GitHub repo →** `hkforwork5260-tech/jeongsi-pass` 선택(권한 허용).
3. 캔버스에서 **New → Database → Add PostgreSQL** → `Postgres` 서비스 생성됨.

## 2. 백엔드 서비스 설정 (서비스 카드 → Settings)
### 2-1. Root Directory (★ 필수)
- **Settings → Build → Root Directory** = `backend`
- (그래야 `backend/Dockerfile`로 빌드. Builder는 Dockerfile 자동 감지.)

### 2-2. 환경변수 (Variables 탭) — ★ 변수명 주의
| 변수 이름 | 값 |
|---|---|
| `DATABASE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `DB_USER` | `${{Postgres.PGUSER}}` |
| `DB_PASSWORD` | `${{Postgres.PGPASSWORD}}` |

> ⚠️ `DATABASE_URL`은 **반드시 `jdbc:` 접두사**로 직접 입력(Railway 기본값은 `postgresql://`라 Spring이 못 읽음).
> ⚠️ 변수명은 `DB_USER`/`DB_PASSWORD`(이 앱 기준). SPRING_PROFILES_ACTIVE는 **설정하지 말 것**(기본 `local` 프로파일이 위 변수를 읽음).
> Postgres 서비스 이름이 `Postgres`가 아니면 그 이름으로 바꿀 것.

### 2-3. 도메인
- **Settings → Networking → Generate Domain** → `xxxx.up.railway.app` 발급. 포트는 `${PORT}` 자동 주입이라 설정 불필요.

## 3. 배포 확인
- Deployments 로그에 `BUILD SUCCESSFUL` + `Started HapgyeokApplicationKt` + Flyway `Successfully applied ... migrations`.
- 헬스: `curl https://<도메인>/actuator/health` → `{"status":"UP"}`
- 데이터: `curl https://<도메인>/api/v1/home -H "X-Device-Id: test"` → 홈 JSON(더미 자동 적재돼 바로 나옴)

## 4. 안드로이드 연결 (배포 후)
- `android-app/.../data/ApiClient.kt`의 `BASE_URL`을 `https://<도메인>/`로 변경 → 앱 재빌드 → 실기기 설치.

## 5. 자주 막히는 곳
| 증상 | 해결 |
|---|---|
| `Failed to configure a DataSource` | `DATABASE_URL`이 `jdbc:`로 시작 안 함 (2-2) |
| `UnknownHostException ... PGHOST` | 참조변수 서비스명이 `Postgres`가 아님 |
| 빌드가 Dockerfile 안 씀 | Root Directory가 `backend` 아님 (2-1) |
| 도메인 접속 404 | 도메인 생성 안 했거나 빌드 진행 중 |
