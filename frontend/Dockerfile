# ── 1단계: React 빌드 ─────────────────────────────────────────
FROM node:22-alpine AS build

WORKDIR /app

# 의존성 먼저 설치 (캐시 활용)
COPY package.json package-lock.json ./
RUN npm ci

# 소스 복사 후 빌드
COPY . .
RUN npm run build

# ── 2단계: Nginx로 정적 파일 서빙 ────────────────────────────
FROM nginx:alpine

# React 빌드 결과물을 Nginx 서빙 경로로 복사
COPY --from=build /app/dist /usr/share/nginx/html

# nginx.conf는 docker-compose에서 볼륨으로 마운트하므로 별도 복사 불필요

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
