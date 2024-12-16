package com.auticuro.firmwallet.common.service;

import com.auticuro.firmwallet.common.model.Balance;
import com.auticuro.firmwallet.common.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface QueryService {
    /**
     * Query balance for an account
     * @param accountId Account ID
     * @return Current balance
     */
    Balance queryBalance(String accountId);

    /**
     * Query transactions for an account
     * @param accountId Account ID
     * @return List of transactions
     */
    List<Transaction> queryTransactions(String accountId);

    /**
     * Query single transaction by ID
     * @param transactionId Transaction ID
     * @return Transaction details
     */
    Transaction queryTransaction(String transactionId);
}
