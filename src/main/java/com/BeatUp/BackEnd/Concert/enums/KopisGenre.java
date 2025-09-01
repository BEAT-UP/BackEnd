package com.BeatUp.BackEnd.Concert.enums;


import lombok.Getter;
import org.springframework.security.core.parameters.P;

@Getter
public enum KopisGenre {
    DRAMA("AAAA", "연극"),
    KOREAN_DANCE("BBBC", "무용(서양/한국무용)"),
    POPULAR_DANCE("BBBE", "대중무용"),
    CLASSICAL("CCCA", "서양음악(클래식)"),
    KOREAN_MUSIC("CCCC", "한국음악(국악)"),
    POPULAR_MUSIC("CCCD", "대중음악"),
    COMPLEX("EEEA", "복합"),
    CIRCUS_MAGIC("EEEB", "서커스/마술"),
    MUSICAL("GGGA", "뮤지컬");

    private final String code;
    private final String name;

    KopisGenre(String code, String name){
        this.code = code;
        this.name = name;
    }

    public static KopisGenre fromCode(String code){
        for(KopisGenre genre : values()){
            if(genre.code.equals(code)){
                return genre;
            }
        }
        return null;
    }
}
