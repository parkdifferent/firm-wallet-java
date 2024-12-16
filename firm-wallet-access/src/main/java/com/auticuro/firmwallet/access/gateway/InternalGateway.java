package com.auticuro.firmwallet.access.gateway;

import com.auticuro.firmwallet.common.model.Account;
import com.auticuro.firmwallet.common.model.Balance;
import com.auticuro.firmwallet.common.model.BalanceOperation;
import com.auticuro.firmwallet.common.service.QueryService;
import firm_wallet.account_management_servicepb.AccountManagementServicepb;
import firm_wallet.accountpb.Accountpb;
import firm_wallet.balance_operation_servicepb.BalanceOperationServicepb;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InternalGateway {
    
    @Autowired
    private QueryService queryService;
    
    @Autowired
    private AccountManagementGateway accountManagementGateway;
    
    @Autowired
    private BalanceOperationGateway balanceOperationGateway;

    public void updateBalances(List<BalanceOperation> operations) {
        // Convert balance operations to protobuf specs
        List<BalanceOperationServicepb.BatchBalanceOperationRequest.BalanceOperationSpec> specs = operations.stream()
            .map(op -> BalanceOperationServicepb.BatchBalanceOperationRequest.BalanceOperationSpec.newBuilder()
                .setAccountId(op.getAccountId())
                .setAmount(op.getAmount().toString())
                .build())
            .collect(Collectors.toList());
            
        // Create batch request
        BalanceOperationServicepb.BatchBalanceOperationRequest request = BalanceOperationServicepb.BatchBalanceOperationRequest.newBuilder()
            .setDedupId(UUID.randomUUID().toString())
            .addAllBalanceOperationSpec(specs)
            .build();
            
        // Call balance operation gateway
        balanceOperationGateway.batchBalanceOperation(request, new StreamObserver<BalanceOperationServicepb.BatchBalanceOperationResponse>() {
            @Override
            public void onNext(BalanceOperationServicepb.BatchBalanceOperationResponse value) {
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

    public void updateAccounts(List<Account> accounts) {
        // Update each account through account management gateway
        for (Account account : accounts) {
            Accountpb.Account pbAccount = Accountpb.Account.newBuilder()
                .setAccountId(account.getAccountId())
                // Set other fields
                .build();
                
            AccountManagementServicepb.UpdateAccountConfigRequest request = AccountManagementServicepb.UpdateAccountConfigRequest.newBuilder()
                .setHeader(AccountManagementServicepb.RequestHeader.newBuilder()
                    .setAccountId(account.getAccountId())
                    .setDedupId(UUID.randomUUID().toString())
                    .build())
                .build();
                
            accountManagementGateway.updateAccountConfig(request, new StreamObserver<AccountManagementServicepb.UpdateAccountConfigResponse>() {
                @Override
                public void onNext(AccountManagementServicepb.UpdateAccountConfigResponse value) {
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
    }

    public Balance getBalance(String accountId) {
        // Query account balance through query service
        return queryService.queryBalance(accountId);
    }
}
