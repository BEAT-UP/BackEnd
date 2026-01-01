#!/bin/bash

# OCI OKE 클러스터 kubectl 설정 스크립트
# 사용법: ./scripts/setup-kubectl.sh

set -e

CLUSTER_ID="ocid1.cluster.oc1.ap-chuncheon-1.aaaaaaaabddmcljq7zlqf676xbgvte3uas6rwdnkwn3bn747eczckiuuqhwa"
REGION="ap-chuncheon-1"

echo "🔧 Setting up kubectl for OCI OKE cluster..."

# OCI CLI 설치 확인
if ! command -v oci &> /dev/null; then
    echo "❌ OCI CLI가 설치되어 있지 않습니다."
    echo "설치 방법:"
    echo "  macOS: brew install oci-cli"
    echo "  또는: bash -c \"\$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)\""
    exit 1
fi

# kubectl 설치 확인
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl이 설치되어 있지 않습니다."
    echo "설치 방법:"
    echo "  macOS: brew install kubectl"
    exit 1
fi

# kubectl 설정
echo "📝 Creating kubeconfig..."
oci ce cluster create-kubeconfig \
  --cluster-id ${CLUSTER_ID} \
  --file $HOME/.kube/config \
  --region ${REGION} \
  --token-version 2.0.0

# 연결 확인
echo "🔍 Verifying connection..."
kubectl get nodes

echo "✅ kubectl 설정이 완료되었습니다!"
echo ""
echo "다음 단계:"
echo "1. Docker Hub에 로그인: docker login"
echo "2. ARM 이미지 빌드: docker buildx build --platform linux/arm64 -t <your-dockerhub-username>/beatup-backend:latest ."
echo "3. 이미지 푸시: docker push <your-dockerhub-username>/beatup-backend:latest"
echo "4. deployment.yaml에서 이미지 경로 수정"
echo "5. 배포 실행: ./scripts/deploy.sh"

