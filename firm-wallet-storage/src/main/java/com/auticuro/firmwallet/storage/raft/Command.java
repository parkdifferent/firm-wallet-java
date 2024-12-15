package com.auticuro.firmwallet.storage.raft;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import firm_wallet.account_management_servicepb.AccountManagementServicepb;
import firm_wallet.balance_operation_servicepb.BalanceOperationServicepb;
import firm_wallet.command_servicepb.CommandServicepb;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Command {
    private CommandType type;
    private ByteString data;
    
    public enum CommandType {
        CREATE_ACCOUNT,
        DELETE_ACCOUNT,
        LOCK_ACCOUNT,
        UNLOCK_ACCOUNT,
        UPDATE_ACCOUNT_CONFIG,
        TRANSFER,
        BATCH_BALANCE_OPERATION,
        RESERVE,
        RELEASE;

        public static CommandType fromProto(CommandServicepb.CommandType type) {
            return CommandType.valueOf(type.name());
        }

        public CommandServicepb.CommandType toProto() {
            return CommandServicepb.CommandType.valueOf(this.name());
        }
    }
    
    public AccountManagementServicepb.CreateAccountRequest getCreateAccountRequest() throws InvalidProtocolBufferException {
        return AccountManagementServicepb.CreateAccountRequest.parseFrom(data);
    }
    
    public AccountManagementServicepb.DeleteAccountRequest getDeleteAccountRequest() throws InvalidProtocolBufferException {
        return AccountManagementServicepb.DeleteAccountRequest.parseFrom(data);
    }
    
    public AccountManagementServicepb.LockAccountRequest getLockAccountRequest() throws InvalidProtocolBufferException {
        return AccountManagementServicepb.LockAccountRequest.parseFrom(data);
    }
    
    public AccountManagementServicepb.UnlockAccountRequest getUnlockAccountRequest() throws InvalidProtocolBufferException {
        return AccountManagementServicepb.UnlockAccountRequest.parseFrom(data);
    }
    
    public AccountManagementServicepb.UpdateAccountConfigRequest getUpdateAccountConfigRequest() throws InvalidProtocolBufferException {
        return AccountManagementServicepb.UpdateAccountConfigRequest.parseFrom(data);
    }
    
    public BalanceOperationServicepb.TransferRequest getTransferRequest() throws InvalidProtocolBufferException {
        return BalanceOperationServicepb.TransferRequest.parseFrom(data);
    }
    
    public BalanceOperationServicepb.BatchBalanceOperationRequest getBatchBalanceOperationRequest() throws InvalidProtocolBufferException {
        return BalanceOperationServicepb.BatchBalanceOperationRequest.parseFrom(data);
    }
    
    public BalanceOperationServicepb.ReserveRequest getReserveRequest() throws InvalidProtocolBufferException {
        return BalanceOperationServicepb.ReserveRequest.parseFrom(data);
    }
    
    public BalanceOperationServicepb.ReleaseRequest getReleaseRequest() throws InvalidProtocolBufferException {
        return BalanceOperationServicepb.ReleaseRequest.parseFrom(data);
    }
    
    public static Command parseFrom(byte[] bytes) throws InvalidProtocolBufferException {
        CommandServicepb.Command proto = CommandServicepb.Command.parseFrom(bytes);
        return Command.builder()
                .type(CommandType.fromProto(proto.getType()))
                .data(proto.getData())
                .build();
    }
    
    public byte[] toByteArray() {
        CommandServicepb.Command proto = CommandServicepb.Command.newBuilder()
                .setType(type.toProto())
                .setData(data)
                .build();
        return proto.toByteArray();
    }
}
