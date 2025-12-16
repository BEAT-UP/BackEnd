-- POSTGIS 확장 활성화
CREATE EXTENSION IF NOT EXISTS postgis;

-- Place 테이블 생성
CREATE TABLE IF NOT EXISTS place(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kakao_place_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    road_address VARCHAR(500),
    phone VARCHAR(20),
    category_code VARCHAR(10),
    category_name VARCHAR(50),
    location geometry(Point, 4326) NOT NULL,
    place_url VARCHAR(500),
    last_synced_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- GIST 인덱스 생성(공간 쿼리 성능 향상)
-- GIST: Generalized Search Tree, 공간 데이터에 최적화된 인덱스
CREATE INDEX idx_place_location_gist ON place USING GIST (location);

-- 카테고리 인덱스
CREATE INDEX idx_place_category ON place (category_code);

-- 카카오 ID 인덱스
CREATE INDEX idx_place_kakao_id ON place (kakao_place_id);

-- 주석 추가
COMMENT ON TABLE place IS '주변 장소 정보(PostGIS 사용)';
COMMENT ON COLUMN place.location IS 'WGS84 좌표계 (SRID 4326), Point 타입';
COMMENT ON INDEX idx_place_location_gist IS '공연 검색 최적화를 위한 GIST 인덱스';

