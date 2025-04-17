# Voyage-Shop 애플리케이션 성능 이슈 분석 문서

## 개요
Voyage-Shop 애플리케이션에서 데이터 조회 시 성능 지연이 발생할 수 있는 기능들을 분석한 결과입니다.

## 조회 성능 이슈가 예상되는 기능

### 1. 주문 아이템 순위 조회 (OrderItemRankFacade)
- **문제 기능**: `getRecentTopOrderItemRanks()`
- **원인**: 모든 주문을 메모리에 로드한 후 필터링 및 집계 작업 수행
- **이슈**: 주문 데이터가 많을 경우 메모리 사용량 증가 및 처리 시간 지연
- **코드 위치**: `application/orderitemrank/OrderItemRankFacade.kt`

### 2. 주문 목록 조회 (OrderFacade)
- **문제 기능**: `getAllOrders()`, `getOrdersByUserId()`
- **원인**: 모든 주문을 조회한 후 각 주문의 항목과 할인 정보를 추가로 조회
- **이슈**: 주문 데이터가 많을 경우 N+1 쿼리 발생 가능성
- **코드 위치**: `application/order/OrderFacade.kt`

### 3. 쿠폰 사용자 정보 조회 (CouponUserService)
- **문제 기능**: `getAllCouponUsers()`, `getAllCouponsByUserId()`
- **원인**: 사용자의 모든 쿠폰 정보를 한 번에 조회
- **이슈**: 쿠폰 데이터가 많을 경우 조회 시간 지연
- **코드 위치**: `domain/coupon/CouponUserService.kt`

### 4. 상품 전체 조회 (ProductFacade)
- **문제 기능**: `getAllProducts()`
- **원인**: 페이지네이션 없이 모든 상품 정보 조회
- **이슈**: 상품 데이터가 많을 경우 응답 시간 지연
- **코드 위치**: `application/product/ProductFacade.kt`

### 5. 락(Lock)을 사용하는 조회 기능
- **문제 기능**: `findByUserIdWithLock()`, `findByIdWithLock()` 등
- **원인**: Pessimistic Lock 사용으로 인한 트랜잭션 경합
- **이슈**: 동시 접속자가 많을 경우 대기 시간 증가
- **코드 위치**: 
  - `infrastructure/userpoint/UserPointJpaRepository.kt`
  - `infrastructure/product/ProductJpaRepository.kt`

## 개선 제안
1. 페이지네이션 도입
2. 인덱스 최적화
3. 캐싱 적용
4. 통계 데이터 별도 저장소 활용
5. 비동기 처리 고려

## 결론
위 기능들은 데이터 증가에 따른 성능 저하 가능성이 높으므로, 실제 서비스 운영 시 모니터링 및 지속적인 성능 개선이 필요합니다. 