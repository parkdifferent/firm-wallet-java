package com.auticuro.firmwallet.transaction.marker;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Message {
    private String topic;
    private String type;
    private byte[] payload;
    private long timestamp;
    private String source;
    
    public Message() {
        this.timestamp = System.currentTimeMillis();
    }
}
