package com.BeatUp.BackEnd.Places.repository;

import com.BeatUp.BackEnd.Places.entity.Place;
import com.BeatUp.BackEnd.common.repository.BaseRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends BaseRepository<Place> {

    /**
     * 카카오 place_id로 조회
     */
    Optional<Place> findByKakaoPlaceId(String kakaoPlaceId);

    /**
     * PostGIS를 사용한 반경 검색
     * ST_DWithin: 두 지점 간 거리가 지정된 반경 내에 있는지 확인
     * ST_Distance: 두 지점 간 실제 거리 계산
     *
     * @param centerPoint 검색 중심점
     * @param radiusMeters 검색 반경(미터)
     * @param categoryCodes 카테고리 코드 리스트(옵션)
     * @param limit 결과 개수 제한
     * @param offset 페이징 오프셋
     * @return 반경 내 장소 리스트(거리순 정렬)
     */
    @Query(value = """
        SELECT p.*,
            ST_DISTANCE(
                p.location::geography,
                :centerPoint::geography
            ) AS distance
        FROM place p
        WHERE ST_DWithin(
            p.location::geography,
            :centerPoint::geography,
            :radiusMeters
        )
        AND (:categoryCodes IS NULL OR cardinality(:categoryCodes) = 0
            OR p.category_code = ANY(:categoryCodes))
        ORDER BY distance
        LIMIT :limit
        OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findNearbyPlacesWithDistance(
            @Param("centerPoint")Point centerPoint,
            @Param("radiusMeters") int radiusMeters,
            @Param("categoryCodes") String[] categoryCodes,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    // 간단한 버전
    @Query(value = """
            SELECT p FROM Place p
            
            """)

    @Override
    default String getEntityName(){
        return "Place";
    }
}
