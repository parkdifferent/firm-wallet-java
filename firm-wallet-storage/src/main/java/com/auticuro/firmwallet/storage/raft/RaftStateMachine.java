package com.auticuro.firmwallet.storage.raft;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.auticuro.firmwallet.common.model.EventType;
import com.auticuro.firmwallet.common.model.WalletEvent;
import com.auticuro.firmwallet.storage.auticuro.RocksDBStore;
import com.auticuro.firmwallet.transaction.marker.WalletOperationProcessor;
import firm_wallet.command_servicepb.CommandServicepb;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import com.alipay.sofa.jraft.StateMachine;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.LeaderChangeContext;
import com.alipay.sofa.jraft.error.RaftException;
import com.auticuro.firmwallet.common.metrics.MetricsService;
import com.auticuro.firmwallet.common.model.EventLog;
import com.auticuro.firmwallet.common.model.Account;
import com.google.protobuf.InvalidProtocolBufferException;
import firm_wallet.account_management_servicepb.AccountManagementServicepb;
import firm_wallet.accountpb.Accountpb;
import firm_wallet.balance_operation_servicepb.BalanceOperationServicepb;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class RaftStateMachine implements StateMachine {
    private final WalletOperationProcessor walletOperationProcessor;
    private final MetricsService metricsService;
    private final RocksDBStore rocksDBStore;
    private final AtomicLong lastAppliedIndex = new AtomicLong(0);

    private void handleCommand(CommandServicepb.Command command) {
        try {
            switch (command.getType()) {
                case CREATE_ACCOUNT:
                    AccountManagementServicepb.CreateAccountRequest createRequest = 
                        AccountManagementServicepb.CreateAccountRequest.parseFrom(command.getData());

                    Accountpb.AssetClass assetClass = createRequest.getAccountConfig().getAssetClass();
                    Accountpb.BalanceLimit balanceLimit = createRequest.getAccountConfig().getBalanceLimit();

                    walletOperationProcessor.createAccount(
                        createRequest.getHeader().getAccountId(),
                        Account.AssetClass.valueOf(assetClass.name()),
                        new BigDecimal(balanceLimit.getMinAvailable()),
                        new BigDecimal(balanceLimit.getMaxAvailable()),
                        new BigDecimal(balanceLimit.getMaxReserved()));
                    break;
                case DELETE_ACCOUNT:
                    AccountManagementServicepb.DeleteAccountRequest deleteRequest = 
                        AccountManagementServicepb.DeleteAccountRequest.parseFrom(command.getData());
                    walletOperationProcessor.deleteAccount(deleteRequest.getHeader().getAccountId());
                    break;
                case LOCK_ACCOUNT:
                    AccountManagementServicepb.LockAccountRequest lockRequest = 
                        AccountManagementServicepb.LockAccountRequest.parseFrom(command.getData());
                    walletOperationProcessor.lockAccount(lockRequest.getHeader().getAccountId());
                    break;
                case UNLOCK_ACCOUNT:
                    AccountManagementServicepb.UnlockAccountRequest unlockRequest = 
                        AccountManagementServicepb.UnlockAccountRequest.parseFrom(command.getData());
                    walletOperationProcessor.unlockAccount(unlockRequest.getHeader().getAccountId());
                    break;
                case TRANSFER:
                    BalanceOperationServicepb.TransferRequest transferRequest = 
                        BalanceOperationServicepb.TransferRequest.parseFrom(command.getData());
                    walletOperationProcessor.transfer(
                        transferRequest.getTransferSpec().getFromAccountId(),
                        transferRequest.getTransferSpec().getToAccountId(),
                        "USD", // Default currency
                        new BigDecimal(transferRequest.getTransferSpec().getAmount()));
                    break;
                case RESERVE:
                    BalanceOperationServicepb.ReserveRequest reserveRequest = 
                        BalanceOperationServicepb.ReserveRequest.parseFrom(command.getData());
                    walletOperationProcessor.reserve(
                        reserveRequest.getAccountId(),
                        "USD", // Default currency
                        new BigDecimal(reserveRequest.getAmount()));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown command type: " + command.getType());
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RaftError("Failed to parse command data", e);
        }
    }

    private String getEventId(CommandServicepb.Command command) {
        try {
            switch (command.getType()) {
                case CREATE_ACCOUNT:
                    return AccountManagementServicepb.CreateAccountRequest.parseFrom(command.getData())
                        .getHeader().getAccountId();
                case DELETE_ACCOUNT:
                    return AccountManagementServicepb.DeleteAccountRequest.parseFrom(command.getData())
                        .getHeader().getAccountId();
                case LOCK_ACCOUNT:
                    return AccountManagementServicepb.LockAccountRequest.parseFrom(command.getData())
                        .getHeader().getAccountId();
                case UNLOCK_ACCOUNT:
                    return AccountManagementServicepb.UnlockAccountRequest.parseFrom(command.getData())
                        .getHeader().getAccountId();
                case TRANSFER:
                    BalanceOperationServicepb.TransferRequest.TransferSpec transferSpec = 
                        BalanceOperationServicepb.TransferRequest.parseFrom(command.getData())
                            .getTransferSpec();
                    return transferSpec.getFromAccountId() + "_" + transferSpec.getToAccountId();
                case RESERVE:
                    BalanceOperationServicepb.ReserveRequest reserveRequest = 
                        BalanceOperationServicepb.ReserveRequest.parseFrom(command.getData());
                    return reserveRequest.getAccountId();
                default:
                    throw new IllegalArgumentException("Unknown command type: " + command.getType());
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RaftError("Failed to parse command data", e);
        }
    }

    private EventType getEventType(CommandServicepb.Command command) {
        switch (command.getType()) {
            case CREATE_ACCOUNT:
                return EventType.ACCOUNT_CREATED;
            case DELETE_ACCOUNT:
                return EventType.ACCOUNT_DELETED;
            case LOCK_ACCOUNT:
                return EventType.ACCOUNT_LOCKED;
            case UNLOCK_ACCOUNT:
                return EventType.ACCOUNT_UNLOCKED;
            case TRANSFER:
                return EventType.TRANSFER;
            case RESERVE:
                return EventType.RESERVE;
            default:
                throw new IllegalArgumentException("Unknown command type: " + command.getType());
        }
    }

    @Override
    public void onApply(Iterator iter) {
        try {
            while (iter.hasNext()) {
                long startTime = System.currentTimeMillis();
                Closure done = iter.done();

                ByteBuffer data = iter.getData();
                CommandServicepb.Command command;
                try {
                    command = CommandServicepb.Command.parseFrom(data.array());
                } catch (InvalidProtocolBufferException e) {
                    log.error("Failed to parse command", e);
                    if (done != null) {
                        done.run(new Status(RaftError.EINVAL, "Invalid command format"));
                    }
                    return;
                }

                try {
                    handleCommand(command);

                    // Record event log
                    EventLog eventLog = new EventLog();
                    eventLog.setEventType(getEventType(command).name());
                    eventLog.setEventData(command.toByteArray());
                    eventLog.setTimestamp(LocalDateTime.now());
                    eventLog.setEventId(getEventId(command));
                    rocksDBStore.put(eventLog.getEventId(),
                            eventLog.getEventData());

                    // Record metrics
                    long latency = System.currentTimeMillis() - startTime;
                    metricsService.incrementOperationCounter(getEventType(command).name());
                    Timer.Sample sample = metricsService.startTimer();
                    metricsService.stopTimer(sample, "wallet.operation." + getEventType(command).name());

                    lastAppliedIndex.incrementAndGet();

                    if (done != null) {
                        done.run(Status.OK());
                    }
                } catch (Exception e) {
                    log.error("Failed to handle command", e);
                    if (done != null) {
                        done.run(new Status(RaftError.UNKNOWN, e.getMessage()));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error applying log entries", e);
            iter.setErrorAndRollback(iter.getIndex(), new Status(RaftError.UNKNOWN, "Apply error"));
        }
    }

    @Override
    public void onShutdown() {
        log.info("RaftStateMachine shutting down");
        rocksDBStore.close();
    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        // Implementation for snapshot save
        done.run(Status.OK());
    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        return true;
    }

    @Override
    public void onLeaderStart(long term) {
        log.info("Node becomes leader, term: {}", term);
    }

    @Override
    public void onLeaderStop(Status status) {
        log.info("Node steps down from leader, status: {}", status);
    }

    @Override
    public void onError(RaftException e) {
        log.error("RaftStateMachine error", e);
    }

    @Override
    public void onConfigurationCommitted(Configuration conf) {
        log.info("Configuration committed: {}", conf);
    }

    @Override
    public void onStopFollowing(LeaderChangeContext ctx) {
        log.info("Node stops following {}", ctx);
    }

    @Override
    public void onStartFollowing(LeaderChangeContext ctx) {
        log.info("Node starts following {}", ctx);
    }
}
