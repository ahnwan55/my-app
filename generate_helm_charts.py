import os

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

# ============================
# Backend Chart
# ============================
write_file('charts/backend/Chart.yaml', """
apiVersion: v2
name: backend
description: A Helm chart for the Bookjjeok Backend
type: application
version: 0.1.0
appVersion: "1.0.0"
""")

write_file('charts/backend/values.yaml', """
replicaCount: 2
image:
  repository: 558231431060.dkr.ecr.ap-northeast-2.amazonaws.com/bookjjeok_cloud_be
  pullPolicy: IfNotPresent
  tag: "latest"
imagePullSecrets: []
nameOverride: ""
fullnameOverride: "backend"
resources:
  requests:
    cpu: 200m
    memory: 512Mi
  limits:
    cpu: 1000m
    memory: 2Gi
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 5
  targetCPUUtilizationPercentage: 70
service:
  type: ClusterIP
  port: 80
  targetPort: 8080
ingress:
  enabled: false
config:
  SPRING_PROFILES_ACTIVE: "prod"
  AI_SERVER_URL: "http://ai-server"
externalSecrets:
  enabled: true
  refreshInterval: "1h"
  secretStoreRef:
    name: "aws-secretsmanager"
    kind: "ClusterSecretStore"
  secretKey: "bookjjeok/test/backend"
secretStore:
  enabled: false
cronjob:
  enabled: false
""")

write_file('charts/backend/values-vpc1.yaml', """
service:
  type: ClusterIP
ingress:
  enabled: true
  className: "alb"
  annotations:
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}]'
    alb.ingress.kubernetes.io/group.name: "shared-alb"
  hosts:
    - host: ""
      paths:
        - path: /
          pathType: Prefix
externalSecrets:
  secretStoreRef:
    name: "aws-secretsmanager"
    kind: "ClusterSecretStore"
  secretKey: "bookjjeok/test/backend"
""")

write_file('charts/backend/values-vpc2.yaml', """
imagePullSecrets:
  - name: ecr-secret
service:
  type: LoadBalancer
externalSecrets:
  secretStoreRef:
    name: "aws-secretsmanager-vpc2"
    kind: "SecretStore"
  secretKey: "bookjjeok/vpc2/backend"
secretStore:
  enabled: true
  name: aws-secretsmanager-vpc2
  namespace: bookjjeok
  region: ap-northeast-2
cronjob:
  enabled: true
  awsAccountId: "558231431060"
  region: "ap-northeast-2"
  schedule: "0 */10 * * *"
""")

write_file('charts/backend/templates/deployment.yaml', """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "backend.fullname" . }}
  labels:
    {{- include "backend.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      {{- include "backend.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "backend.selectorLabels" . | nindent 8 }}
    spec:
      {{- with .Values.imagePullSecrets }}
      imagePullSecrets:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: 8080
              protocol: TCP
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 120
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 180
            periodSeconds: 20
          envFrom:
            - configMapRef:
                name: {{ include "backend.fullname" . }}-config
            - secretRef:
                name: {{ include "backend.fullname" . }}-secrets
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
""")

write_file('charts/backend/templates/service.yaml', """
apiVersion: v1
kind: Service
metadata:
  name: {{ include "backend.fullname" . }}
  labels:
    {{- include "backend.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}
  ports:
    - port: {{ .Values.service.port }}
      targetPort: {{ .Values.service.targetPort }}
      protocol: TCP
      name: http
  selector:
    {{- include "backend.selectorLabels" . | nindent 4 }}
""")

write_file('charts/backend/templates/configmap.yaml', """
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ include "backend.fullname" . }}-config
data:
  {{- range $key, $val := .Values.config }}
  {{ $key }}: {{ $val | quote }}
  {{- end }}
""")

write_file('charts/backend/templates/hpa.yaml', """
{{- if .Values.autoscaling.enabled }}
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: {{ include "backend.fullname" . }}
  labels:
    {{- include "backend.labels" . | nindent 4 }}
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: {{ include "backend.fullname" . }}
  minReplicas: {{ .Values.autoscaling.minReplicas }}
  maxReplicas: {{ .Values.autoscaling.maxReplicas }}
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: {{ .Values.autoscaling.targetCPUUtilizationPercentage }}
{{- end }}
""")

write_file('charts/backend/templates/external-secret.yaml', """
{{- if .Values.externalSecrets.enabled }}
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: {{ include "backend.fullname" . }}-secrets
spec:
  refreshInterval: {{ .Values.externalSecrets.refreshInterval }}
  secretStoreRef:
    name: {{ .Values.externalSecrets.secretStoreRef.name }}
    kind: {{ .Values.externalSecrets.secretStoreRef.kind }}
  target:
    name: {{ include "backend.fullname" . }}-secrets
    creationPolicy: Owner
  dataFrom:
  - extract:
      key: {{ .Values.externalSecrets.secretKey }}
{{- end }}
""")

write_file('charts/backend/templates/secret-store.yaml', """
{{- if .Values.secretStore.enabled }}
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: {{ .Values.secretStore.name }}
  {{- if .Values.secretStore.namespace }}
  namespace: {{ .Values.secretStore.namespace }}
  {{- end }}
spec:
  provider:
    aws:
      service: SecretsManager
      region: {{ .Values.secretStore.region }}
{{- end }}
""")

write_file('charts/backend/templates/ingress.yaml', """
{{- if .Values.ingress.enabled -}}
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: {{ include "backend.fullname" . }}
  labels:
    {{- include "backend.labels" . | nindent 4 }}
  {{- with .Values.ingress.annotations }}
  annotations:
    {{- toYaml . | nindent 4 }}
  {{- end }}
spec:
  ingressClassName: {{ .Values.ingress.className }}
  rules:
    {{- range .Values.ingress.hosts }}
    - http:
        paths:
          {{- range .paths }}
          - path: {{ .path }}
            pathType: {{ .pathType }}
            backend:
              service:
                name: {{ include "backend.fullname" $ }}
                port:
                  number: {{ $.Values.service.port }}
          {{- end }}
      {{- if .host }}
      host: {{ .host | quote }}
      {{- end }}
    {{- end }}
{{- end }}
""")

write_file('charts/backend/templates/ecr-cronjob.yaml', """
{{- if .Values.cronjob.enabled }}
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: ecr-secret-manager
rules:
- apiGroups: [""]
  resources: ["secrets"]
  verbs: ["get", "create", "update", "patch", "delete"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: ecr-secret-manager-binding
subjects:
- kind: ServiceAccount
  name: default
roleRef:
  kind: Role
  name: ecr-secret-manager
  apiGroup: rbac.authorization.k8s.io
---
apiVersion: batch/v1
kind: CronJob
metadata:
  name: ecr-token-refresh
spec:
  schedule: {{ .Values.cronjob.schedule | quote }}
  successfulJobsHistoryLimit: 1
  failedJobsHistoryLimit: 1
  jobTemplate:
    spec:
      template:
        spec:
          serviceAccountName: default
          containers:
          - name: ecr-token-refresh
            image: amazon/aws-cli:latest
            command:
            - /bin/sh
            - -c
            - |-
              echo "Fetching ECR token..."
              TOKEN=$(aws ecr get-login-password --region {{ .Values.cronjob.region }})
              if [ $? -ne 0 ]; then exit 1; fi
              kubectl create secret docker-registry ecr-secret \
                --docker-server={{ .Values.cronjob.awsAccountId }}.dkr.ecr.{{ .Values.cronjob.region }}.amazonaws.com \
                --docker-username=AWS \
                --docker-password="${TOKEN}" \
                --dry-run=client -o yaml | kubectl apply -f -
            env:
            - name: AWS_DEFAULT_REGION
              value: {{ .Values.cronjob.region }}
          restartPolicy: OnFailure
{{- end }}
""")

write_file('charts/backend/templates/_helpers.tpl', """
{{- define "backend.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "backend.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "backend.labels" -}}
helm.sh/chart: {{ include "backend.name" . }}-{{ .Chart.Version | replace "+" "_" }}
{{ include "backend.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "backend.selectorLabels" -}}
app: {{ include "backend.name" . }}
{{- end }}
""")

# ============================
# AI Chart
# ============================
write_file('charts/ai/Chart.yaml', """
apiVersion: v2
name: ai-server
description: A Helm chart for the Bookjjeok AI Server
type: application
version: 0.1.0
""")

write_file('charts/ai/values.yaml', """
replicaCount: 2
image:
  repository: 558231431060.dkr.ecr.ap-northeast-2.amazonaws.com/bookjjeok_cloud_ai
  pullPolicy: IfNotPresent
  tag: "latest"
imagePullSecrets: []
nameOverride: ""
fullnameOverride: "ai-server"
resources:
  requests: {cpu: 100m, memory: 256Mi}
  limits: {cpu: 500m, memory: 1Gi}
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 5
  targetCPUUtilizationPercentage: 70
service:
  type: ClusterIP
  port: 80
  targetPort: 8000
envFromSecret: "backend-secrets"
""")

write_file('charts/ai/values-vpc1.yaml', """
service:
  type: ClusterIP
""")

write_file('charts/ai/values-vpc2.yaml', """
imagePullSecrets:
  - name: ecr-secret
service:
  type: LoadBalancer
""")

write_file('charts/ai/templates/deployment.yaml', """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "ai.fullname" . }}
  labels:
    {{- include "ai.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      {{- include "ai.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "ai.selectorLabels" . | nindent 8 }}
    spec:
      {{- with .Values.imagePullSecrets }}
      imagePullSecrets:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: 8000
              protocol: TCP
          envFrom:
            - secretRef:
                name: {{ .Values.envFromSecret }}
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
""")

write_file('charts/ai/templates/service.yaml', """
apiVersion: v1
kind: Service
metadata:
  name: {{ include "ai.fullname" . }}
  labels:
    {{- include "ai.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}
  ports:
    - port: {{ .Values.service.port }}
      targetPort: {{ .Values.service.targetPort }}
      protocol: TCP
      name: http
  selector:
    {{- include "ai.selectorLabels" . | nindent 4 }}
""")

write_file('charts/ai/templates/hpa.yaml', """
{{- if .Values.autoscaling.enabled }}
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: {{ include "ai.fullname" . }}
  labels:
    {{- include "ai.labels" . | nindent 4 }}
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: {{ include "ai.fullname" . }}
  minReplicas: {{ .Values.autoscaling.minReplicas }}
  maxReplicas: {{ .Values.autoscaling.maxReplicas }}
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: {{ .Values.autoscaling.targetCPUUtilizationPercentage }}
{{- end }}
""")

write_file('charts/ai/templates/_helpers.tpl', """
{{- define "ai.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "ai.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "ai.labels" -}}
helm.sh/chart: {{ include "ai.name" . }}-{{ .Chart.Version | replace "+" "_" }}
{{ include "ai.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "ai.selectorLabels" -}}
app: {{ include "ai.name" . }}
{{- end }}
""")

print("All Helm charts generated successfully.")
