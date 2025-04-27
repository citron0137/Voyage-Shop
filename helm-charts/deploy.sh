#!/bin/bash
# Voyage Shop Helm 차트 배포 스크립트 (Linux/macOS용)

# 텍스트 색상 정의
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

echo -e "${CYAN}Voyage Shop 배포를 시작합니다...${NC}"

# 현재 경로 설정
CURRENT_PATH=$(pwd)
if [[ "$OSTYPE" == "darwin"* ]]; then
  # macOS의 경우 Docker Desktop 마운트 경로로 변환
  HOST_PATH="/run/desktop/mnt/host$(echo $CURRENT_PATH | sed 's/\/\([a-zA-Z]\)\//\/\L\1\//')"
else
  # Linux의 경우 그대로 사용
  HOST_PATH=$CURRENT_PATH
fi

echo -e "${YELLOW}현재 경로: $CURRENT_PATH${NC}"
echo -e "${YELLOW}호스트 경로: $HOST_PATH${NC}"

# MySQL 배포 또는 업그레이드
echo -e "\n${CYAN}MySQL 배포를 시작합니다...${NC}"

# MySQL 릴리스가 이미 존재하는지 확인
if helm list | grep -q "voyage-shop-mysql"; then
  echo -e "${YELLOW}MySQL 릴리스가 이미 존재합니다. 업그레이드를 수행합니다...${NC}"
  helm upgrade voyage-shop-mysql voyage-shop-mysql --values voyage-shop-mysql/values-local.yaml --set basePath.host="$HOST_PATH"
else
  echo -e "${YELLOW}MySQL을 새로 설치합니다...${NC}"
  helm install voyage-shop-mysql voyage-shop-mysql --values voyage-shop-mysql/values-local.yaml --set basePath.host="$HOST_PATH"
fi

# MySQL이 준비될 때까지 대기
echo -e "\n${CYAN}MySQL 파드가 준비될 때까지 대기합니다...${NC}"
kubectl wait --for=condition=ready pod -l app=mysql --timeout=120s

# Voyage Shop 애플리케이션 배포 또는 업그레이드
echo -e "\n${CYAN}Voyage Shop 애플리케이션 배포를 시작합니다...${NC}"

# Voyage Shop 릴리스가 이미 존재하는지 확인
if helm list | grep -q "voyage-shop"; then
  echo -e "${YELLOW}Voyage Shop 릴리스가 이미 존재합니다. 업그레이드를 수행합니다...${NC}"
  helm upgrade voyage-shop voyage-shop --values voyage-shop/values-local.yaml --set database.host=mysql --set basePath.host="$HOST_PATH"
else
  echo -e "${YELLOW}Voyage Shop을 새로 설치합니다...${NC}"
  helm install voyage-shop voyage-shop --values voyage-shop/values-local.yaml --set database.host=mysql --set basePath.host="$HOST_PATH"
fi

# 배포된 리소스 확인
echo -e "\n${CYAN}배포된 Helm 릴리스:${NC}"
helm list

echo -e "\n${CYAN}배포된 파드:${NC}"
kubectl get pods

echo -e "\n${CYAN}배포된 서비스:${NC}"
kubectl get svc

echo -e "\n${GREEN}Voyage Shop이 성공적으로 배포되었습니다!${NC}"
echo -e "${GREEN}애플리케이션에 접속하려면 브라우저에서 http://localhost:3000 을 열어주세요.${NC}" 