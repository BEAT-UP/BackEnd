package com.BeatUp.BackEnd.Concert.service.sync;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SyncStatus {

    private boolean isRunning;
    private long totalConcerts;
    private long kopisConcerts;
    private long needsSyncCount;
    private double kopisDataRatio;

    public String getStatusMessage(){
        if(isRunning){
            return "동기화 진행 중";
        }else if(needsSyncCount > 0){
            return String.format("동기화 필요한 데이터 %d개 존재", needsSyncCount);
        }else{
            return "모든 데이터 최신 상태";
        }
    }
}
