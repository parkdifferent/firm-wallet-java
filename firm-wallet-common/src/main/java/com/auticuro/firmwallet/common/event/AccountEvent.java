package com.auticuro.firmwallet.common.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

public class AccountEvent {
    @Value
    @Builder
    public static class AccountCreated extends Event {
        String accountId;
        String assetClass;
        LocalDateTime timestamp;
    }

    @Value
    @Builder
    public static class AccountLocked extends Event {
        String accountId;
        LocalDateTime timestamp;
    }

    @Value
    @Builder
    public static class AccountUnlocked extends Event {
        String accountId;
        LocalDateTime timestamp;
    }

    @Value
    @Builder
    public static class BalanceChanged extends Event {
        String accountId;
        String currency;
        String amount;
        String operation;
        LocalDateTime timestamp;
    }
}
