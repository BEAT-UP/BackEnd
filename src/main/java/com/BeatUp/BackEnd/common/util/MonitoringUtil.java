package com.BeatUp.BackEnd.common.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class MonitoringUtil {

    private final MeterRegistry meterRegistry;

    /**
     * API 호출 시간 측정
     */
    public Timer.Sample startApiCallTimer(String apiName){
        return Timer.start(meterRegistry);
    }

    /**
     * API 호출 시간 기록
     */
    public void recordApiCall(Timer.Sample sample, String apiName, String status){
        sample.stop(Timer.builder("api.call.duration")
            .tag("api", apiName)
            .tag("status", status)
            .register(meterRegistry));
    }

    /**
     * API 호출 성공/실패 카운터
     */
    public void recordApiCall(String apiName, String status){
        Counter.builder("api.call.count")
                .tag("api", apiName)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Rate Limit 카운터
     */
    public void recordRateLimit(String apiName){
        Counter.builder("api.rate.limit")
                .tag("api", apiName)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Gauge 등록 (활성 연결 수 등)
     */
    public void registerGauge(String name, AtomicInteger value, String... tags){
        Gauge.builder(name, value, AtomicInteger::get)
                .tags(tags)
                .register(meterRegistry);
    }
}
