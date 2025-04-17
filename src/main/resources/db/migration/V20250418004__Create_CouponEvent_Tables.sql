-- 쿠폰 이벤트 테이블 생성
CREATE TABLE IF NOT EXISTS coupon_event (
    id VARCHAR(36) PRIMARY KEY,
    benefit_method VARCHAR(255) NOT NULL,
    benefit_amount VARCHAR(255) NOT NULL,
    total_issue_amount BIGINT NOT NULL,
    left_issue_amount BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_coupon_event_benefit_method ON coupon_event(benefit_method); 