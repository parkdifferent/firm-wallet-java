package com.auticuro.firmwallet.access.gateway;

import com.auticuro.firmwallet.common.model.Transaction;
import com.auticuro.firmwallet.common.service.QueryService;
import firm_wallet.balance_operation_servicepb.BalanceOperationServicepb;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TransactionGateway {

    @Autowired
    private QueryService queryService;
    
    @Autowired
    private BalanceOperationGateway balanceOperationGateway;

    public void createTransaction(Transaction transaction) {
        // Create a new transaction by executing balance operations
        BalanceOperationServicepb.TransferRequest.TransferSpec transferSpec = BalanceOperationServicepb.TransferRequest.TransferSpec.newBuilder()
            .setAmount(transaction.getAmount().toString())
            .setFromAccountId(transaction.getFromAccountId())
            .setToAccountId(transaction.getToAccountId())
            .setMetadata(transaction.getMetadata())
            .build();
            
        BalanceOperationServicepb.TransferRequest request = BalanceOperationServicepb.TransferRequest.newBuilder()
            .setDedupId(UUID.randomUUID().toString())
            .setTransferSpec(transferSpec)
            .build();
            
        balanceOperationGateway.transfer(request, new StreamObserver<BalanceOperationServicepb.TransferResponse>() {
            @Override
            public void onNext(BalanceOperationServicepb.TransferResponse value) {
                // Handle response
            }
            
            @Override
            public void onError(Throwable t) {
                // Handle error
            }
            
            @Override
            public void onCompleted() {
                // Handle completion
            }
        });
    }

    public List<Transaction> getTransactions(String accountId) {
        // Query transactions for the account through query service
        return queryService.queryTransactions(accountId);
    }

    public Transaction getTransaction(String transactionId) {
        // Query single transaction through query service
        return queryService.queryTransaction(transactionId);
    }
}
