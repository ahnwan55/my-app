# 📚 My-App: AI 기반 공공 도서관 서비스

My-App은 전국 공공 도서관 데이터(정보나루 API)를 활용하여 사용자에게 도서 정보를 제공하고, AI 기술을 통해 맞춤형 도서 추천 및 질의응답 서비스를 제공하는 웹 애플리케이션입니다.

## ✨ 주요 기능

*   **카카오 소셜 로그인**: 간편하고 안전한 사용자 인증 메커니즘 제공
*   **도서 정보 및 통계 제공**: 공공 데이터 API를 활용한 실시간 도서 정보 검색 및 대출 통계 데이터 시각화
*   **AI 맞춤형 도서 추천**: `SRoBERTa` 텍스트 임베딩과 `pgvector` 기반의 벡터 유사도 검색을 통한 텍스트 의미 기반의 정교한 추천 시스템
*   **AI 도서 요약 및 맞춤 코멘트**: Spring Boot 백엔드와 AWS Bedrock(Claude 모델)의 직접 연동을 통한 강력한 생성형 AI 서비스 경험 제공

## 🛠️ 기술 스택

*   **Frontend**: React 19, Vite, Tailwind CSS, Recharts
*   **Backend**: Java 21, Spring Boot 3.4, Spring Security, QueryDSL
*   **AI Server**: Python, FastAPI, SRoBERTa
*   **Database**: PostgreSQL (with pgvector extension)
*   **Infrastructure**: Docker, Docker Compose, Nginx

## 🚀 시작하기

이 프로젝트는 Docker Compose를 사용하여 전체 서비스 환경(DB, API 서버, AI 서버, 프론트엔드 웹 서버)을 한 번에 구축하고 실행할 수 있도록 구성되어 있습니다.

### 사전 요구 사항
*   Docker 및 Docker Compose가 시스템에 설치되어 있어야 합니다.
*   프로젝트 내의 `backend/.env` 파일에 필요한 환경 변수(DB 계정, 카카오 API 키, AWS 자격 증명 등)를 환경에 맞게 설정해야 합니다.

### 실행 방법

1.  터미널에서 프로젝트 루트 디렉토리로 이동합니다.
    ```bash
    cd my-app
    ```

2.  Docker Compose를 사용하여 전체 컨테이너를 빌드하고 백그라운드에서 실행합니다.
    ```bash
    docker-compose up -d --build
    ```

3.  서비스가 정상적으로 실행되면 웹 브라우저에서 `http://localhost` (프론트엔드)에 접속하여 서비스를 이용할 수 있습니다.

*💡 백엔드 API 명세서(Swagger UI)는 서비스 실행 후 `http://localhost:8080/swagger-ui.html`에서 확인할 수 있습니다.*

## 📖 기술 명세 문서

더 상세한 아키텍처, 디렉토리 구조 및 내부 시스템 데이터 파이프라인에 대한 설명은 개발자 참조용 파일인 [`project_spec.md`](./project_spec.md) 문서를 확인해 주세요.
