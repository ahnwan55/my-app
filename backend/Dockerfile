# ────────────────────────────────────────────
# Stage 1: Build  (Gradle + JDK 빌드 환경)
# ────────────────────────────────────────────
FROM gradle:8.7-jdk21 AS builder

# 컨테이너 내 작업 디렉토리 설정
WORKDIR /app

# ① 의존성 레이어 캐싱 최적화
#    build.gradle, settings.gradle 만 먼저 복사 → 소스 변경 시에도
#    의존성 다운로드 레이어는 재사용됨 (빌드 속도 향상)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성만 먼저 다운로드 (소스 없이)
RUN gradle dependencies --no-daemon || true

# ② 전체 소스 복사 후 빌드
COPY src ./src

# 테스트 건너뜀: 로컬 DB(H2)는 있지만 CI 환경에서 외부 의존성 없이 빠르게 빌드
# -x test 제거하면 테스트도 실행됨
RUN gradle bootJar -x test --no-daemon

# ────────────────────────────────────────────
# Stage 2: Runtime  (JRE만 포함 — 최소 이미지)
# ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# 보안: root 대신 전용 유저로 실행
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Stage 1에서 빌드된 JAR만 복사 (Gradle 캐시, 소스코드 등은 포함 안 됨)
COPY --from=builder /app/build/libs/*.jar app.jar

# OpenTelemetry Java Agent 다운로드
# Spring Boot 앱에 자동 계측(auto-instrumentation)을 적용하여
# Tempo로 트레이스 데이터를 전송하기 위해 필요
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

# 파일 소유권을 appuser에게 부여
RUN chown appuser:appgroup app.jar opentelemetry-javaagent.jar

# appuser로 전환 (root 권한 불필요)
USER appuser

# Spring Boot 기본 포트
EXPOSE 8080

# ────────────────────────────────────────────
# 실행 옵션 설명:
#   -Djava.security.egd: Tomcat 시작 속도 개선 (난수 생성기 설정)
#   -javaagent: OTEL Java Agent 로드 (트레이스 자동 계측)
#   spring.profiles.active: 환경변수로 주입받음 (기본값 local)
# ────────────────────────────────────────────
ENV SPRING_PROFILES_ACTIVE=local

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-javaagent:/app/opentelemetry-javaagent.jar", \
  "-jar", "app.jar"]
