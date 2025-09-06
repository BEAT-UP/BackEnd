package com.BeatUp.BackEnd.Concert.service.sync;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataWindowSplitter {

    private static final int MAX_DAYS_PER_WINDOW = 30; // KOPIS 31일 제한(시작일 포함)

    /**
     * 날짜 범위를 31일 이내로 윈도우 분할
     */
    List<DateWindow> splitIntoWindows(LocalDate start, LocalDate end){
        List<DateWindow> windows = new ArrayList<>();
        LocalDate current = start;

        while(!current.isAfter(end)){
            LocalDate windowEnd = current.plusDays(MAX_DAYS_PER_WINDOW);
            if(windowEnd.isAfter(end)){
                windowEnd = end;
            }

            windows.add(new DateWindow(current, windowEnd));
            current = windowEnd.plusDays(1);
        }

        return windows;
    }

    // 날짜 윈도우 헬퍼 클래스
    static class DateWindow{
        final LocalDate start;
        final LocalDate end;

        DateWindow(LocalDate start, LocalDate end){
            this.start = start;
            this.end = end;
        }
    }
}
