# Voyage Shop MySQL Helm Chart

이 Helm 차트는 Voyage Shop 애플리케이션을 위한 MySQL 데이터베이스를 Kubernetes에 배포합니다.

## 기능

* MySQL 데이터베이스 배포
* 환경 별 설정 관리 (개발, 스테이징, 프로덕션)
* 데이터 영속성

## 전제 조건

* Kubernetes 1.16+
* Helm 3.0+
* PV 프로비저너 지원 (동적 또는 정적)

## 설치

```
# 독립 설치
helm install voyage-shop-mysql ./helm-charts/voyage-shop-mysql -f ./helm-charts/voyage-shop-mysql/values-dev.yaml

# 주 애플리케이션의 종속성으로 설치
helm install voyage-shop ./helm-charts/voyage-shop -f ./helm-charts/voyage-shop/values-dev.yaml
```

## 환경 별 설정

다음과 같은 환경 별 값 파일이 제공됩니다:

* `values.yaml`: 기본 설정
* `values-dev.yaml`: 개발 환경 설정

## 구성 옵션

| 파라미터 | 설명 | 기본값 |
|----------|-------------|---------|
| `mysql.name` | MySQL 서비스 이름 | `mysql` |
| `mysql.image.repository` | MySQL 이미지 | `mysql` |
| `mysql.image.tag` | MySQL 이미지 태그 | `8.0` |
| `mysql.persistence.hostPath` | MySQL 데이터 저장 경로 | `/run/desktop/mnt/host/c/tmp/mysqldata` |
| `mysql.persistence.size` | MySQL 볼륨 크기 | `1Gi` |

## 환경 별 PersistentVolume 설정

이 Helm 차트는 환경에 맞는 PersistentVolume 설정을 사용합니다:

```yaml
# 개발 환경
mysql:
  persistence:
    hostPath: "/run/desktop/mnt/host/c/tmp/mysqldata-dev"
```

## 업그레이드

```
helm upgrade voyage-shop-mysql ./helm-charts/voyage-shop-mysql -f ./helm-charts/voyage-shop-mysql/values-dev.yaml
```

## 삭제

```
helm delete voyage-shop-mysql
``` 