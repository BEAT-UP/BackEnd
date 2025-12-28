SET search_path TO public;
-- FCM 토큰 테이블 생성
CREATE TABLE IF NOT EXISTS fcm_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    fcm_token VARCHAR(255) NOT NULL,
    device_type VARCHAR(20) NOT NULL CHECK (device_type IN ('IOS', 'ANDROID')),
    device_id VARCHAR(255), -- 기기 고유 ID (선택사항, 중복 토큰 방지용)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at TIMESTAMP, -- 마지막 알림 전송 성공 시간
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- 외래키 제약조건
    CONSTRAINT fk_fcm_token_user
        FOREIGN KEY (user_id)
        REFERENCES user_account(id)
        ON DELETE CASCADE,

    -- 사용자별 토큰 중복 방지 (같은 사용자가 같은 토큰을 중복 등록하지 못하게)
    CONSTRAINT uq_fcm_token_user_token
        UNIQUE (user_id, fcm_token)
);

-- 인덱스 생성
-- 1. 사용자별 활성 토큰 조회 최적화
CREATE INDEX idx_fcm_token_user_active
    ON fcm_token (user_id, is_active)
    WHERE is_active = TRUE;

-- 2. 토큰으로 사용자 조회 (토큰 업데이트 시 사용)
CREATE INDEX idx_fcm_token_token
    ON fcm_token (fcm_token);

-- 3. 비활성 토큰 정리용 (선택사항)
CREATE INDEX idx_fcm_token_inactive
    ON fcm_token (is_active, updated_at)
    WHERE is_active = FALSE;

-- 주석 추가
COMMENT ON TABLE fcm_token IS '사용자별 FCM 푸시 알림 토큰 관리';
COMMENT ON COLUMN fcm_token.user_id IS '사용자 계정 ID (user_account 참조)';
COMMENT ON COLUMN fcm_token.fcm_token IS 'Firebase Cloud Messaging 토큰';
COMMENT ON COLUMN fcm_token.device_type IS '디바이스 타입 (IOS, ANDROID)';
COMMENT ON COLUMN fcm_token.device_id IS '기기 고유 식별자 (선택사항)';
COMMENT ON COLUMN fcm_token.is_active IS '토큰 활성화 여부 (false 시 알림 미전송)';
COMMENT ON COLUMN fcm_token.last_used_at IS '마지막 알림 전송 성공 시간';