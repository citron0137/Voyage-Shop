-- 결제 테이블 생성
CREATE TABLE IF NOT EXISTS payments (
    payment_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    total_payment_amount BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 인덱스 생성
CREATE INDEX idx_payments_user_id ON payments(user_id); 