FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
RUN opentelemetry-bootstrap -a install
COPY . .
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://172.20.141.155:4317
ENV OTEL_SERVICE_NAME=ai-server
CMD ["opentelemetry-instrument", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]