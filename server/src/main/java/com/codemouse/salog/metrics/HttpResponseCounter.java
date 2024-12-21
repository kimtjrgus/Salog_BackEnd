package com.codemouse.salog.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HttpResponseCounter {

    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public HttpResponseCounter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordResponse(String status) {
        // 상태 코드에 따라 카운터를 생성하고 증가시킴
        counters.computeIfAbsent(status, key -> Counter.builder("http_response_count")
                        .description("Count of HTTP responses by status code")
                        .tag("status", key)
                        .register(meterRegistry))
                .increment();
    }
}
