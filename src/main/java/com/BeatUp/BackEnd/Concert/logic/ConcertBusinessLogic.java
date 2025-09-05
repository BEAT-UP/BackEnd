package com.BeatUp.BackEnd.Concert.logic;

import com.BeatUp.BackEnd.Concert.entity.Concert;
import com.BeatUp.BackEnd.Concert.enums.DataSource;

import java.time.LocalDateTime;

public class ConcertBusinessLogic {

    public static boolean isFromKopis(Concert concert){
        return DataSource.KOPIS.equals(concert.getDataSource()) && concert.getKopisId() != null;
    }

    public static boolean isOngoing(Concert concert){
        return "공연중".equals(concert.getState()) || "02".equals(concert.getState());
    }

    public static boolean isCompleted(Concert concert){
        return "공연완료".equals(concert.getState()) || "03".equals(concert.getState());
    }

    public boolean needsSync(Concert concert){
        if(concert.getLastSyncedAt() == null)
            return true;
        return concert.getLastSyncedAt().isBefore(LocalDateTime.now().minusHours(24));
    }
}
