package com.auticuro.firmwallet.access.gateway;

import firm_wallet.balance_operation_servicepb.BalanceOperationServiceGrpc;
import firm_wallet.balance_operation_servicepb.BalanceOperationServicepb;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@GrpcService
public class BalanceOperationGateway extends BalanceOperationServiceGrpc.BalanceOperationServiceImplBase {
    
    private final BalanceOperationServiceGrpc.BalanceOperationServiceBlockingStub blockingStub;
    
    @Autowired
    public BalanceOperationGateway(ManagedChannel firmWalletChannel) {
        this.blockingStub = BalanceOperationServiceGrpc.newBlockingStub(firmWalletChannel);
    }
    
    @Override
    public void transfer(BalanceOperationServicepb.TransferRequest request,
                         io.grpc.stub.StreamObserver<BalanceOperationServicepb.TransferResponse> responseObserver) {
        try {
            BalanceOperationServicepb.TransferResponse response = blockingStub.transfer(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to transfer", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void batchBalanceOperation(BalanceOperationServicepb.BatchBalanceOperationRequest request,
                                      io.grpc.stub.StreamObserver<BalanceOperationServicepb.BatchBalanceOperationResponse> responseObserver) {
        try {
            BalanceOperationServicepb.BatchBalanceOperationResponse response = blockingStub.batchBalanceOperation(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to execute batch balance operation", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void reserve(BalanceOperationServicepb.ReserveRequest request,
                        io.grpc.stub.StreamObserver<BalanceOperationServicepb.ReserveResponse> responseObserver) {
        try {
            BalanceOperationServicepb.ReserveResponse response = blockingStub.reserve(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to reserve", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void release(BalanceOperationServicepb.ReleaseRequest request,
                        io.grpc.stub.StreamObserver<BalanceOperationServicepb.ReleaseResponse> responseObserver) {
        try {
            BalanceOperationServicepb.ReleaseResponse response = blockingStub.release(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to release", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void queryBalance(BalanceOperationServicepb.QueryBalanceRequest request,
                             io.grpc.stub.StreamObserver<BalanceOperationServicepb.QueryBalanceResponse> responseObserver) {
        try {
            BalanceOperationServicepb.QueryBalanceResponse response = blockingStub.queryBalance(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Failed to query balance", e);
            responseObserver.onError(e);
        }
    }
}
