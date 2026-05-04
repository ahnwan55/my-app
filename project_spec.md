# Bookjjeok (북적북적) Project Specification

## 1. Project Overview
"북적북적(Bookjjeok)"은 공공 도서관 데이터(정보나루 API)와 카카오 도서 검색 API를 결합하고, SRoBERTa 텍스트 임베딩, pgvector 유사도 검색, AWS Bedrock(Claude) 생성형 AI를 활용하여 독서 성향을 12가지 서브 페르소나로 분류하고 맞춤형 도서를 추천해주는 풀스택 AI 웹 애플리케이션입니다.

## 2. Architecture & Infrastructure
- **Containerization**: Docker (멀티 스테이지 빌드) 및 Docker Compose (`docker-compose.yml` - 로컬 개발용)
- **Deployment & Orchestration**: Kubernetes (EKS) 기반 배포 (`/k8s` 디렉토리의 Deployment, Service, ConfigMap 매니페스트)
- **Secret Management**: External-Secrets 연동을 통한 AWS Secrets Manager 활용 (`backend-external-secret.yaml`)
- **CI/CD Pipeline**: GitHub Actions (`.github/workflows/`)
  - 백엔드 & AI 서버: ECR 이미지 빌드 및 푸시, K8s 매니페스트 태그 업데이트
  - 프론트엔드: S3 정적 호스팅 및 CloudFront 캐시 무효화
- **Web Server / Reverse Proxy**: Nginx (React SPA 서빙 및 `/api`, `/oauth2`, `/login` 백엔드 라우팅)
- **Database**: PostgreSQL 15 (`pgvector` 확장 포함, 도서 텍스트의 768차원 임베딩 벡터 저장 및 거리 계산 지원)

## 3. Backend (`/backend`)
비즈니스 로직, 인증, 데이터 파이프라인(Spring Batch) 및 AI 서버 통신을 담당하는 메인 API 서버.
- **Language / Framework**: Java 21, Spring Boot 3.4.4, Gradle 8.7
- **Core Libraries**: Spring Web, Spring Data JPA, QueryDSL 5, Spring Batch, Spring Security
- **Authentication**: Kakao OAuth2 Client (`/oauth2/authorization/kakao`), JWT (HttpOnly 쿠키 방식 아님, Authorization 헤더 기반)
- **AI Integration**:
  - `software.amazon.awssdk:bedrockruntime` (버전 2.25.11): Claude 모델에 프롬프트를 전송하여 12종의 서브 페르소나 분석 결과(JSON) 파싱 및 추천 도서 코멘트 생성
  - `AiServerClient` (WebClient): 내부 AI 서버(FastAPI)의 `/embed`, `/embed/batch` 엔드포인트를 호출하여 벡터값 반환
- **External API Integration**:
  - **카카오 도서 검색 API**: 도서 기본 정보(제목, 저자, ISBN-13, 표지 등) 검색
  - **정보나루 API**: `jackson-dataformat-xml`을 사용하여 이달의 인기 대출 도서, 도서관별 장서/대출 현황 데이터 수집
- **Batch Processing**: `Spring Batch`와 `LibraryScheduler`를 통해 월간 인기 도서 업데이트 및 도서관 장서 데이터 정기 동기화 수행
- **Monitoring**: Spring Actuator와 `micrometer-registry-prometheus`를 통한 메트릭 노출

## 4. Frontend (`/frontend`)
사용자에게 보여지는 웹 인터페이스(SPA).
- **Core Libraries**: React 19.2, Vite 8
- **Routing**: `react-router-dom` 7 (인증/프로필 유무에 따른 Protected 라우팅 처리 로직 존재 - `App.jsx`)
- **Data Visualization**: `recharts` 3 (독서 페르소나 6대 지표 레이더 차트, 대출 통계 등 시각화)
- **Styling**: `index.css`를 통한 Vanilla CSS 및 CSS 변수 기반 디자인 시스템 (벚꽃 핑크 × 퍼플 컬러 테마 적용). (Tailwind는 설정만 되어있고 주로 Vanilla CSS 활용)
- **Key Pages**: `SurveyPage`(설문), `PersonaResultPage`(페르소나 판별 결과), `BookResultPage`(추천 도서 목록), `InventoryPage`(도서관 장서/대출 현황) 등 13개의 독립된 페이지 컴포넌트 구조.

## 5. AI Server (`/ai-server`)
도서 정보 및 사용자의 텍스트를 벡터로 변환하는 마이크로서비스.
- **Language / Framework**: Python 3.11, FastAPI, Uvicorn
- **Embeddings**: Sentence-Transformers 기반 `jhgan/ko-sroberta-multitask` 모델 사용. 입력 텍스트를 768차원 실수 리스트(벡터)로 변환하여 JSON 배열로 반환.
- **Database Context**: `database.py`에서 SQLAlchemy를 사용해 서버 시작 시 DB 접속 후 `CREATE EXTENSION IF NOT EXISTS vector`를 실행해 pgvector를 초기화함.

## 6. Key Workflows

### 1. 인증 및 프로필 설정 (Auth Flow)
- 카카오 소셜 로그인 완료 후 `OAuth2SuccessHandler`에서 JWT 발급
- 프론트엔드 라우팅에서 `/api/users/me` 호출 후 최초 로그인 사용자(성별 데이터 등 누락)는 강제로 `/user-info` 페이지로 이동시켜 프로필을 완성함.

### 2. 독서 페르소나 분석 파이프라인
1. 사용자가 12가지 항목의 독서 성향 설문(`SurveyPage`) 완료
2. 백엔드에서 답변 내용을 모아 프롬프트를 구성하여 AWS Bedrock Claude API 호출
3. Claude는 답변을 분석해 12개의 지정된 서브 페르소나(EXPLORER 계열, CURATOR 계열 등) 중 하나를 선택하고, 6개 핵심 지표 점수를 JSON으로 반환
4. 백엔드는 이를 파싱하여 `PersonaAnalysis` 엔티티로 DB에 저장 및 프론트엔드로 전달 (`Recharts`를 통해 시각화)

### 3. AI 기반 의미론적 도서 추천 파이프라인
1. 사용자의 설문 데이터(또는 페르소나 핵심 키워드)를 백엔드에서 AI 서버(`/embed`)로 전송하여 768차원 기준 벡터값 획득
2. PostgreSQL에서 `pgvector` 코사인 거리 연산 등 유사도 검색 쿼리(`BookVectorRepository`)를 수행하여 가장 가까운 도서 목록 추출
3. 추출된 도서 목록과 사용자의 페르소나 이름을 묶어 다시 AWS Bedrock Claude에 전달, 사용자 맞춤형 추천 이유(코멘트) 생성
4. 프론트엔드는 도서 목록과 AI 코멘트를 렌더링 (`BookResultPage`)

### 4. 도서관 데이터 배치 동기화
1. Spring Batch(`BookSyncJobConfig`)를 활용하여 대량의 정보나루 API 데이터를 읽고 씀
2. 월간 인기 대출 도서, 도서 상세 데이터, 도서관 장서 목록을 일정 주기로 동기화하여 `Library` 및 `BookHolding` 테이블 최신화
