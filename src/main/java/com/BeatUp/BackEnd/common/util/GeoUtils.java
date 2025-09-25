package com.BeatUp.BackEnd.common.util;



public class GeoUtils {

    // Harvesine 공식으로 거리 계산(km)
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2){
        final int R = 6371; // 지구 반지름(km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // 예상 소요시간 계산(분)
    public static int calculateEstimateTime(double distance){
        // 평균 속도 30km/h로 계산(도심지 기준)
        return (int) Math.round(distance / 30.0 * 60);
    }
}
