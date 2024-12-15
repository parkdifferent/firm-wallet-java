package com.auticuro.firmwallet.access.gateway;

import firm_wallet.account_management_servicepb.AccountManagementServiceGrpc;
import firm_wallet.account_management_servicepb.AccountManagementServicepb;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@GrpcService
public class AccountManagementGateway extends AccountManagementServiceGrpc.AccountManagementServiceImplBase {
    
    private final AccountManagementServiceGrpc.AccountManagementServiceBlockingStub blockingStub;
    
    @Autowired
    public AccountManagementGateway(ManagedChannel firmWalletChannel) {
        this.blockingStub = AccountManagementServiceGrpc.newBlockingStub(firmWalletChannel);
    }
    
    @Override
    public void createAccount(AccountManagementServicepb.CreateAccountRequest request,
                              io.grpc.stub.StreamObserver<AccountManagementServicepb.CreateAccountResponse> responseObserver) {
        try {
            AccountManagementServicepb.CreateAccountResponse response = blockingStub.createAccount(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to create account", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void deleteAccount(AccountManagementServicepb.DeleteAccountRequest request,
                              io.grpc.stub.StreamObserver<AccountManagementServicepb.DeleteAccountResponse> responseObserver) {
        try {
            AccountManagementServicepb.DeleteAccountResponse response = blockingStub.deleteAccount(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to delete account", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void lockAccount(AccountManagementServicepb.LockAccountRequest request,
                            io.grpc.stub.StreamObserver<AccountManagementServicepb.LockAccountResponse> responseObserver) {
        try {
            AccountManagementServicepb.LockAccountResponse response = blockingStub.lockAccount(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to lock account", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void unlockAccount(AccountManagementServicepb.UnlockAccountRequest request,
                              io.grpc.stub.StreamObserver<AccountManagementServicepb.UnlockAccountResponse> responseObserver) {
        try {
            AccountManagementServicepb.UnlockAccountResponse response = blockingStub.unlockAccount(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to unlock account", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void updateAccountConfig(AccountManagementServicepb.UpdateAccountConfigRequest request,
                                    io.grpc.stub.StreamObserver<AccountManagementServicepb.UpdateAccountConfigResponse> responseObserver) {
        try {
            AccountManagementServicepb.UpdateAccountConfigResponse response = blockingStub.updateAccountConfig(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to update account config", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void getAccount(AccountManagementServicepb.GetAccountRequest request,
                           io.grpc.stub.StreamObserver<AccountManagementServicepb.GetAccountResponse> responseObserver) {
        try {
            AccountManagementServicepb.GetAccountResponse response = blockingStub.getAccount(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to get account", e);
            responseObserver.onError(e);
        }
    }
}
