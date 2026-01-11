#!/bin/bash

set -e

NAMESPACE="beatup"
IMAGE_TAG=${1:-latest}

echo "🚀 Deploying BeatUp Backend..."

# Namespace 생성
kubectl apply -f k8s/namespace.yaml

# ConfigMap 적용
kubectl apply -f k8s/configmap.yaml

# Secret 적용 (주의: 실제 운영에서는 Sealed Secrets 사용)
kubectl apply -f k8s/secret.yaml

# PostgreSQL 배포
kubectl apply -f k8s/postgres-stateful.yaml

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
