package com.BeatUp.BackEnd.Concert.enums;


import lombok.Getter;

@Getter
public enum KopisPerformanceState {
    UPCOMING("01", "공연예정"),
    ONGOING("02", "공연중"),
    COMPLETED("03", "공연완료");

    private final String code;
    private final String name;

    KopisPerformanceState(String code, String name){
        this.code = code;
        this.name = name;
    }

    public static KopisPerformanceState fromCode(String code){
        for(KopisPerformanceState state: values()){
            if(state.code.equals(code)){
                return state;
            }
        }
        return null;
    }
}
