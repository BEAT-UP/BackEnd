#!/bin/bash

set -e

NAMESPACE="beatup"
IMAGE_TAG=${1:-latest}

echo "🚀 Deploying BeatUp Backend..."

# 로컬 Kubernetes 컨텍스트 확인 및 설정
CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "")
if [[ "$CURRENT_CONTEXT" == *"docker-desktop"* ]]; then
    echo "✅ Using local Kubernetes context: $CURRENT_CONTEXT"
elif [[ "$CURRENT_CONTEXT" == *"minikube"* ]] || [[ "$CURRENT_CONTEXT" == *"kind"* ]]; then
    echo "✅ Using local Kubernetes context: $CURRENT_CONTEXT"
else
    echo "⚠️  Current context: $CURRENT_CONTEXT"
    echo "🔧 Switching to docker-desktop context..."
    if kubectl config use-context docker-desktop 2>/dev/null; then
        echo "✅ Switched to docker-desktop context"
    else
        echo "❌ Failed to switch context. Please ensure Docker Desktop Kubernetes is enabled."
        echo "Available contexts:"
        kubectl config get-contexts
        exit 1
    fi
fi

# 클러스터 연결 확인
if ! kubectl cluster-info &>/dev/null; then
    echo "❌ Unable to connect to Kubernetes cluster. Please check your cluster status."
    exit 1
fi

# Namespace 생성
kubectl apply -f k8s/namespace.yaml

# ConfigMap 적용
kubectl apply -f k8s/configmap.yaml

# Secret 적용 (주의: 실제 운영에서는 Sealed Secrets 사용)
kubectl apply -f k8s/secret.yaml

# PostgreSQL 배포
kubectl apply -f k8s/postgres-statefulset.yaml

# Redis 배포
kubectl apply -f k8s/redis-deployment.yaml

# 애플리케이션 배포
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# 이미지 업데이트 (GitHub Container Registry 사용)
kubectl set image deployment/beatup-backend \
  beatup-backend=ghcr.io/BEAT-UP/BackEnd:${IMAGE_TAG} \
  -n ${NAMESPACE}

# 롤아웃 상태 확인
kubectl rollout status deployment/beatup-backend -n ${NAMESPACE}

echo "✅ Deployment completed!"