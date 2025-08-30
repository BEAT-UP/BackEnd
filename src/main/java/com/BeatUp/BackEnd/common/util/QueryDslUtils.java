package com.BeatUp.BackEnd.common.util;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;


@UtilityClass
public class QueryDslUtils {

    /**
     * 문자열 포함 검색(대소문자 무시)
     */
    public static BooleanExpression containsIgnoreCase(StringPath field, String value){
        return hasText(value) ? field.lower().contains(value.toLowerCase()) : null;
    }

    /**
     * 동등 비교 (null 안전)
     */
    public static <T> BooleanExpression eqlfNotNull(SimpleExpression<T> field, T value){
        return value != null ? field.eq(value): null;
    }

    /**
     * 날짜 범위 검색
     */
    public static BooleanExpression betweenDates(DateTimePath<LocalDateTime> field,
                                                 LocalDateTime start, LocalDateTime end){

        if(start != null && end != null){
            return field.between(start, end);
        }else if(start != null){
            return field.goe(start);
        }else if(end != null){
            return field.loe(end);
        }

        return null;
    }

    public static boolean hasText(String str){
        return str != null && !str.trim().isEmpty();
    }
}
