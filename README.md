# 📚 북적북적 (Bookjjeok) — AI 기반 독서 페르소나 & 도서 추천 서비스

> 설문 기반 독서 페르소나 분석과 벡터 유사도 검색을 결합하여 **나만의 책**을 찾아주는 풀스택 웹 애플리케이션

북적북적은 전국 공공 도서관 데이터(정보나루 API)와 카카오 도서 검색 API를 활용하여 도서 정보를 제공하고, **SRoBERTa 텍스트 임베딩 + pgvector 벡터 유사도 검색 + AWS Bedrock Claude 생성형 AI**를 통해 사용자 맞춤형 도서 추천 및 페르소나 분석 서비스를 제공합니다.

---

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| **🔐 카카오 소셜 로그인** | OAuth2 + JWT(HttpOnly 쿠키) 기반의 안전한 인증. 최초 로그인 시 프로필(성별 등) 설정 유도 |
| **📋 독서 성향 설문** | 독서 습관·선호에 대한 설문을 통해 데이터 수집 |
| **🧠 AI 페르소나 분석** | AWS Bedrock Claude가 설문 답변을 분석하여 **12가지 서브 페르소나** 중 하나로 분류하고, 6대 지표 점수(레이더 차트) 제공 |
| **📖 AI 맞춤형 도서 추천** | SRoBERTa 임베딩 + pgvector 벡터 유사도 검색으로 의미 기반 1차 추천 → Bedrock Claude가 페르소나에 맞춘 추천 코멘트 생성 |
| **🔍 도서 검색** | 카카오 도서 검색 API를 활용한 키워드 기반 도서 검색 |
| **📊 인기 대출 도서 랭킹** | 정보나루 API 기반 월간 인기 대출 도서 Top N 데이터 시각화 (Recharts) |
| **📍 도서관 장서·대출 현황** | 서울 지역 공공 도서관별 장서 보유 및 대출 가능 여부 조회 |
| **👤 마이페이지** | 나의 페르소나 이력, 추천 도서 목록 확인 |

### 🎭 12가지 독서 페르소나

| 메인 유형 | 서브 페르소나 |
|-----------|-------------|
| **EXPLORER** (탐험가) | TREND_SURFER · POLYMATH_SEEKER |
| **CURATOR** (큐레이터) | AESTHETIC_COLLECTOR · KNOWLEDGE_EDITOR |
| **NAVIGATOR** (항해사) | FAST_SOLVER · CAREER_STRATEGIST |
| **DWELLER** (정주민) | EMOTIONAL_SYNCHRO · CASUAL_RESTER |
| **ANALYST** (분석가) | COLD_CRITIC · SILENT_RESEARCHER |
| **DIVER** (잠수부) | CONTEMPLATIVE_MONK · OBSESSIVE_FANDOM |

---

## 🛠️ 기술 스택

### Frontend
| 항목 | 버전 / 라이브러리 |
|------|-----------------|
| Core | React 19.2, Vite 8 |
| Routing | react-router-dom 7 |
| Styling | Vanilla CSS (Noto Sans KR + Playfair Display, 벚꽃 핑크 × 퍼플 디자인 시스템) |
| Data Visualization | Recharts 3 |
| Icons | lucide-react |

### Backend
| 항목 | 버전 / 라이브러리 |
|------|-----------------|
| Core | Java 21, Spring Boot 3.4.4, Gradle 8.7 |
| Security | Spring Security, OAuth2 Client (카카오), JWT (jjwt 0.12.3) |
| Data | Spring Data JPA, QueryDSL 5, PostgreSQL + pgvector 0.1.4 |
| Batch | Spring Batch (도서관 데이터 동기화 파이프라인) |
| AI Integration | AWS Bedrock SDK 2.25.11 (Claude), FastAPI SRoBERTa 클라이언트 |
| External APIs | 정보나루 API (XML/jackson-dataformat-xml), 카카오 도서 검색 API |
| HTTP Client | Spring WebFlux (WebClient) |
| Monitoring | Spring Actuator + Micrometer Prometheus |
| API Docs | springdoc-openapi (Swagger UI) |
| Utilities | Lombok, spring-dotenv |

### AI Server
| 항목 | 버전 / 라이브러리 |
|------|-----------------|
| Core | Python 3.11, FastAPI, Uvicorn |
| Embeddings | sentence-transformers (SRoBERTa: `jhgan/ko-sroberta-multitask`, 768차원) |
| Database | SQLAlchemy 2 + psycopg2-binary (pgvector 익스텐션 관리) |

### Infrastructure
| 항목 | 설명 |
|------|------|
| Containerization | Docker (멀티 스테이지 빌드) + Docker Compose |
| Web Server | Nginx (React SPA 서빙 + 백엔드 API 리버스 프록시) |
| Database | PostgreSQL 15 + pgvector 확장 |
| CI/CD | GitHub Actions (Backend/AI → ECR, Frontend → S3 + CloudFront) |
| Orchestration | Kubernetes (EKS) — ExternalSecret + AWS Secrets Manager |
| Cloud | AWS (ECR, EKS, S3, CloudFront, Bedrock, Secrets Manager) |

---

## 🏗️ 아키텍처

```
                   ┌─────────────┐
                   │   Browser   │
                   └──────┬──────┘
                          │ :80
                   ┌──────▼──────┐
                   │    Nginx    │ ← React SPA 서빙
                   │ /api → proxy│
                   └──────┬──────┘
                          │ :8080
              ┌───────────▼───────────┐
              │   Spring Boot 백엔드   │
              │  (Java 21, JPA, JWT)  │
              └───┬───────┬───────┬───┘
                  │       │       │
       ┌──────────▼ ┐  ┌──▼────┐  ├──→ 카카오 API
       │  AI Server │  │ AWS   │  ├──→ 정보나루 API
       │  (FastAPI) │  │Bedrock│  │
       │  SRoBERTa  │  │Claude │  │
       └──────┬─────┘  └───────┘  │
              │                   │
       ┌──────▼───────────────────▼┐
       │    PostgreSQL 15          │
       │  + pgvector (768차원)     │
       └───────────────────────────┘
```

---

## 🚀 시작하기

### 사전 요구 사항

- Docker 및 Docker Compose
- `backend/.env` 파일에 필수 환경 변수 설정:
  ```
  DB_URL=jdbc:postgresql://postgres:5432/library_db
  DB_USER=postgres
  DB_PASSWORD=postgres
  KAKAO_CLIENT_ID=<카카오 OAuth 클라이언트 ID>
  KAKAO_CLIENT_SECRET=<카카오 OAuth 시크릿>
  KAKAO_REST_API_KEY=<카카오 REST API 키>
  JWT_SECRET=<JWT 서명 비밀키>
  AWS_ACCESS_KEY=<AWS 액세스 키>
  AWS_SECRET_KEY=<AWS 시크릿 키>
  AWS_REGION=ap-northeast-2
  BEDROCK_MODEL_ID=<Bedrock Claude 모델 ID>
  LIBRARY_API_KEY=<정보나루 API 인증키>
  AI_SERVER_URL=http://ai-server:8000
  ```

### 실행 방법

```bash
# 1. 프로젝트 루트로 이동
cd my-app

# 2. 전체 서비스 빌드 및 실행
docker-compose up -d --build

# 3. 접속
#    - 프론트엔드: http://localhost
#    - Swagger UI: http://localhost:8080/swagger-ui.html
#    - AI Server Health: http://localhost:8000/health
#    - Prometheus Metrics: http://localhost:8080/actuator/prometheus
```

### 로컬 개발 (Docker 없이)

```bash
# Frontend (포트 3000, /api → :8080 프록시 설정됨)
cd frontend && npm install && npm run dev

# Backend (포트 8080)
cd backend && ./gradlew bootRun

# AI Server (포트 8000)
cd ai-server && pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

---

## 📂 프로젝트 구조

```
my-app/
├── frontend/                # React 19 SPA (Vite)
│   ├── src/
│   │   ├── pages/           # 13개 페이지 컴포넌트
│   │   │   ├── LoginPage        # 카카오 로그인
│   │   │   ├── UserInfoPage     # 최초 프로필 설정
│   │   │   ├── MainPage         # 메인 (설문 시작)
│   │   │   ├── SurveyPage       # 독서 성향 설문
│   │   │   ├── LoadingPage      # 페르소나 분석 중
│   │   │   ├── PersonaResultPage# 페르소나 결과 + 레이더 차트
│   │   │   ├── BookLoadingPage  # 도서 추천 중
│   │   │   ├── BookResultPage   # 추천 도서 목록
│   │   │   ├── BookDetailPage   # 도서 상세 정보
│   │   │   ├── SearchPage       # 카카오 도서 검색
│   │   │   ├── RankingPage      # 인기 대출 도서 랭킹
│   │   │   ├── InventoryPage    # 도서관 장서/대출 현황
│   │   │   └── MyPage           # 마이페이지
│   │   ├── index.css        # 글로벌 디자인 시스템 (CSS 변수)
│   │   ├── App.jsx          # 라우팅 + 인증 상태 관리
│   │   └── main.jsx         # 앱 진입점
│   ├── Dockerfile           # 멀티 스테이지 빌드 (Node → Nginx)
│   └── package.json
│
├── backend/                 # Spring Boot 3.4 API 서버
│   ├── src/main/java/com/example/demo/
│   │   ├── auth/            # OAuth2 인증 (카카오 로그인 + JWT 발급)
│   │   ├── jwt/             # JWT 필터 + 유틸리티
│   │   ├── config/          # Security, Bedrock, RestClient 설정
│   │   ├── domain/
│   │   │   ├── book/        # 도서 (엔티티, 벡터, 월간인기, CRUD)
│   │   │   ├── survey/      # 설문 + 페르소나 분석 결과
│   │   │   ├── persona/     # 페르소나 코드/유형 엔티티
│   │   │   ├── recommendation/ # AI 추천 (벡터 검색 + Bedrock 코멘트)
│   │   │   ├── user/        # 사용자 프로필 관리
│   │   │   ├── library/     # 도서관 + 장서 보유 엔티티
│   │   │   └── inventory/   # 장서/대출 현황 조회
│   │   └── infra/
│   │       ├── ai/          # AiServerClient (임베딩) + BedrockClient (생성AI)
│   │       ├── kakao/       # KakaoBookClient (도서 검색)
│   │       └── library/     # LibraryApiClient + Spring Batch 동기화
│   ├── src/main/resources/
│   │   ├── application.yml  # 공통 설정 (환경변수 바인딩)
│   │   └── data.sql         # 초기 데이터
│   ├── Dockerfile           # 멀티 스테이지 빌드 (Gradle → JRE Alpine)
│   └── build.gradle
│
├── ai-server/               # FastAPI 임베딩 서버
│   ├── app/
│   │   ├── main.py          # FastAPI 앱 (lifespan: pgvector 활성화)
│   │   ├── routers.py       # /embed, /embed/batch 엔드포인트
│   │   ├── schemas.py       # Pydantic 요청/응답 모델
│   │   └── database.py      # SQLAlchemy 엔진 + pgvector 확인
│   ├── Dockerfile
│   └── requirements.txt
│
├── k8s/                     # Kubernetes 매니페스트
│   ├── backend-deployment.yaml
│   ├── backend-service.yaml
│   ├── backend-configmap.yaml
│   ├── backend-external-secret.yaml  # ExternalSecret → AWS Secrets Manager
│   ├── external-secret-store.yaml
│   ├── ai-deployment.yaml
│   └── ai-service.yaml
│
├── nginx/
│   └── nginx.conf           # SPA 서빙 + /api, /oauth2, /login 프록시
│
├── .github/workflows/       # CI/CD
│   ├── backend-cd.yml       # Backend → ECR + K8s manifest 업데이트
│   ├── ai-cd.yml            # AI Server → ECR + K8s manifest 업데이트
│   └── frontend-cd.yml      # Frontend → S3 + CloudFront 캐시 무효화
│
├── docker-compose.yml       # 로컬 개발용 전체 서비스 구성
└── .env                     # Docker Compose용 DB 환경변수
```

---

## 📖 기술 명세 문서

더 상세한 아키텍처, 내부 데이터 파이프라인 및 코드 레벨 설명은 [`project_spec.md`](./project_spec.md)를 참조해 주세요.
