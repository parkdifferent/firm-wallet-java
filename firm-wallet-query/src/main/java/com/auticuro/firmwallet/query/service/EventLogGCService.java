package com.auticuro.firmwallet.query.service;

import com.auticuro.firmwallet.config.WalletServiceConfig;
import com.auticuro.firmwallet.query.repository.EventLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class EventLogGCService {
    
    @Autowired
    private WalletServiceConfig config;
    
    @Autowired
    private EventLogRepository eventLogRepository;
    
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    @PostConstruct
    public void init() {
        if (!config.getEventLogGC().isEnabled()) {
            log.info("Event log GC is disabled");
            return;
        }
        log.info("Event log GC initialized with config: {}", config.getEventLogGC());
    }
    
    @Scheduled(fixedDelayString = "${wallet.service.event-log-gc.poll-interval-millis:1000}")
    public void scheduleGC() {
        if (!config.getEventLogGC().isEnabled() || !isRunning.compareAndSet(false, true)) {
            return;
        }
        
        try {
            performGC();
        } catch (Exception e) {
            log.error("Failed to perform GC", e);
        } finally {
            isRunning.set(false);
        }
    }
    
    @Transactional
    protected void performGC() {
        long totalCount = eventLogRepository.selectCount(null);
        long countLimit = config.getEventLogGC().getCountLimit();
        
        if (totalCount <= countLimit) {
            log.debug("No need to perform GC, total events: {}, limit: {}", totalCount, countLimit);
            return;
        }
        
        long eventsToDelete = totalCount - countLimit;
        long batchSize = config.getEventLogGC().getBatchSize();
        long batches = (eventsToDelete + batchSize - 1) / batchSize;
        
        log.info("Starting GC, total events: {}, to delete: {}, batches: {}", 
                totalCount, eventsToDelete, batches);
        
        for (long i = 0; i < batches; i++) {
            long deleted = eventLogRepository.deleteOldestEvents(batchSize);
            log.debug("Deleted {} events in batch {}", deleted, i + 1);
            
            if (deleted < batchSize) {
                break;
            }
        }
    }
}
