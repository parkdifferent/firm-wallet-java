package com.auticuro.firmwallet.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry registry;
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void stopTimer(Timer.Sample sample, String name) {
        Timer timer = timers.computeIfAbsent(name,
                key -> Timer.builder(name)
                        .publishPercentileHistogram()
                        .minimumExpectedValue(Duration.ofMillis(1))
                        .maximumExpectedValue(Duration.ofSeconds(30))
                        .register(registry));
        sample.stop(timer);
    }

    public void incrementCounter(String name) {
        Counter counter = counters.computeIfAbsent(name,
                key -> Counter.builder(name)
                        .register(registry));
        counter.increment();
    }

    public void recordValue(String name, double value) {
        registry.gauge(name, value);
    }

    public void incrementOperationCounter(String operation) {
        log.info("Operation counter incremented - type: {}", operation);
        incrementCounter("wallet.operation." + operation);
    }

    public void recordBalanceChange(String accountId, String currency, BigDecimal amount) {
        log.info("Balance change recorded - account: {}, currency: {}, amount: {}", accountId, currency, amount);
        recordValue("wallet.balance." + accountId + "." + currency, amount.doubleValue());
    }

    public MeterRegistry getRegistry() {
        return registry;
    }
}
