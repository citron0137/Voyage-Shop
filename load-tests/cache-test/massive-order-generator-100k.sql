-- massive-order-generator-100000.sql
-- 100,000 orders data generation script

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
SET SESSION cte_max_recursion_depth = 100000;
SELECT 'CTE recursion depth limit increased to 100000.' AS INFO;

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

-- Regular order data generation (50,000 orders - 90 days period) - batch processing
SELECT 'Generating 50,000 regular orders in batches...' AS status;

-- Generate in batches of 2,000 (25 batches)
DELIMITER //
CREATE PROCEDURE generate_regular_orders()
BEGIN
    DECLARE i INT DEFAULT 0;
    WHILE i < 25 DO
        INSERT INTO orders (order_id, user_id, payment_id, total_amount, total_discount_amount, final_amount, created_at, updated_at)
        WITH RECURSIVE numbers AS (
            SELECT 1 AS n 
            UNION ALL 
            SELECT n + 1 FROM numbers WHERE n < 2000
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
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_regular_orders();
DROP PROCEDURE IF EXISTS generate_regular_orders;

-- Recent order data generation (30,000 orders - 30 days period) - batch processing
SELECT 'Generating 30,000 recent orders in batches...' AS status;

-- Generate in batches of 2,000 (15 batches)
DELIMITER //
CREATE PROCEDURE generate_recent_orders()
BEGIN
    DECLARE i INT DEFAULT 0;
    WHILE i < 15 DO
        INSERT INTO orders (order_id, user_id, payment_id, total_amount, total_discount_amount, final_amount, created_at, updated_at)
        WITH RECURSIVE numbers AS (
            SELECT 1 AS n 
            UNION ALL 
            SELECT n + 1 FROM numbers WHERE n < 2000
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
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_recent_orders();
DROP PROCEDURE IF EXISTS generate_recent_orders;

-- Very recent order data generation (15,000 orders - 7 days period) - batch processing
SELECT 'Generating 15,000 very recent orders in batches...' AS status;

-- Generate in batches of 2,000 (7.5 -> 8 batches)
DELIMITER //
CREATE PROCEDURE generate_very_recent_orders()
BEGIN
    DECLARE i INT DEFAULT 0;
    WHILE i < 7 DO
        INSERT INTO orders (order_id, user_id, payment_id, total_amount, total_discount_amount, final_amount, created_at, updated_at)
        WITH RECURSIVE numbers AS (
            SELECT 1 AS n 
            UNION ALL 
            SELECT n + 1 FROM numbers WHERE n < 2000
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
        SET i = i + 1;
    END WHILE;
    
    -- One batch of 1,000 to reach 15,000
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
END //
DELIMITER ;

CALL generate_very_recent_orders();
DROP PROCEDURE IF EXISTS generate_very_recent_orders;

-- Today order data generation (5,000 orders)
SELECT 'Generating 5,000 today orders in batches...' AS status;

-- Generate in batches of 1,000 (5 batches)
DELIMITER //
CREATE PROCEDURE generate_today_orders()
BEGIN
    DECLARE i INT DEFAULT 0;
    WHILE i < 5 DO
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
            DATE_SUB(@now, INTERVAL FLOOR(RAND() * 1) DAY),
            DATE_SUB(@now, INTERVAL FLOOR(RAND() * 1) DAY)
        FROM numbers;
        
        COMMIT;
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_today_orders();
DROP PROCEDURE IF EXISTS generate_today_orders;

-- Verify order count
SELECT COUNT(*) AS total_orders FROM orders;

-- ============== Popular Product Setup ==============

-- Set popular product IDs
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

-- ============== Order Item Generation (in batches) ==============

-- Basic order item batch generation
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

-- Generate basic order items (all orders get 1 item)
SELECT 'Generating basic order items in batches...' AS status;

-- Process in 10K batches for better performance
DELIMITER //
CREATE PROCEDURE generate_all_basic_items()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE total_batches INT DEFAULT 10;
    DECLARE batch_size INT DEFAULT 10000;
    
    WHILE i < total_batches DO
        CALL generate_basic_order_items(i * batch_size, batch_size);
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_all_basic_items();
DROP PROCEDURE IF EXISTS generate_all_basic_items;

-- Generate popular product order items
-- Popular product 1 (50,000 orders)
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 50000) o;
COMMIT;

-- Popular product 2 (40,000 orders)
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 40000) o;
COMMIT;

-- Popular product 3 (30,000 orders)
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 30000) o;
COMMIT;

-- Popular product 4 (25,000 orders)
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 25000) o;
COMMIT;

-- Popular product 5 (20,000 orders)
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 20000) o;
COMMIT;

-- Additional popular products 6-10
SELECT 'Generating additional popular products order items...' AS status;

-- Popular product 6 (15,000 orders)
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 15000) o;
COMMIT;

-- Popular product 7 (10,000 orders)
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 10000) o;
COMMIT;

-- Popular product 8 (8,000 orders)
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 8000) o;
COMMIT;

-- Popular product 9 (6,000 orders)
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 6000) o;
COMMIT;

-- Popular product 10 (4,000 orders)
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
FROM (SELECT order_id, created_at, updated_at FROM orders ORDER BY RAND() LIMIT 4000) o;
COMMIT;

-- Additional random order items (50,000 orders)
SELECT 'Generating additional random order items...' AS status;

-- Create function to generate additional items in batches
DROP PROCEDURE IF EXISTS generate_additional_items;
DELIMITER //
CREATE PROCEDURE generate_additional_items(IN batch_size INT, IN total_batches INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE offset_val INT;
    
    WHILE i < total_batches DO
        -- Assign calculation to variable first
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
        WHERE RAND() > 0.3;  -- About 70% probability
        
        COMMIT;
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- Generate additional items in 5,000 order batches (10 batches)
CALL generate_additional_items(5000, 10);
DROP PROCEDURE IF EXISTS generate_additional_items;

-- ============== Order Discount Generation ==============

-- Generate coupon discounts (40,000 orders)
SELECT 'Generating COUPON discounts...' AS status;
DROP PROCEDURE IF EXISTS generate_coupon_discounts;
DELIMITER //
CREATE PROCEDURE generate_coupon_discounts(IN batch_size INT, IN total_batches INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE offset_val INT;
    
    WHILE i < total_batches DO
        -- Assign calculation to variable first
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

-- Generate 40,000 coupon discounts in 5,000 order batches (8 batches)
CALL generate_coupon_discounts(5000, 8);
DROP PROCEDURE IF EXISTS generate_coupon_discounts;

-- Generate point discounts (30,000 orders)
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
    LIMIT 30000
) o;
COMMIT;

-- Generate promotion discounts (20,000 orders)
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
    LIMIT 20000
) o;
COMMIT;

-- ============== Amount Updates ==============

-- Order item total updates
SELECT 'Updating order totals...' AS status;
DROP TEMPORARY TABLE IF EXISTS temp_order_totals;
CREATE TEMPORARY TABLE temp_order_totals AS
SELECT order_id, SUM(total_price) AS total_amount
FROM order_items
GROUP BY order_id;

-- Update order table in batches
DROP PROCEDURE IF EXISTS update_order_totals_in_batches;
DELIMITER //
CREATE PROCEDURE update_order_totals_in_batches()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE curr_order_id VARCHAR(36);
    DECLARE curr_total_amount DECIMAL(10,2);
    DECLARE cur CURSOR FOR SELECT order_id, total_amount FROM temp_order_totals;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    batch_loop: LOOP
        FETCH cur INTO curr_order_id, curr_total_amount;
        IF done THEN
            LEAVE batch_loop;
        END IF;
        
        UPDATE orders SET total_amount = curr_total_amount 
        WHERE order_id = curr_order_id COLLATE utf8mb4_unicode_ci;
        
        IF MOD(ROW_COUNT(), 1000) = 0 THEN
            COMMIT;
        END IF;
    END LOOP;
    
    COMMIT;
    CLOSE cur;
END //
DELIMITER ;

CALL update_order_totals_in_batches();
DROP PROCEDURE IF EXISTS update_order_totals_in_batches;

-- Order discount total updates
SELECT 'Updating discount totals...' AS status;
DROP TEMPORARY TABLE IF EXISTS temp_discount_totals;
CREATE TEMPORARY TABLE temp_discount_totals AS
SELECT order_id, SUM(discount_amount) AS total_discount
FROM order_discounts
GROUP BY order_id;

-- Update order table with discounts in batches
DROP PROCEDURE IF EXISTS update_discount_totals_in_batches;
DELIMITER //
CREATE PROCEDURE update_discount_totals_in_batches()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE curr_order_id VARCHAR(36);
    DECLARE curr_total_discount DECIMAL(10,2);
    DECLARE cur CURSOR FOR SELECT order_id, total_discount FROM temp_discount_totals;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    batch_loop: LOOP
        FETCH cur INTO curr_order_id, curr_total_discount;
        IF done THEN
            LEAVE batch_loop;
        END IF;
        
        UPDATE orders SET total_discount_amount = curr_total_discount 
        WHERE order_id = curr_order_id COLLATE utf8mb4_unicode_ci;
        
        IF MOD(ROW_COUNT(), 1000) = 0 THEN
            COMMIT;
        END IF;
    END LOOP;
    
    COMMIT;
    CLOSE cur;
END //
DELIMITER ;

CALL update_discount_totals_in_batches();
DROP PROCEDURE IF EXISTS update_discount_totals_in_batches;

-- Final amount updates in batches
SELECT 'Updating final amounts...' AS status;
DROP PROCEDURE IF EXISTS update_final_amounts_in_batches;
DELIMITER //
CREATE PROCEDURE update_final_amounts_in_batches()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE curr_order_id VARCHAR(36);
    DECLARE cur CURSOR FOR SELECT order_id FROM orders 
                          WHERE final_amount != total_amount - total_discount_amount;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    batch_loop: LOOP
        FETCH cur INTO curr_order_id;
        IF done THEN
            LEAVE batch_loop;
        END IF;
        
        UPDATE orders SET final_amount = total_amount - total_discount_amount 
        WHERE order_id = curr_order_id COLLATE utf8mb4_unicode_ci;
        
        IF MOD(ROW_COUNT(), 1000) = 0 THEN
            COMMIT;
        END IF;
    END LOOP;
    
    COMMIT;
    CLOSE cur;
END //
DELIMITER ;

CALL update_final_amounts_in_batches();
DROP PROCEDURE IF EXISTS update_final_amounts_in_batches;

-- Clean up temporary tables
DROP TEMPORARY TABLE IF EXISTS temp_order_totals;
DROP TEMPORARY TABLE IF EXISTS temp_discount_totals;

-- Restore settings
SET autocommit = 1;
SET unique_checks = 1;
SET foreign_key_checks = 1;
-- Restore global variables (originally commented out) - commented out
-- SET SESSION innodb_flush_log_at_trx_commit = 1;
-- SET SESSION sql_log_bin = 1;

-- Check results
SELECT 'Massive 100,000+ data generation completed' AS message;
SELECT COUNT(*) AS orders_count FROM orders;
SELECT COUNT(*) AS order_items_count FROM order_items;
SELECT COUNT(*) AS order_discounts_count FROM order_discounts;

-- Check popular products
SELECT product_id, COUNT(*) AS order_count, SUM(amount) AS total_quantity 
FROM order_items 
GROUP BY product_id
ORDER BY total_quantity DESC 
LIMIT 10; 