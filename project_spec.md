# My-App Project Specification

## 1. Project Overview
이 프로젝트는 공공 도서관 데이터(정보나루 API)를 활용하여 도서 정보를 제공하고, 텍스트 임베딩, 벡터 검색 및 생성형 AI를 사용하여 도서 추천 및 질의응답과 같은 AI 기반 기능을 제공하는 풀스택 모노레포 웹 애플리케이션입니다.

## 2. Architecture & Infrastructure
- **Deployment & Orchestration**: Docker Compose (`docker-compose.yml`)
- **Database**: PostgreSQL (벡터 저장 및 유사도 검색을 위한 `pgvector` 확장 포함, DB명: `library_db`)
- **Web Server / Reverse Proxy**: Nginx (프론트엔드 정적 파일 서빙 및 백엔드 API 라우팅)

## 3. Backend (`/backend`)
비즈니스 로직, 인증, 외부 API 연동 및 AI 서버와의 통신을 담당하는 메인 API 서버입니다.
- **Language / Framework**: Java 21, Spring Boot 3.4.4
- **Core Libraries**: Spring Web, Spring Data JPA, QueryDSL, Spring Security, Spring Batch
- **Database Access**: PostgreSQL (JPA & QueryDSL 활용), H2 (로컬 테스트용)
- **Authentication**: Kakao OAuth2 Client, JWT (JSON Web Tokens)
- **AI Integration**: AWS Bedrock SDK (`software.amazon.awssdk:bedrockruntime`) - 생성형 AI 기능 활용
- **External API Integration**: 
  - `jackson-dataformat-xml`: 전국 도서관 정보나루 API 등의 XML 응답 데이터 파싱
  - Spring WebFlux (WebClient): 비동기 외부 API 호출
- **API Documentation**: Swagger (`springdoc-openapi-starter-webmvc-ui`)

## 4. Frontend (`/frontend`)
사용자에게 보여지는 웹 인터페이스(SPA)입니다.
- **Core Libraries**: React 19.2, Vite
- **Routing**: `react-router-dom`
- **Data Visualization**: `recharts` (도서 관련 통계, 대출 현황 등의 그래프 시각화)
- **Styling**: Tailwind CSS (루트 디렉토리의 `tailwind.config.js` 및 PostCSS 사용)

## 5. AI Server (`/ai-server`)
도서 정보 텍스트를 벡터로 변환하는 임베딩 전용 파이썬 기반 서버입니다.
- **Language / Framework**: Python, FastAPI
- **Embeddings**: SRoBERTa (Sentence-RoBERTa) 모델(`jhgan/ko-sroberta-multitask`)을 활용하여 텍스트(책 줄거리, 리뷰 등)를 768차원 벡터로 변환합니다. 변환된 벡터는 백엔드를 거쳐 PostgreSQL(`pgvector`)에 저장되어 유사도 검색에 사용됩니다.

## 6. Key Workflows (Inferred)
1. **인증 과정**: 사용자가 카카오 로그인을 통해 인증하면, 백엔드에서 JWT를 발급하여 이후 API 요청에 사용합니다.
2. **도서 데이터 수집**: 백엔드에서 "정보나루" API를 호출하여 도서 및 대출 관련 XML 데이터를 수집하고 파싱합니다.
3. **AI 임베딩 파이프라인**: 도서 정보나 텍스트 데이터가 AI 서버로 전달되어 SRoBERTa를 통해 벡터화되고, 이는 PostgreSQL의 `pgvector` 컬럼에 저장됩니다.
4. **AI 기반 추천/검색**: 사용자가 설문을 완료하거나 도서를 검색하면, PostgreSQL의 `pgvector`를 통한 벡터 유사도 검색으로 의미적으로 연관된 도서를 1차 추천합니다. 이후 백엔드에서 AWS Bedrock(Claude) API를 직접 호출하여 사용자의 페르소나에 맞춘 추천 코멘트를 최종 생성합니다.
