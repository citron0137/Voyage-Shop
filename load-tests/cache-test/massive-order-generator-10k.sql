-- massive-order-generator-10000.sql
-- 10,000 orders data generation script

-- 문자 인코딩 설정
SET NAMES utf8mb4;
SET SESSION collation_connection = 'utf8mb4_unicode_ci';

-- Delete existing data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_items;
TRUNCATE TABLE order_discounts;
TRUNCATE TABLE orders;
SET FOREIGN_KEY_CHECKS = 1;

-- Set reference date
SET @now = NOW();

-- Increase recursion depth limit
SET SESSION cte_max_recursion_depth = 10000;
SELECT 'CTE recursion depth limit increased to 10000.' AS INFO;

-- Performance optimization settings
SET autocommit = 0;
SET unique_checks = 0;
SET foreign_key_checks = 0;
SET SESSION bulk_insert_buffer_size = 536870912; -- 512MB
-- Global variable settings (requires admin privileges) - commented out
-- SET SESSION innodb_flush_log_at_trx_commit = 0;
-- SET SESSION sql_log_bin = 0;

-- Performance optimization message
SELECT 'Note: Some global performance settings are commented out. If you have admin privileges, run the following commands:' AS INFO;
SELECT 'SET GLOBAL innodb_flush_log_at_trx_commit = 0; SET GLOBAL sql_log_bin = 0;' AS ADMIN_COMMAND;

-- ============== Order Data Generation ==============

-- Regular order data generation (5,000 orders - 90 days period) - batch processing
SELECT 'Generating 5,000 regular orders in batches...' AS status;

-- 1,000개씩 5번 나누어 생성
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

-- 최근 주문 데이터 생성 (3,000개 - 30일 이내) - 배치 처리로 변경
SELECT 'Generating 3,000 recent orders in batches...' AS status;

-- 1,000개씩 3번 나누어 생성
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

-- 매우 최근 주문 데이터 생성 (1,500개 - 7일 이내) - 배치 처리로 변경
SELECT 'Generating 1,500 very recent orders in batches...' AS status;

-- 1,000개와 500개로 나누어 생성
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
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 7) DAY),
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 7) DAY)
FROM numbers;
COMMIT;

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

-- 오늘 주문 데이터 생성 (500개)
SELECT 'Generating 500 today orders...' AS status;
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
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 1) DAY),
    DATE_SUB(@now, INTERVAL FLOOR(RAND() * 1) DAY)
FROM numbers;

COMMIT;

-- 주문 수 확인
SELECT COUNT(*) AS total_orders FROM orders;

-- ============== 인기 상품 설정 ==============

-- 인기 상품 ID 생성
SET @popular_product_1 = '11111111-1111-1111-1111-111111111111';
SET @popular_product_2 = '22222222-2222-2222-2222-222222222222';
SET @popular_product_3 = '33333333-3333-3333-3333-333333333333';
SET @popular_product_4 = '44444444-4444-4444-4444-444444444444';
SET @popular_product_5 = '55555555-5555-5555-5555-555555555555';
SET @popular_product_6 = '66666666-6666-6666-6666-666666666666';
SET @popular_product_7 = '77777777-7777-7777-7777-777777777777';
SET @popular_product_8 = '88888888-8888-8888-8888-888888888888';
SET @popular_product_9 = '99999999-9999-9999-9999-999999999999';
SET @popular_product_10 = 'AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA';

-- ============== 주문 아이템 생성 (1000개 배치 단위로) ==============

-- 기본 주문 아이템 배치 생성 함수
DROP PROCEDURE IF EXISTS generate_basic_order_items;
DELIMITER //
CREATE PROCEDURE generate_basic_order_items(IN offset_val INT, IN limit_val INT)
BEGIN
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
    FROM (SELECT order_id, created_at, updated_at FROM orders LIMIT limit_val OFFSET offset_val) o;
    
    COMMIT;
END //
DELIMITER ;

-- 기본 주문 아이템 생성 (모든 주문에 1개씩)
SELECT 'Generating basic order items in batches...' AS status;

-- 배치 단위로 처리 (10개 배치)
CALL generate_basic_order_items(0, 1000);
CALL generate_basic_order_items(1000, 1000);
CALL generate_basic_order_items(2000, 1000);
CALL generate_basic_order_items(3000, 1000);
CALL generate_basic_order_items(4000, 1000);
CALL generate_basic_order_items(5000, 1000);
CALL generate_basic_order_items(6000, 1000);
CALL generate_basic_order_items(7000, 1000);
CALL generate_basic_order_items(8000, 1000);
CALL generate_basic_order_items(9000, 1000);

-- 인기 상품 1에 대한 주문 아이템 추가 (5,000개 주문)
SELECT 'Generating popular product #1 order items...' AS status;
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 5000) o;

COMMIT;

-- 인기 상품 2에 대한 주문 아이템 추가 (4,000개 주문)
SELECT 'Generating popular product #2 order items...' AS status;
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 4000) o;

COMMIT;

-- 인기 상품 3에 대한 주문 아이템 추가 (3,000개 주문)
SELECT 'Generating popular product #3 order items...' AS status;
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 3000) o;

COMMIT;

-- 인기 상품 4에 대한 주문 아이템 추가 (2,500개 주문)
SELECT 'Generating popular product #4 order items...' AS status;
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 2500) o;

COMMIT;

-- 인기 상품 5에 대한 주문 아이템 추가 (2,000개 주문)
SELECT 'Generating popular product #5 order items...' AS status;
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 2000) o;

COMMIT;

-- 추가 인기 상품 6-10에 대한 아이템 추가 (각각 1500, 1000, 800, 600, 400개 주문)
SELECT 'Generating additional popular products order items...' AS status;

-- 인기 상품 6
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    @popular_product_6,
    1 + FLOOR(RAND() * 3), 
    40000 + FLOOR(RAND() * 20000), 
    (1 + FLOOR(RAND() * 3)) * (40000 + FLOOR(RAND() * 20000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 1500) o;
COMMIT;

-- 인기 상품 7
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    @popular_product_7,
    1 + FLOOR(RAND() * 3), 
    35000 + FLOOR(RAND() * 25000), 
    (1 + FLOOR(RAND() * 3)) * (35000 + FLOOR(RAND() * 25000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 1000) o;
COMMIT;

-- 인기 상품 8
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    @popular_product_8,
    1 + FLOOR(RAND() * 2), 
    45000 + FLOOR(RAND() * 15000), 
    (1 + FLOOR(RAND() * 2)) * (45000 + FLOOR(RAND() * 15000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 800) o;
COMMIT;

-- 인기 상품 9
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    @popular_product_9,
    1 + FLOOR(RAND() * 2), 
    55000 + FLOOR(RAND() * 10000), 
    (1 + FLOOR(RAND() * 2)) * (55000 + FLOOR(RAND() * 10000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 600) o;
COMMIT;

-- 인기 상품 10
INSERT INTO order_items (order_item_id, order_id, product_id, amount, unit_price, total_price, created_at, updated_at)
SELECT 
    UUID(), 
    order_id, 
    @popular_product_10,
    1 + FLOOR(RAND() * 2), 
    60000 + FLOOR(RAND() * 20000), 
    (1 + FLOOR(RAND() * 2)) * (60000 + FLOOR(RAND() * 20000)),
    created_at,
    updated_at
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 400) o;
COMMIT;

-- 추가 랜덤 주문 아이템 생성 함수 (5,000개 주문에 각 1-3개 아이템 추가)
SELECT 'Generating additional random order items...' AS status;

DROP PROCEDURE IF EXISTS generate_additional_items;
DELIMITER //
CREATE PROCEDURE generate_additional_items(IN batch_size INT, IN total_batches INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE offset_val INT;
    
    WHILE i < total_batches DO
        -- 계산식을 변수에 먼저 할당
        SET offset_val = i * batch_size;
        
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
        FROM (
            SELECT order_id, created_at, updated_at 
            FROM orders 
            ORDER BY RAND() 
            LIMIT batch_size OFFSET offset_val
        ) o
        CROSS JOIN (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3) AS nums
        WHERE RAND() > 0.3;  -- 약 70%의 확률로 추가
        
        COMMIT;
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 5,000개 주문에 추가 아이템 생성 (500개씩 10개 배치)
CALL generate_additional_items(500, 10);

-- ============== 주문 할인 생성 ==============

-- 주문 할인 데이터 생성 (4,000개 주문에 COUPON 할인)
SELECT 'Generating COUPON discounts...' AS status;
DROP PROCEDURE IF EXISTS generate_coupon_discounts;
DELIMITER //
CREATE PROCEDURE generate_coupon_discounts(IN batch_size INT, IN total_batches INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE offset_val INT;
    
    WHILE i < total_batches DO
        -- 계산식을 변수에 먼저 할당
        SET offset_val = i * batch_size;
        
        INSERT INTO order_discounts (order_discount_id, order_id, discount_type, discount_id, discount_amount, created_at, updated_at)
        SELECT 
            UUID(),
            order_id,
            'COUPON',
            UUID(),
            1000 + FLOOR(RAND() * 5000),
            created_at,
            updated_at
        FROM (
            SELECT order_id, created_at, updated_at 
            FROM orders 
            ORDER BY RAND() 
            LIMIT batch_size OFFSET offset_val
        ) o;
        
        COMMIT;
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 4,000개 쿠폰 할인 생성 (1,000개씩 4개 배치)
CALL generate_coupon_discounts(1000, 4);

-- 주문 할인 데이터 생성 (3,000개 주문에 POINT 할인)
SELECT 'Generating POINT discounts...' AS status;
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
    LIMIT 3000
) o;

COMMIT;

-- 주문 할인 데이터 생성 (2,000개 주문에 PROMOTION 할인)
SELECT 'Generating PROMOTION discounts...' AS status;
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
    LIMIT 2000
) o;

COMMIT;

-- ============== Amount Updates ==============

-- Delete existing procedures
DROP PROCEDURE IF EXISTS update_order_totals;
DROP PROCEDURE IF EXISTS update_discount_totals;
DROP PROCEDURE IF EXISTS update_final_amounts;

-- Order item total updates
SELECT 'Updating order totals...' AS status;
DROP TEMPORARY TABLE IF EXISTS temp_order_totals;
CREATE TEMPORARY TABLE temp_order_totals AS
SELECT order_id, SUM(total_price) AS total_amount
FROM order_items
GROUP BY order_id;

-- Update order table
UPDATE orders o
JOIN temp_order_totals t ON o.order_id = t.order_id COLLATE utf8mb4_unicode_ci
SET o.total_amount = t.total_amount;
COMMIT;

-- Order discount total updates
SELECT 'Updating discount totals...' AS status;
DROP TEMPORARY TABLE IF EXISTS temp_discount_totals;
CREATE TEMPORARY TABLE temp_discount_totals AS
SELECT order_id, SUM(discount_amount) AS total_discount
FROM order_discounts
GROUP BY order_id;

-- Update order table with discounts
UPDATE orders o
JOIN temp_discount_totals t ON o.order_id = t.order_id COLLATE utf8mb4_unicode_ci
SET o.total_discount_amount = t.total_discount;
COMMIT;

-- Final amount updates
SELECT 'Updating final amounts...' AS status;
UPDATE orders
SET final_amount = total_amount - total_discount_amount
WHERE final_amount != total_amount - total_discount_amount;
COMMIT;

-- Clean up temporary tables
DROP TEMPORARY TABLE IF EXISTS temp_order_totals;
DROP TEMPORARY TABLE IF EXISTS temp_discount_totals;

-- Delete temporary procedures
DROP PROCEDURE IF EXISTS generate_basic_order_items;
DROP PROCEDURE IF EXISTS generate_additional_items;
DROP PROCEDURE IF EXISTS generate_coupon_discounts;

-- Restore settings
SET autocommit = 1;
SET unique_checks = 1;
SET foreign_key_checks = 1;
-- Restore global variables (originally commented out) - commented out
-- SET SESSION innodb_flush_log_at_trx_commit = 1;
-- SET SESSION sql_log_bin = 1;

-- Check results
SELECT 'Massive 10,000+ data generation completed' AS message;
SELECT COUNT(*) AS orders_count FROM orders;
SELECT COUNT(*) AS order_items_count FROM order_items;
SELECT COUNT(*) AS order_discounts_count FROM order_discounts;

-- Check popular products
SELECT product_id, COUNT(*) AS order_count, SUM(amount) AS total_quantity 
FROM order_items 
GROUP BY product_id
ORDER BY total_quantity DESC 
LIMIT 10; 