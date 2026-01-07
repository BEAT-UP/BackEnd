#!/bin/bash

# 로컬 Kubernetes 클러스터 kubectl 설정 스크립트
# 사용법: ./scripts/setup-kubectl.sh
# 지원 환경: minikube, kind, Docker Desktop Kubernetes

set -e

echo "🔧 Setting up kubectl for local Kubernetes cluster..."

# kubectl 설치 확인
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl이 설치되어 있지 않습니다."
    echo "설치 방법:"
    echo "  macOS: brew install kubectl"
    exit 1
fi

# 로컬 Kubernetes 클러스터 확인
echo "🔍 Checking local Kubernetes cluster..."

# minikube 확인
if command -v minikube &> /dev/null; then
    echo "📦 Minikube detected. Starting cluster..."
    minikube start || echo "⚠️  Minikube cluster may already be running"
    minikube status
    echo "✅ Minikube cluster is ready!"
    exit 0
fi

# kind 확인
if command -v kind &> /dev/null; then
    echo "📦 Kind detected. Checking for existing cluster..."
    if kind get clusters | grep -q "kind"; then
        echo "✅ Kind cluster already exists"
    else
        echo "Creating kind cluster..."
        kind create cluster --name kind
    fi
    echo "✅ Kind cluster is ready!"
    exit 0
fi

# Docker Desktop Kubernetes 확인
if kubectl config current-context 2>/dev/null | grep -q "docker-desktop\|docker-for-desktop"; then
    echo "✅ Docker Desktop Kubernetes detected"
    kubectl get nodes
    echo "✅ Docker Desktop Kubernetes is ready!"
    exit 0
fi

# 기본 kubeconfig 확인
if [ -f "$HOME/.kube/config" ]; then
    echo "✅ Found existing kubeconfig at ~/.kube/config"
    kubectl config current-context || echo "⚠️  No current context set"
    kubectl get nodes || echo "⚠️  Unable to connect to cluster. Please check your kubeconfig."
else
    echo "⚠️  No kubeconfig found. Please set up your Kubernetes cluster first."
    echo ""
    echo "로컬 Kubernetes 클러스터 설정 방법:"
    echo "1. Minikube: minikube start"
    echo "2. Kind: kind create cluster"
    echo "3. Docker Desktop: Enable Kubernetes in Docker Desktop settings"
    exit 1
fi

echo ""
echo "다음 단계:"
echo "1. 이미지 빌드 및 푸시 (필요시)"
echo "2. 배포 실행: ./scripts/deploy.sh"

