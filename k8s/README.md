# 쿠버네티스 환경 설정

이 디렉토리에는 Voyage-Shop 애플리케이션을 위한 쿠버네티스 설정 파일들이 포함되어 있습니다.

## 디렉토리 구조

```
k8s/
├── config/             # ConfigMap과 Secret 설정
├── deployment/         # Deployment와 PV/PVC 설정
├── service/            # Service 설정
├── ingress/            # Ingress 설정
└── kustomization.yaml  # kustomize 설정 파일
```

## 애플리케이션 도커 이미지 빌드

먼저 애플리케이션 도커 이미지를 빌드해야 합니다:

```bash
docker build -t voyage-shop:latest .
```

Minikube를 사용하는 경우 로컬 이미지를 쿠버네티스에서 사용할 수 있도록 설정:

```bash
# Minikube의 Docker 데몬 사용
eval $(minikube docker-env)
# 이미지 빌드
docker build -t voyage-shop:latest .
```

## 설치 방법

1. kubectl과 kubernetes 클러스터가 설정되어 있는지 확인합니다.

```bash
kubectl version
```

2. 현재 디렉토리에서 다음 명령어를 실행하여 모든 리소스를 적용합니다.

```bash
kubectl apply -k .
```

3. 파드가 실행 중인지 확인합니다.

```bash
kubectl get pods
```

4. 애플리케이션에 접근하기 위한 URL을 확인합니다 (Minikube 사용 시).

```bash
minikube service voyage-shop --url
```

## 환경 변수 및 설정

- MySQL 구성은 ConfigMap과 Secret을 통해 관리됩니다.
- 데이터베이스 이름: `hhplus`
- 사용자 이름: `application`
- 비밀번호: `application`
- 루트 비밀번호: `root`

## 트러블슈팅

### 애플리케이션이 시작되지 않는 경우

데이터베이스 연결을 확인합니다:

```bash
kubectl logs deployment/voyage-shop-app
```

### 데이터베이스 접속 확인

MySQL 파드에 접속하여 데이터베이스가 제대로 설정되었는지 확인:

```bash
kubectl exec -it $(kubectl get pods -l app=mysql -o jsonpath="{.items[0].metadata.name}") -- mysql -u application -papplication hhplus
```

---

## 해결해야 할 이슈

- PersistentVolume 설정에서 hostPath가 환경별로 정적 하드코딩되어 있어 환경에 따른 동적 설정이 필요함 (Helm 또는 Kustomize 사용 검토) 