package com.auticuro.firmwallet.transaction.marker;

import com.auticuro.firmwallet.config.WalletServiceConfig;
import com.auticuro.firmwallet.common.metrics.MetricsService;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@Service
public class MessageBroker {
    private final WalletServiceConfig config;
    private final MetricsService metricsService;
    private final Map<String, Set<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService;
    private volatile boolean running = true;

    public MessageBroker(WalletServiceConfig config, MetricsService metricsService) {
        this.config = config;
        this.metricsService = metricsService;
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("message-broker-" + threadNumber.getAndIncrement());
                        return thread;
                    }
                });
    }

    @PostConstruct
    public void start() {
        executorService.submit(this::processMessages);
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void publish(String topic, Object message) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("Publishing message to topic: {}, message: {}", topic, message);
            messageQueue.put(new Message(topic, message));
            metricsService.incrementOperationCounter("message.publish.success");
        } catch (InterruptedException e) {
            log.error("Failed to publish message", e);
            metricsService.incrementOperationCounter("message.publish.failure");
            Thread.currentThread().interrupt();
        } finally {
            Timer.Sample sample = metricsService.startTimer();
            sample.stop(Timer.builder("message.publish.latency")
                    .publishPercentileHistogram()
                    .register(metricsService.getRegistry()));
        }
    }

    public void subscribe(String topic, Consumer<Object> subscriber) {
        subscribers.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(subscriber);
        metricsService.recordValue("subscribers." + topic, subscribers.get(topic).size());
    }

    public void unsubscribe(String topic, Consumer<Object> subscriber) {
        subscribers.computeIfPresent(topic, (k, v) -> {
            v.remove(subscriber);
            metricsService.recordValue("subscribers." + topic, v.size());
            return v.isEmpty() ? null : v;
        });
    }

    private void processMessages() {
        while (running) {
            try {
                Message message = messageQueue.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    Timer.Sample sample = metricsService.startTimer();
                    Set<Consumer<Object>> topicSubscribers = subscribers.get(message.getTopic());
                    if (topicSubscribers != null) {
                        for (Consumer<Object> subscriber : topicSubscribers) {
                            try {
                                subscriber.accept(message.getPayload());
                                metricsService.incrementOperationCounter("message.delivery.success");
                            } catch (Exception e) {
                                log.error("Failed to deliver message to subscriber", e);
                                metricsService.incrementOperationCounter("message.delivery.failure");
                            }
                        }
                    }
                    sample.stop(Timer.builder("message.delivery.latency")
                            .publishPercentileHistogram()
                            .register(metricsService.getRegistry()));
                }
            } catch (InterruptedException e) {
                log.error("Message processing interrupted", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static class Message {
        private final String topic;
        private final Object payload;

        public Message(String topic, Object payload) {
            this.topic = topic;
            this.payload = payload;
        }

        public String getTopic() {
            return topic;
        }

        public Object getPayload() {
            return payload;
        }
    }
}
