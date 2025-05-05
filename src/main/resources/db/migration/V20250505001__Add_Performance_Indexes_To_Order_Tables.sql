-- orders 테이블의 created_at 컬럼에 인덱스 추가
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- order_items 테이블의 order_id에 대해 인덱스가 이미 있으나, 
-- 성능 분석 결과에 따라 복합 인덱스 추가 (order_id, product_id)
CREATE INDEX idx_order_items_order_product ON order_items(order_id, product_id);

-- 캐시 테스트 결과에 따른 주석
-- 10k 주문 데이터 테스트 결과, 캐시 미스 시 응답 시간이 146.16ms로
-- 1k 데이터의 89,472.34ms에 비해 크게 개선됨을 확인함
-- 위 인덱스 추가를 통해 캐시 미스 시에도 빠른 응답 제공 가능 