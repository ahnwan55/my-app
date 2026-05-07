# Bookjjeok (북적북적) Project Specification

## 1. Project Overview
"북적북적(Bookjjeok)"은 공공 도서관 데이터(정보나루 API)와 카카오 도서 검색 API를 결합하고, SRoBERTa 텍스트 임베딩, pgvector 유사도 검색, AWS Bedrock(Claude) 생성형 AI를 활용하여 독서 성향을 12가지 서브 페르소나로 분류하고 맞춤형 도서를 추천해주는 풀스택 AI 웹 애플리케이션입니다.

## 2. Architecture & Infrastructure

### Containerization & Local Development
- **Docker**: 멀티 스테이지 빌드를 적용하여 각 서비스(backend, ai-server, frontend)를 이미지화
- **Docker Compose** (`docker-compose.yml`): 로컬 개발 환경에서 5개 서비스를 한 번에 실행
  - `postgres` (PostgreSQL 15), `redis` (Redis 7 Alpine), `backend` (Spring Boot :8080), `ai-server` (FastAPI :8000), `frontend` (React + Nginx :80)

### Kubernetes & Deployment
- **Orchestration**: Kubernetes (EKS) 기반 배포, Kustomize를 활용한 다중 환경 관리
- **디렉토리 구조** (`/k8s`):
  - `base/`: 공통 베이스 매니페스트 (Deployment, Service, ConfigMap, HPA, ExternalSecret)
  - `vpc1-eks/`: EKS 환경 오버레이 — ALB Ingress, LoadBalancer 타입 서비스
  - `vpc2-manual/`: 수동 구성 환경 오버레이 — ECR 자격증명 갱신 CronJob, NodePort 서비스
  - `logging/`: Fluent Bit DaemonSet (컨테이너 로그 수집)
- **Auto Scaling**: HorizontalPodAutoscaler(HPA)를 backend 및 ai-server에 각각 적용
- **Secret Management**: External-Secrets Operator + AWS Secrets Manager 연동 (`backend-external-secret.yaml`, `external-secret-store.yaml`)
- **Helm**: `/charts` 디렉토리 및 `generate_helm_charts.py` 스크립트로 Helm Chart 관리 지원

### CI/CD Pipeline
- **GitHub Actions** (`.github/workflows/`):
  - `backend-cd.yml`: 백엔드 → ECR 이미지 빌드/푸시, K8s 매니페스트 이미지 태그 업데이트
  - `ai-cd.yml`: AI 서버 → ECR 이미지 빌드/푸시, K8s 매니페스트 이미지 태그 업데이트
  - `frontend-cd.yml`: React 빌드 → S3 정적 호스팅 + CloudFront 캐시 무효화

### Web Server / Reverse Proxy
- **Nginx** (`nginx/nginx.conf`): React SPA 서빙(try_files), `/api`, `/oauth2`, `/login` 경로를 백엔드(:8080)로 프록시

### Databases & Cache
- **PostgreSQL 15**: 주 데이터베이스. `pgvector` 확장으로 도서 텍스트의 768차원 임베딩 벡터 저장 및 코사인 유사도 검색 지원
- **Redis 7**: Spring Data Redis 연동. 캐싱 및 세션 보조 용도

---

## 3. Backend (`/backend`)
비즈니스 로직, 인증, 데이터 파이프라인(Spring Batch) 및 외부 서비스 통신을 담당하는 메인 API 서버.

- **Language / Framework**: Java 21, Spring Boot 3.4.4, Gradle 8.7
- **Core Libraries**: Spring Web, Spring Data JPA, QueryDSL 5, Spring Batch, Spring Security, Spring Data Redis
- **Authentication**: Kakao OAuth2 Client (`/oauth2/authorization/kakao`), JWT (Authorization 헤더 방식, jjwt 0.12.3)
- **AI Integration**:
  - `software.amazon.awssdk:bedrockruntime` (버전 2.25.11): Claude 모델에 프롬프트를 전송하여 12종의 서브 페르소나 분석 결과(JSON) 파싱 및 추천 도서 코멘트 생성
  - `AiServerClient` (WebClient): 내부 AI 서버(FastAPI)의 `/embed`, `/embed/batch` 엔드포인트를 호출하여 벡터값 반환
- **External API Integration**:
  - **카카오 도서 검색 API**: 도서 기본 정보(제목, 저자, ISBN-13, 표지 등) 검색
  - **정보나루 API**: `jackson-dataformat-xml`을 사용하여 이달의 인기 대출 도서, 도서관별 장서/대출 현황 데이터 수집
- **Batch Processing**: `Spring Batch`와 `LibraryScheduler`를 통해 월간 인기 도서 업데이트 및 도서관 장서 데이터 정기 동기화 수행
- **Observability**:
  - Spring Actuator + `micrometer-registry-prometheus`: HTTP 메트릭 노출 (`/actuator/prometheus`)
  - `micrometer-tracing-bridge-otel` + `opentelemetry-spring-boot-starter`: 분산 추적(OTLP Tracing)
- **API Docs**: `springdoc-openapi-starter-webmvc-ui` 2.8.6 — Swagger UI (`/swagger-ui.html`)

---

## 4. Frontend (`/frontend`)
사용자에게 보여지는 웹 인터페이스(SPA).

- **Core Libraries**: React 19.2, Vite 8
- **Routing**: `react-router-dom` 7 (인증/프로필 유무에 따른 Protected 라우팅 처리 — `App.jsx`)
- **Data Visualization**: `recharts` 3 (독서 페르소나 6대 지표 레이더 차트, 대출 통계 등 시각화)
- **Styling**: `index.css`를 통한 Vanilla CSS 및 CSS 변수 기반 디자인 시스템 (벚꽃 핑크 × 퍼플 컬러 테마). Tailwind 설정 파일 존재하나 주요 스타일링은 Vanilla CSS 활용
- **Key Pages (13개)**: `LoginPage`, `UserInfoPage`, `MainPage`, `SurveyPage`, `LoadingPage`, `PersonaResultPage`, `BookLoadingPage`, `BookResultPage`, `BookDetailPage`, `SearchPage`, `RankingPage`, `InventoryPage`, `MyPage`

---

## 5. AI Server (`/ai-server`)
도서 정보 및 사용자의 텍스트를 벡터로 변환하는 마이크로서비스.

- **Language / Framework**: Python 3.11, FastAPI, Uvicorn
- **Embeddings**: Sentence-Transformers 기반 `jhgan/ko-sroberta-multitask` 모델 사용. 입력 텍스트를 768차원 실수 리스트(벡터)로 변환하여 JSON 배열로 반환
- **Endpoints**: `POST /embed` (단건), `POST /embed/batch` (배치), `GET /health`, `GET /metrics`
- **Database Context**: `database.py`에서 SQLAlchemy를 사용해 서버 시작 시 DB 접속 후 `CREATE EXTENSION IF NOT EXISTS vector`를 실행해 pgvector를 초기화
- **Monitoring**: `prometheus-fastapi-instrumentator` — HTTP 요청 latency, throughput, error 메트릭을 `/metrics` 엔드포인트로 노출

---

## 6. Key Workflows

### 1. 인증 및 프로필 설정 (Auth Flow)
- 카카오 소셜 로그인 완료 후 `OAuth2SuccessHandler`에서 JWT 발급
- 프론트엔드 라우팅에서 `/api/users/me` 호출 후 최초 로그인 사용자(성별 데이터 등 누락)는 강제로 `/user-info` 페이지로 이동시켜 프로필을 완성함

### 2. 독서 페르소나 분석 파이프라인
1. 사용자가 12가지 항목의 독서 성향 설문(`SurveyPage`) 완료
2. 백엔드에서 답변 내용을 모아 프롬프트를 구성하여 AWS Bedrock Claude API 호출
3. Claude는 답변을 분석해 12개의 지정된 서브 페르소나(EXPLORER 계열, CURATOR 계열 등) 중 하나를 선택하고, 6개 핵심 지표 점수를 JSON으로 반환
4. 백엔드는 이를 파싱하여 `PersonaAnalysis` 엔티티로 DB에 저장 및 프론트엔드로 전달 (`Recharts`를 통해 레이더 차트로 시각화)

### 3. AI 기반 의미론적 도서 추천 파이프라인 (Real-time Re-ranking)
1. 카카오 도서 검색 API를 통해 사용자의 페르소나 키워드에 맞는 후보 도서(약 20권)를 실시간으로 확보
2. 후보 도서들의 텍스트(제목+저자+소개)와 사용자의 취향 텍스트(페르소나 판정 이유)를 AI 서버(`/embed/batch`, `/embed`)로 전송하여 각각 768차원 벡터값 획득
3. Java 메모리 상에서 사용자 벡터와 후보 도서 벡터 간의 **코사인 유사도(Cosine Similarity)**를 직접 계산하여 점수가 가장 높은 상위 5권으로 순위 재조정 (Re-ranking)
4. 최종 선정된 도서 목록과 페르소나 정보를 AWS Bedrock Claude에 전달하여 사용자 맞춤형 추천 이유(코멘트) 생성
5. 프론트엔드는 도서 목록과 AI 코멘트를 렌더링 (`BookResultPage`)

### 4. 도서관 데이터 배치 동기화
1. Spring Batch(`BookSyncJobConfig`)를 활용하여 대량의 정보나루 API 데이터를 읽고 씀
2. 월간 인기 대출 도서, 도서 상세 데이터, 도서관 장서 목록을 일정 주기로 동기화하여 `Library` 및 `BookHolding` 테이블 최신화

### 5. 도서관 장서/대출 현황 조회 (Inventory)
- 사용자가 도서를 선택하면 백엔드가 정보나루 API를 통해 서울 지역 공공 도서관의 해당 도서 장서 보유 여부 및 대출 가능 여부를 실시간 조회
- API 호출 한도 초과 시 결정론적 Mock 데이터(도서관별 입고일 기반 시뮬레이션)로 폴백하여 UI 개발 연속성 보장

---

## 7. Observability Stack
| 구성 요소 | 기술 | 엔드포인트 |
|-----------|------|-----------|
| 백엔드 메트릭 | Micrometer Prometheus | `/actuator/prometheus` |
| AI 서버 메트릭 | prometheus-fastapi-instrumentator | `/metrics` |
| 분산 추적 | OpenTelemetry (OTLP) | OTLP Collector로 전송 |
| 로그 수집 | Fluent Bit DaemonSet | K8s 컨테이너 로그 → 외부 로그 백엔드 |
