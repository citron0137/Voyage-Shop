-- verify-order-data.sql
-- SQL script to verify order data
-- Verifies the integrity and quality of order data, order items, and order discounts

-- Record start time
SELECT NOW() AS 'Verification Start Time';
SET @start_time = NOW();

-- =========================================================
-- 1. Basic Data Count Check
-- =========================================================

-- Check record count for each table
SELECT '1. Basic Data Count Check' AS 'Section';

SELECT 'orders' AS table_name, COUNT(*) AS record_count FROM orders
UNION ALL
SELECT 'order_items' AS table_name, COUNT(*) AS record_count FROM order_items
UNION ALL
SELECT 'order_discounts' AS table_name, COUNT(*) AS record_count FROM order_discounts;

-- =========================================================
-- 2. Order Distribution by Date
-- =========================================================

SELECT '2. Order Distribution by Date' AS 'Section';

-- Check order count by date
SELECT 
    DATE(created_at) AS order_date, 
    COUNT(*) AS order_count
FROM orders
GROUP BY DATE(created_at)
ORDER BY order_date DESC
LIMIT 20;

-- Check order distribution by period
SELECT 
    CASE 
        WHEN created_at >= DATE_SUB(NOW(), INTERVAL 1 DAY) THEN 'Today'
        WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 'Last 7 days'
        WHEN created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 'Last 30 days'
        ELSE 'Before 30 days'
    END AS period,
    COUNT(*) AS order_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM orders), 2) AS percentage
FROM orders
GROUP BY period
ORDER BY 
    CASE period
        WHEN 'Today' THEN 1
        WHEN 'Last 7 days' THEN 2
        WHEN 'Last 30 days' THEN 3
        ELSE 4
    END;

-- =========================================================
-- 3. Order Items and Products Analysis
-- =========================================================

SELECT '3. Order Items and Products Analysis' AS 'Section';

-- Distribution of items per order
SELECT 
    items_per_order,
    COUNT(*) AS order_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM orders), 2) AS percentage
FROM (
    SELECT 
        o.order_id,
        COUNT(oi.order_item_id) AS items_per_order
    FROM orders o
    LEFT JOIN order_items oi ON o.order_id = oi.order_id
    GROUP BY o.order_id
) AS order_items_count
GROUP BY items_per_order
ORDER BY items_per_order;

-- Popular products (by quantity)
SELECT 
    product_id,
    COUNT(DISTINCT order_id) AS order_count,
    SUM(amount) AS total_quantity,
    ROUND(AVG(unit_price), 2) AS avg_price,
    ROUND(SUM(total_price) / SUM(amount), 2) AS avg_unit_price
FROM order_items
GROUP BY product_id
ORDER BY total_quantity DESC
LIMIT 15;

-- Specific popular products (predefined IDs)
SELECT 
    product_id,
    COUNT(DISTINCT order_id) AS order_count,
    SUM(amount) AS total_quantity
FROM order_items
WHERE product_id IN (
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '33333333-3333-3333-3333-333333333333',
    '44444444-4444-4444-4444-444444444444',
    '55555555-5555-5555-5555-555555555555',
    '66666666-6666-6666-6666-666666666666',
    '77777777-7777-7777-7777-777777777777',
    '88888888-8888-8888-8888-888888888888',
    '99999999-9999-9999-9999-999999999999',
    'AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA'
)
GROUP BY product_id
ORDER BY total_quantity DESC;

-- =========================================================
-- 4. Discount Data Analysis
-- =========================================================

SELECT '4. Discount Data Analysis' AS 'Section';

-- Statistics by discount type
SELECT 
    discount_type,
    COUNT(*) AS discount_count,
    ROUND(AVG(discount_amount), 2) AS avg_discount,
    MIN(discount_amount) AS min_discount,
    MAX(discount_amount) AS max_discount,
    SUM(discount_amount) AS total_discount_amount
FROM order_discounts
GROUP BY discount_type;

-- Orders with multiple discounts
SELECT 
    COUNT(DISTINCT o.order_id) AS orders_with_multiple_discounts,
    ROUND(COUNT(DISTINCT o.order_id) * 100.0 / COUNT(DISTINCT orders.order_id), 2) AS percentage
FROM orders
INNER JOIN (
    SELECT 
        order_id
    FROM order_discounts
    GROUP BY order_id
    HAVING COUNT(*) > 1
) AS o ON orders.order_id = o.order_id;

-- =========================================================
-- 5. Data Integrity Check
-- =========================================================

SELECT '5. Data Integrity Check' AS 'Section';

-- Check order-order items relationship
SELECT 
    'Orders without items' AS check_type,
    COUNT(*) AS count
FROM orders o
LEFT JOIN order_items oi ON o.order_id = oi.order_id
WHERE oi.order_id IS NULL
UNION ALL
-- Check order items with valid order ID
SELECT 
    'Items with invalid order ID' AS check_type,
    COUNT(*) AS count
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL
UNION ALL
-- Check amount calculation
SELECT 
    'Orders with incorrect total amount' AS check_type,
    COUNT(*) AS count
FROM orders o
JOIN (
    SELECT 
        order_id, 
        SUM(total_price) AS calculated_total
    FROM order_items
    GROUP BY order_id
) AS calc ON o.order_id = calc.order_id
WHERE ABS(o.total_amount - calc.calculated_total) > 1
UNION ALL
-- Check discount amount calculation
SELECT 
    'Orders with incorrect discount amount' AS check_type,
    COUNT(*) AS count
FROM orders o
LEFT JOIN (
    SELECT 
        order_id, 
        SUM(discount_amount) AS calculated_discount
    FROM order_discounts
    GROUP BY order_id
) AS calc ON o.order_id = calc.order_id
WHERE (calc.calculated_discount IS NOT NULL AND ABS(o.total_discount_amount - calc.calculated_discount) > 1)
   OR (calc.calculated_discount IS NULL AND o.total_discount_amount > 0);

-- =========================================================
-- 6. Order Amount Statistics
-- =========================================================

SELECT '6. Order Amount Statistics' AS 'Section';

-- Order amount statistics
SELECT 
    ROUND(AVG(total_amount), 2) AS avg_total_amount,
    ROUND(AVG(total_discount_amount), 2) AS avg_discount_amount,
    ROUND(AVG(final_amount), 2) AS avg_final_amount,
    
    ROUND(MIN(total_amount), 2) AS min_total_amount,
    ROUND(MIN(total_discount_amount), 2) AS min_discount_amount,
    ROUND(MIN(final_amount), 2) AS min_final_amount,
    
    ROUND(MAX(total_amount), 2) AS max_total_amount,
    ROUND(MAX(total_discount_amount), 2) AS max_discount_amount,
    ROUND(MAX(final_amount), 2) AS max_final_amount
FROM orders;

-- Order distribution by price range
SELECT 
    CASE 
        WHEN final_amount < 10000 THEN 'Under 10K'
        WHEN final_amount < 50000 THEN '10K-50K'
        WHEN final_amount < 100000 THEN '50K-100K'
        WHEN final_amount < 300000 THEN '100K-300K'
        ELSE 'Over 300K'
    END AS price_range,
    COUNT(*) AS order_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM orders), 2) AS percentage
FROM orders
GROUP BY price_range
ORDER BY 
    CASE price_range
        WHEN 'Under 10K' THEN 1
        WHEN '10K-50K' THEN 2
        WHEN '50K-100K' THEN 3
        WHEN '100K-300K' THEN 4
        ELSE 5
    END;

-- =========================================================
-- 7. Execution Time Measurement and Summary
-- =========================================================

SELECT '7. Execution Time Measurement and Summary' AS 'Section';

-- Measure execution time
SELECT 
    NOW() AS end_time,
    TIMEDIFF(NOW(), @start_time) AS execution_time;

-- Summary information
SELECT 
    (SELECT COUNT(*) FROM orders) AS total_orders,
    (SELECT COUNT(*) FROM order_items) AS total_order_items,
    (SELECT COUNT(*) FROM order_discounts) AS total_order_discounts,
    (SELECT COUNT(DISTINCT product_id) FROM order_items) AS unique_products,
    (SELECT SUM(total_amount) FROM orders) AS total_sales_amount,
    (SELECT SUM(total_discount_amount) FROM orders) AS total_discount_amount,
    (SELECT SUM(final_amount) FROM orders) AS total_final_amount; 