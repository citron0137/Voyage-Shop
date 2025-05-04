-- 주문 테이블 생성
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    payment_id VARCHAR(36) NOT NULL,
    total_amount BIGINT NOT NULL,
    total_discount_amount BIGINT NOT NULL,
    final_amount BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 주문 항목 테이블 생성
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    amount BIGINT NOT NULL,
    unit_price BIGINT NOT NULL,
    total_price BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 주문 할인 테이블 생성
CREATE TABLE IF NOT EXISTS order_discounts (
    order_discount_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    discount_type VARCHAR(50) NOT NULL,
    discount_id VARCHAR(255) NOT NULL,
    discount_amount BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_discounts_order_id ON order_discounts(order_id);
CREATE INDEX idx_order_discounts_discount_type ON order_discounts(discount_type); 