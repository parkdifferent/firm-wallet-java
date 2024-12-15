package com.auticuro.firmwallet.common.model;

import lombok.Data;

@Data
public class WalletEvent {
    private String accountId;
    private String eventType;
    private String eventData;
    private String timestamp;
}
