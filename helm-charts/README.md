# Voyage Shop Helm 차트

이 디렉토리는 Voyage Shop 애플리케이션과 그 의존성을 Kubernetes에 배포하기 위한 Helm 차트를 포함하고 있습니다.

## 구조

- `voyage-shop/`: 메인 애플리케이션 Helm 차트
- `voyage-shop-mysql/`: MySQL 데이터베이스 Helm 차트

## 간편 배포 스크립트

더 쉬운 배포를 위해 다음 스크립트가 제공됩니다:

- `deploy.ps1`: Windows PowerShell용 배포 스크립트
- `deploy.sh`: Linux/macOS용 배포 스크립트 (실행 전 `chmod +x deploy.sh`로 권한 설정 필요)

### 스크립트 사용 방법:
```bash
# Windows PowerShell
./deploy.ps1

# Linux/macOS
chmod +x deploy.sh
./deploy.sh
```

## 수동 설치 방법

각 차트는 `values.yaml` 파일에 기본 설정이 포함되어 있으며, 환경별 설정은 `values-local.yaml`과 같은 별도 파일에 정의되어 있습니다.

### MySQL 배포:
```bash
# Windows PowerShell
$currentPath = $pwd.Path.Replace('\', '/').Replace('C:/', '/run/desktop/mnt/host/c/')
helm install voyage-shop-mysql voyage-shop-mysql --values voyage-shop-mysql/values-local.yaml --set basePath.host="$currentPath"

# Linux/macOS
helm install voyage-shop-mysql voyage-shop-mysql --values voyage-shop-mysql/values-local.yaml --set basePath.host="$(pwd)"
```

### Voyage Shop 애플리케이션 배포:
```bash
# Windows PowerShell
$currentPath = $pwd.Path.Replace('\', '/').Replace('C:/', '/run/desktop/mnt/host/c/')
helm install voyage-shop voyage-shop --values voyage-shop/values-local.yaml --set database.host=mysql --set basePath.host="$currentPath"

# Linux/macOS
helm install voyage-shop voyage-shop --values voyage-shop/values-local.yaml --set database.host=mysql --set basePath.host="$(pwd)"
```

## 동적 값 설정

Helm의 `--set` 옵션을 사용하여 설치 시 동적으로 값을 설정할 수 있습니다:

```bash
# 단일 값 설정
helm install ... --set key=value

# 여러 값 설정
helm install ... --set key1=value1,key2=value2

# 중첩된 값 설정
helm install ... --set parent.child=value
```

## 주의사항

- `basePath.host`는 Docker Desktop과 Kubernetes 간의 파일 공유를 위한 경로입니다.
- Windows에서는 경로 변환이 필요합니다(`C:/path` → `/run/desktop/mnt/host/c/path`).
- Minikube를 사용하는 경우 다른 경로 변환이 필요할 수 있습니다.

## 문제 해결

- **경로 문제**: Windows에서 경로 변환이 올바르게 되었는지 확인하세요.
- **권한 문제**: 파일 시스템 권한이 올바르게 설정되었는지 확인하세요.
- **배포 확인**: `helm list`와 `kubectl get pods`로 배포 상태를 확인할 수 있습니다.

## 사전 요구사항

- Kubernetes 클러스터
- Helm 3.x
- Docker Desktop (로컬 개발 시)

## 헬름 차트 디버깅

설치 전에 템플릿 렌더링 결과를 확인하려면:

```bash
# MySQL 차트 템플릿 렌더링 결과 확인
helm template voyage-shop-mysql ./voyage-shop-mysql --values ./voyage-shop-mysql/values-local.yaml --set basePath.host="$(pwd)"

# Voyage Shop 차트 템플릿 렌더링 결과 확인
helm template voyage-shop ./voyage-shop --values ./voyage-shop/values-local.yaml --set basePath.host="$(pwd)" --set database.host=mysql
```

## 차트 제거

```bash
helm uninstall voyage-shop
helm uninstall voyage-shop-mysql
```

## 트러블슈팅

### 경로 문제

Windows에서 Docker Desktop을 사용할 때 호스트 경로 매핑에 문제가 있는 경우:

1. 데이터 디렉토리가 존재하는지 확인
2. Docker Desktop 설정에서 리소스 > 파일 공유에 해당 경로가 추가되어 있는지 확인
3. `basePath.host` 값이 `/run/desktop/mnt/host/c/...` 형식으로 올바르게 지정되었는지 확인 