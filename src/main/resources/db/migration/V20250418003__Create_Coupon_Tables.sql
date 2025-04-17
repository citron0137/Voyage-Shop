-- 쿠폰 사용자 테이블 생성
CREATE TABLE IF NOT EXISTS coupon_users (
    coupon_user_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    benefit_method VARCHAR(255) NOT NULL,
    benefit_amount VARCHAR(255) NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_coupon_users_user_id FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 인덱스 생성
CREATE INDEX idx_coupon_users_user_id ON coupon_users(user_id); 