-- massive-order-generator-1000.sql
-- 1000개 단위의 대량 주문 데이터 생성 스크립트

-- 기존 데이터 삭제
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_items;
TRUNCATE TABLE order_discounts;
TRUNCATE TABLE orders;
SET FOREIGN_KEY_CHECKS = 1;

-- 기준 일자 설정
SET @now = NOW();

-- 성능 설정
SET autocommit = 0;
SET unique_checks = 0;
SET foreign_key_checks = 0;

-- 1. 기본 주문 데이터 생성 (1000개 - 90일 기간)
INSERT INTO orders (order_id, user_id, payment_id, total_amount, total_discount_amount, final_amount, created_at, updated_at)
WITH RECURSIVE numbers AS (
    SELECT 1 AS n 
    UNION ALL 
    SELECT n + 1 FROM numbers WHERE n < 1000
)
SELECT 
    UUID(), UUID(), UUID(),
    10000 + FLOOR(RAND() * 90000),
    FLOOR(RAND() * 10000),
    10000 + FLOOR(RAND() * 90000) - FLOOR(RAND() * 10000),
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 90) DAY),
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 90) DAY)
FROM numbers;

COMMIT;

-- 2. 최근 주문 데이터 생성 (1000개 - 30일 이내)
INSERT INTO orders (order_id, user_id, payment_id, total_amount, total_discount_amount, final_amount, created_at, updated_at)
WITH RECURSIVE numbers AS (
    SELECT 1 AS n 
    UNION ALL 
    SELECT n + 1 FROM numbers WHERE n < 1000
)
SELECT 
    UUID(), UUID(), UUID(),
    10000 + FLOOR(RAND() * 90000),
    FLOOR(RAND() * 10000),
    10000 + FLOOR(RAND() * 90000) - FLOOR(RAND() * 10000),
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 30) DAY),
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 30) DAY)
FROM numbers;

COMMIT;

-- 3. 매우 최근 주문 데이터 생성 (500개 - 7일 이내)
INSERT INTO orders (order_id, user_id, payment_id, total_amount, total_discount_amount, final_amount, created_at, updated_at)
WITH RECURSIVE numbers AS (
    SELECT 1 AS n 
    UNION ALL 
    SELECT n + 1 FROM numbers WHERE n < 500
)
SELECT 
    UUID(), UUID(), UUID(),
    10000 + FLOOR(RAND() * 90000),
    FLOOR(RAND() * 10000),
    10000 + FLOOR(RAND() * 90000) - FLOOR(RAND() * 10000),
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 7) DAY),
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 7) DAY)
FROM numbers;

COMMIT;

-- 4. 오늘 주문 데이터 생성 (200개)
INSERT INTO orders (order_id, user_id, payment_id, total_amount, total_discount_amount, final_amount, created_at, updated_at)
WITH RECURSIVE numbers AS (
    SELECT 1 AS n 
    UNION ALL 
    SELECT n + 1 FROM numbers WHERE n < 200
)
SELECT 
    UUID(), UUID(), UUID(),
    10000 + FLOOR(RAND() * 90000),
    FLOOR(RAND() * 10000),
    10000 + FLOOR(RAND() * 90000) - FLOOR(RAND() * 10000),
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 1) DAY),
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 1) DAY)
FROM numbers;

COMMIT;

-- 주문 수 확인
SELECT COUNT(*) AS total_orders FROM orders;

-- 인기 상품 ID 생성
SET @popular_product_1 = '11111111-1111-1111-1111-111111111111';
SET @popular_product_2 = '22222222-2222-2222-2222-222222222222';
SET @popular_product_3 = '33333333-3333-3333-3333-333333333333';
SET @popular_product_4 = '44444444-4444-4444-4444-444444444444';
SET @popular_product_5 = '55555555-5555-5555-5555-555555555555';

-- 5. 모든 주문에 대한 기본 주문 아이템 추가 (첫번째 배치 1000개)
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    UUID(), 
    1 + FLOOR(RAND() * 3), 
    10000 + FLOOR(RAND() * 50000), 
    (1 + FLOOR(RAND() * 3)) * (10000 + FLOOR(RAND() * 50000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders LIMIT 1000) o;

COMMIT;

-- 6. 모든 주문에 대한 기본 주문 아이템 추가 (두번째 배치 1000개)
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    UUID(), 
    1 + FLOOR(RAND() * 3), 
    10000 + FLOOR(RAND() * 50000), 
    (1 + FLOOR(RAND() * 3)) * (10000 + FLOOR(RAND() * 50000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders LIMIT 1000 OFFSET 1000) o;

COMMIT;

-- 7. 모든 주문에 대한 기본 주문 아이템 추가 (세번째 배치 700개)
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    UUID(), 
    1 + FLOOR(RAND() * 3), 
    10000 + FLOOR(RAND() * 50000), 
    (1 + FLOOR(RAND() * 3)) * (10000 + FLOOR(RAND() * 50000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders LIMIT 700 OFFSET 2000) o;

COMMIT;

-- 8. 인기 상품 1에 대한 주문 아이템 추가 (상위 1000개 주문)
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    @popular_product_1, 
    2 + FLOOR(RAND() * 5), 
    15000 + FLOOR(RAND() * 30000), 
    (2 + FLOOR(RAND() * 5)) * (15000 + FLOOR(RAND() * 30000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 1000) o;

COMMIT;

-- 9. 인기 상품 2에 대한 주문 아이템 추가 (상위 800개 주문)
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    @popular_product_2, 
    2 + FLOOR(RAND() * 4), 
    20000 + FLOOR(RAND() * 20000), 
    (2 + FLOOR(RAND() * 4)) * (20000 + FLOOR(RAND() * 20000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 800) o;

COMMIT;

-- 10. 인기 상품 3에 대한 주문 아이템 추가 (상위 600개 주문)
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    @popular_product_3,
    1 + FLOOR(RAND() * 3), 
    25000 + FLOOR(RAND() * 25000), 
    (1 + FLOOR(RAND() * 3)) * (25000 + FLOOR(RAND() * 25000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 600) o;

COMMIT;

-- 11. 인기 상품 4에 대한 주문 아이템 추가 (상위 400개 주문)
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    @popular_product_4,
    1 + FLOOR(RAND() * 2), 
    30000 + FLOOR(RAND() * 20000), 
    (1 + FLOOR(RAND() * 2)) * (30000 + FLOOR(RAND() * 20000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 400) o;

COMMIT;

-- 12. 인기 상품 5에 대한 주문 아이템 추가 (상위 200개 주문)
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    @popular_product_5,
    1 + FLOOR(RAND() * 2), 
    50000 + FLOOR(RAND() * 30000), 
    (1 + FLOOR(RAND() * 2)) * (50000 + FLOOR(RAND() * 30000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 200) o;

COMMIT;

-- 13. 추가 랜덤 주문 아이템 (1000개 주문에 각 1-3개 아이템 추가)
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(),
    o.order_id,
    UUID(),
    1 + FLOOR(RAND() * 4),
    10000 + FLOOR(RAND() * 90000),
    (1 + FLOOR(RAND() * 4)) * (10000 + FLOOR(RAND() * 90000)),
    o.created_at,
    o.updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 1000) o
CROSS JOIN (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3) AS nums
WHERE RAND() > 0.3;  -- 약 70%의 확률로 추가

COMMIT;

-- 14. 주문 할인 데이터 생성 (약 1000개 주문에 COUPON 할인)
INSERT INTO order_discounts (order_discount_id, order_id, discount_type, discount_id, discount_amount, created_at, updated_at)
SELECT 
    UUID(),
    order_id,
    'COUPON',
    UUID(),
    1000 + FLOOR(RAND() * 5000),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 1000) o;

COMMIT;

-- 15. 주문 할인 데이터 생성 (약 800개 주문에 POINT 할인)
INSERT INTO order_discounts (order_discount_id, order_id, discount_type, discount_id, discount_amount, created_at, updated_at)
SELECT 
    UUID(),
    order_id,
    'POINT',
    UUID(),
    500 + FLOOR(RAND() * 3000),
    created_at,
    updated_at
FROM (
    SELECT o.order_id, o.created_at, o.updated_at 
    FROM orders o 
    LEFT JOIN order_discounts d ON o.order_id = d.order_id
    WHERE d.order_id IS NULL
    ORDER BY RAND() 
    LIMIT 800
) o;

COMMIT;

-- 16. 주문 할인 데이터 생성 (약 600개 주문에 PROMOTION 할인)
INSERT INTO order_discounts (order_discount_id, order_id, discount_type, discount_id, discount_amount, created_at, updated_at)
SELECT 
    UUID(),
    order_id,
    'PROMOTION',
    UUID(),
    2000 + FLOOR(RAND() * 8000),
    created_at,
    updated_at
FROM (
    SELECT o.order_id, o.created_at, o.updated_at 
    FROM orders o 
    LEFT JOIN order_discounts d ON o.order_id = d.order_id
    WHERE d.order_id IS NULL
    ORDER BY RAND() 
    LIMIT 600
) o;

COMMIT;

-- 주문 총액 업데이트 (1000개씩 배치 처리)
UPDATE orders o 
JOIN (
    SELECT order_id, SUM(total_price) AS total
    FROM order_items
    GROUP BY order_id
    LIMIT 1000
) AS i ON o.order_id = i.order_id
SET o.total_amount = i.total;

COMMIT;

UPDATE orders o 
JOIN (
    SELECT order_id, SUM(total_price) AS total
    FROM order_items
    GROUP BY order_id
    LIMIT 1000 OFFSET 1000
) AS i ON o.order_id = i.order_id
SET o.total_amount = i.total;

COMMIT;

UPDATE orders o 
JOIN (
    SELECT order_id, SUM(total_price) AS total
    FROM order_items
    GROUP BY order_id
    LIMIT 1000 OFFSET 2000
) AS i ON o.order_id = i.order_id
SET o.total_amount = i.total;

COMMIT;

-- 주문 할인 총액 업데이트 (1000개씩 배치 처리)
UPDATE orders o 
JOIN (
    SELECT order_id, SUM(discount_amount) AS total_discount
    FROM order_discounts
    GROUP BY order_id
    LIMIT 1000
) AS d ON o.order_id = d.order_id
SET o.total_discount_amount = d.total_discount;

COMMIT;

UPDATE orders o 
JOIN (
    SELECT order_id, SUM(discount_amount) AS total_discount
    FROM order_discounts
    GROUP BY order_id
    LIMIT 1000 OFFSET 1000
) AS d ON o.order_id = d.order_id
SET o.total_discount_amount = d.total_discount;

COMMIT;

UPDATE orders o 
JOIN (
    SELECT order_id, SUM(discount_amount) AS total_discount
    FROM order_discounts
    GROUP BY order_id
    LIMIT 1000 OFFSET 2000
) AS d ON o.order_id = d.order_id
SET o.total_discount_amount = d.total_discount;

COMMIT;

-- 최종 금액 업데이트
UPDATE orders
SET final_amount = total_amount - total_discount_amount
WHERE final_amount != total_amount - total_discount_amount;

COMMIT;

-- 설정 복원
SET autocommit = 1;
SET unique_checks = 1;
SET foreign_key_checks = 1;

-- 결과 확인
SELECT 'Massive 1000x data generation completed' AS message;
SELECT COUNT(*) AS orders_count FROM orders;
SELECT COUNT(*) AS order_items_count FROM order_items;
SELECT COUNT(*) AS order_discounts_count FROM order_discounts;

-- 인기 상품 확인
SELECT product_id, COUNT(*) AS order_count, SUM(amount) AS total_quantity 
FROM order_items 
GROUP BY product_id
ORDER BY total_quantity DESC 
LIMIT 10; 