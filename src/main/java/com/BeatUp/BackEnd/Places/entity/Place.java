package com.BeatUp.BackEnd.Places.entity;

import com.BeatUp.BackEnd.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "place", indexes = {
        @Index(name = "idx_place_location_gist", columnList = "location"),
        @Index(name = "idx_place_category", columnList = "category_code"),
        @Index(name = "idx_place_kakao_id", columnList = "kakao_place_id", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place extends BaseEntity {

    @Column(name = "kakao_place_id", nullable = false, unique = true, length = 50)
    private String kakaoPlaceId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(name = "road_address", length = 500)
    private String roadAddress;

    @Column(length = 20)
    private String phone;

    @Column(name = "category_code", length = 10)
    private String categoryCode; // FD6, CE7, CS2 등

    @Column(name = "category_name", length = 50)
    private String categoryName; // 음식점, 카페 등

    // PostGIS Point 타입(SRID 4326 = WGS84)
    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point location;

    @Column(name = "place_url", length = 500)
    private String placeUrl;

    @Column(name = "last_synced_at")
    private java.time.LocalDateTime lastSyncedAt;

    /**
     * 위도/경도로 Point 생성 헬퍼 메서드
     * 주의: PostGIS는 (경도, 위도) = (x, y) 순서
     *
     * @param lat 위도
     * @param lng 경도
     *
     */
    public static Point createPoint(double lat, double lng){
        org.locationtech.jts.geom.GeometryFactory geometryFactory =
                new org.locationtech.jts.geom.GeometryFactory(
                        new org.locationtech.jts.geom.PrecisionModel(),
                        4326 // SRID: WGS84
                );
        return geometryFactory.createPoint(
                new org.locationtech.jts.geom.Coordinate(lng, lat) // x=경도, y=위도
        );
    }
}
