# Voyage Shop Helm Chart

이 Helm 차트는 Voyage Shop 애플리케이션을 Kubernetes에 배포하기 위한 것입니다.

## 기능

* Spring Boot 애플리케이션 배포
* 개발 환경 설정 관리
* 인그레스 설정

## 전제 조건

* Kubernetes 1.16+
* Helm 3.0+
* 인그레스 컨트롤러 (선택 사항)

## 구성

이 애플리케이션은 다음 구성 요소로 이루어져 있습니다:
* 애플리케이션 서버
* 외부 MySQL 데이터베이스 연결 설정

## 설치

```
# 개발 환경 설치
helm install voyage-shop ./helm-charts/voyage-shop -f ./helm-charts/voyage-shop/values-dev.yaml
```

## 환경 설정

다음과 같은 환경 값 파일이 제공됩니다:

* `values.yaml`: 기본 설정
* `values-dev.yaml`: 개발 환경 설정

## 구성 옵션

| 파라미터 | 설명 | 기본값 |
|----------|-------------|---------|
| `app.replicaCount` | 애플리케이션 레플리카 수 | `1` |
| `app.image.repository` | 애플리케이션 이미지 | `voyage-shop` |
| `app.image.tag` | 애플리케이션 이미지 태그 | `latest` |
| `database.host` | 데이터베이스 호스트 | `mysql` |
| `database.port` | 데이터베이스 포트 | `3306` |

## 데이터베이스 연결

MySQL 데이터베이스 연결 설정:

```yaml
database:
  host: your-mysql-host
  port: 3306
  user: your-user
  name: your-database
  password: your-password
```

## 업그레이드

```
helm upgrade voyage-shop ./helm-charts/voyage-shop -f ./helm-charts/voyage-shop/values-dev.yaml
```

## 삭제

```
helm delete voyage-shop
``` 