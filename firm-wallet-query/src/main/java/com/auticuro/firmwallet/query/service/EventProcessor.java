package com.auticuro.firmwallet.query.service;

import com.auticuro.firmwallet.common.event.Event;
import com.auticuro.firmwallet.common.event.AccountEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventProcessor {
    private final AccountHierarchyService accountHierarchyService;
    
    @Transactional
    public void processEvent(Event event) {
        if (event instanceof AccountEvent.AccountCreated) {
            handleAccountCreated((AccountEvent.AccountCreated) event);
        } else if (event instanceof AccountEvent.BalanceChanged) {
            handleBalanceChanged((AccountEvent.BalanceChanged) event);
        }
        // Handle other event types...
    }
    
    private void handleAccountCreated(AccountEvent.AccountCreated event) {
        // Update read model for account creation
    }
    
    private void handleBalanceChanged(AccountEvent.BalanceChanged event) {
        // Update read model for balance changes
        // Trigger recalculation of account hierarchy if needed
        accountHierarchyService.recalculateBalance(event.getAccountId());
    }
}
