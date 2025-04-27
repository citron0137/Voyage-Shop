#!/bin/bash
# Voyage Shop Helm 차트 배포 스크립트 (Linux/macOS용)

# 텍스트 색상 정의
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}Starting Voyage Shop deployment...${NC}"

# 현재 경로 설정
CURRENT_PATH=$(pwd)
if [[ "$OSTYPE" == "darwin"* ]]; then
  # macOS의 경우 Docker Desktop 마운트 경로로 변환
  HOST_PATH="/run/desktop/mnt/host$(echo $CURRENT_PATH | sed 's/\/\([a-zA-Z]\)\//\/\L\1\//')"
else
  # Linux의 경우 그대로 사용
  HOST_PATH=$CURRENT_PATH
fi

echo -e "${YELLOW}Current path: $CURRENT_PATH${NC}"
echo -e "${YELLOW}Host path: $HOST_PATH${NC}"

# 기존 배포 정리
echo -e "\n${CYAN}Cleaning up existing deployments...${NC}"
DEPLOYMENTS=$(helm list -q)
if [ ! -z "$DEPLOYMENTS" ]; then
  echo -e "${YELLOW}Removing existing Helm releases...${NC}"
  for deployment in $DEPLOYMENTS; do
    helm uninstall $deployment
  done
  
  # Kubernetes 리소스 정리
  echo -e "${YELLOW}Cleaning up remaining Kubernetes resources...${NC}"
  kubectl delete services --all
  kubectl delete pods --all --force --grace-period=0
  
  # 잠시 대기
  echo -e "${YELLOW}Waiting 5 seconds for cleanup to complete...${NC}"
  sleep 5
fi

# 의존성 업데이트
echo -e "\n${CYAN}Updating Helm dependencies...${NC}"
helm dependency update voyage-shop

# Voyage Shop 배포 (의존성에 의해 MySQL 자동 포함)
echo -e "\n${CYAN}Deploying Voyage Shop (including MySQL)...${NC}"
helm install voyage-shop voyage-shop --values voyage-shop/values-local.yaml --set basePath.host="$HOST_PATH"

# 파드 준비 대기
echo -e "\n${CYAN}Waiting for pods to be ready...${NC}"
RETRY_COUNT=0
MAX_RETRIES=10

echo -e "${YELLOW}Waiting for MySQL to be ready...${NC}"
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  MYSQL_POD=$(kubectl get pods -l app=mysql --no-headers 2>/dev/null)
  if [ ! -z "$MYSQL_POD" ] && [[ "$MYSQL_POD" == *"Running"* ]]; then
    echo -e "${GREEN}MySQL is ready!${NC}"
    break
  fi
  RETRY_COUNT=$((RETRY_COUNT+1))
  echo -e "${YELLOW}Waiting for MySQL... ($RETRY_COUNT/$MAX_RETRIES)${NC}"
  sleep 5
done

# 배포된 리소스 확인
echo -e "\n${CYAN}Deployed Helm releases:${NC}"
helm list

echo -e "\n${CYAN}Deployed pods:${NC}"
kubectl get pods

echo -e "\n${CYAN}Deployed services:${NC}"
kubectl get svc

echo -e "\n${GREEN}Voyage Shop has been successfully deployed!${NC}"

# 포트 포워딩 설정
echo -e "\n${CYAN}Setting up port forwarding (8080 -> voyage-shop service)...${NC}"
echo -e "${YELLOW}Running in background. Press Ctrl+C to stop when done.${NC}"
kubectl port-forward svc/voyage-shop 8080:80 &
PF_PID=$!

echo -e "${GREEN}Port forwarding started in background (PID: $PF_PID)${NC}"
echo -e "${GREEN}To access the application, open http://localhost:8080 in your browser.${NC}"
echo -e "\n${YELLOW}To stop port forwarding later, run:${NC}"
echo -e "${YELLOW}kill $PF_PID${NC}"

# 스크립트를 여기서 중단하지 않도록 대기
echo -e "\n${YELLOW}Press Ctrl+C to exit this script${NC}"
wait $PF_PID 