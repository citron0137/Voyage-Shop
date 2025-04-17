# Voyage-Shop 애플리케이션 성능 이슈 분석 개요

## 개요
Voyage-Shop 애플리케이션에서 데이터 조회 시 성능 지연이 발생할 수 있는 기능들을 분석한 결과입니다. 각 기능에 대한 세부적인 분석과 개선 방안은 별도 문서에서 다루고 있습니다.

## 조회 성능 이슈가 예상되는 기능

### 1. 주문 아이템 순위 조회 (OrderItemRankFacade)
- **문제 기능**: `getRecentTopOrderItemRanks()`
- **원인**: 모든 주문을 메모리에 로드한 후 필터링 및 집계 작업 수행
- **코드 위치**: `application/orderitemrank/OrderItemRankFacade.kt`
- **상세 분석 및 개선 방안**: [주문 아이템 순위 조회 성능 개선 방안](02-order-rank-performance-solution.md)

### 2. 주문 목록 조회 (OrderFacade)
- **문제 기능**: `getAllOrders()`, `getOrdersByUserId()`
- **원인**: 모든 주문을 조회한 후 각 주문의 항목과 할인 정보를 추가로 조회
- **코드 위치**: `application/order/OrderFacade.kt`
- **상세 분석 및 개선 방안**: [주문 목록 조회 성능 개선 방안](03-order-list-performance-solution.md)

### 3. 쿠폰 사용자 정보 조회 (CouponUserService)
- **문제 기능**: `getAllCouponUsers()`, `getAllCouponsByUserId()`
- **원인**: 사용자의 모든 쿠폰 정보를 한 번에 조회
- **코드 위치**: `domain/coupon/CouponUserService.kt`
- **상세 분석 및 개선 방안**: [쿠폰 사용자 정보 조회 성능 개선 방안](04-coupon-user-product-performance-solution.md#part-1-쿠폰-사용자-정보-조회-성능-개선)

### 4. 상품 전체 조회 (ProductFacade)
- **문제 기능**: `getAllProducts()`
- **원인**: 페이지네이션 없이 모든 상품 정보 조회
- **코드 위치**: `application/product/ProductFacade.kt`
- **상세 분석 및 개선 방안**: [상품 전체 조회 성능 개선 방안](04-coupon-user-product-performance-solution.md#part-2-상품-전체-조회-성능-개선)

### 5. 락(Lock)을 사용하는 조회 기능
- **문제 기능**: `findByUserIdWithLock()`, `findByIdWithLock()` 등
- **원인**: Pessimistic Lock 사용으로 인한 트랜잭션 경합
- **코드 위치**: 
  - `infrastructure/userpoint/UserPointJpaRepository.kt`
  - `infrastructure/product/ProductJpaRepository.kt`
- **상세 분석 및 개선 방안**: [락(Lock)을 사용하는 조회 기능 성능 개선 방안](05-lock-performance-solution.md)

## 공통 개선 원칙

성능 이슈 개선을 위한 공통 원칙은 다음과 같습니다:

1. **페이지네이션 적용**
2. **인덱스 최적화**
3. **캐싱 도입**
4. **쿼리 최적화**
5. **데이터 접근 패턴 개선**

각 원칙의 세부적인 구현 방법과 적용 사례는 개별 문서에서 확인할 수 있습니다.

## 부하 테스트

성능 개선 작업 전후에 시스템의 성능 특성을 정확히 파악하고 병목 지점을 식별하기 위한 체계적인 부하 테스트 방안에 대해서는 별도 문서를 참고하세요.

- **부하 테스트 방안**: [부하 테스트 방안](07-load-testing.md)

## 성능 모니터링

성능 개선 효과를 측정하고 지속적인 최적화를 위한 모니터링 구축 방안에 대해서는 별도 문서를 참고하세요.

- **모니터링 구축 방안**: [조회 성능 모니터링 구축 방안](06-performance-monitoring.md)

## 결론

위 기능들은 데이터 증가에 따른 성능 저하 가능성이 높으므로, 실제 서비스 운영 시 모니터링 및 지속적인 성능 개선이 필요합니다. 각 문서에서 제시된 단기, 중기, 장기적 개선 방안을 단계적으로 적용하여 시스템의 안정성과 확장성을 확보할 수 있습니다. 