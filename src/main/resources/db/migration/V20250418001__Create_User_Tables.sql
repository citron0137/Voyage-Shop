-- 사용자 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 포인트 테이블 생성
CREATE TABLE IF NOT EXISTS user_points (
    user_point_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;