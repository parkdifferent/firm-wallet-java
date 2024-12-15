package com.auticuro.firmwallet.common.event;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Event {
    private String eventId;
    private String eventType;
    private String aggregateId;
    private LocalDateTime timestamp;
    private long sequence;
    private byte[] data;
    
    public Event() {
        this.timestamp = LocalDateTime.now();
    }
}
