package com.auticuro.firmwallet.transaction.marker;

import com.auticuro.firmwallet.common.event.AccountEvent;
import com.auticuro.firmwallet.common.event.Event;
import com.auticuro.firmwallet.saga.SagaInstance;
import com.auticuro.firmwallet.transaction.saga.SagaCoordinator;
import com.auticuro.firmwallet.transaction.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommandHandler {
    private final WalletOperationProcessor walletOperationProcessor;
    private final SagaCoordinator sagaCoordinator;

    public void handleLockCommand(String accountId) {
        String sagaId = UUID.randomUUID().toString();
        SagaInstance saga = new SagaInstance();
        saga.setSagaId(sagaId);

        List<SagaInstance.SagaStep> steps = new ArrayList<>();

        SagaStep lockStep = new SagaStep(
            "LOCK_STEP",
            instance -> {
                walletOperationProcessor.lockAccount(accountId);
                AccountEvent.AccountLocked event = AccountEvent.AccountLocked.builder()
                    .accountId(accountId)
                    .timestamp(LocalDateTime.now())
                    .build();
            },
            instance -> walletOperationProcessor.unlockAccount(accountId)
        );

        steps.add(lockStep);
        saga.setSteps(steps);
        sagaCoordinator.executeSaga(saga);
    }

    public void handleUnlockCommand(String accountId) {
        String sagaId = UUID.randomUUID().toString();
        SagaInstance saga = new SagaInstance();
        saga.setSagaId(sagaId);

        List<SagaInstance.SagaStep> steps = new ArrayList<>();

        SagaStep unlockStep = new SagaStep(
            "UNLOCK_STEP",
            instance -> {
                walletOperationProcessor.unlockAccount(accountId);
                AccountEvent.AccountUnlocked event = AccountEvent.AccountUnlocked.builder()
                    .accountId(accountId)
                    .timestamp(LocalDateTime.now())
                    .build();
            },
            instance -> walletOperationProcessor.lockAccount(accountId)
        );

        steps.add(unlockStep);
        saga.setSteps(steps);
        sagaCoordinator.executeSaga(saga);
    }

    public void handleTransferCommand(String fromAccountId, String toAccountId, String currency, java.math.BigDecimal amount) {
        String sagaId = UUID.randomUUID().toString();
        SagaInstance saga = new SagaInstance();
        saga.setSagaId(sagaId);

        List<SagaInstance.SagaStep> steps = new ArrayList<>();

        SagaStep transferStep = new SagaStep(
            "TRANSFER_STEP",
            instance -> {
                walletOperationProcessor.transfer(fromAccountId, toAccountId, currency, amount);
                AccountEvent.BalanceChanged event = AccountEvent.BalanceChanged.builder()
                    .accountId(fromAccountId)
                    .currency(currency)
                    .amount(String.valueOf(amount))
                    .operation("TRANSFER")
                    .timestamp(LocalDateTime.now())
                    .build();
            },
            instance -> walletOperationProcessor.transfer(toAccountId, fromAccountId, currency, amount)
        );

        steps.add(transferStep);
        saga.setSteps(steps);
        sagaCoordinator.executeSaga(saga);
    }
}
