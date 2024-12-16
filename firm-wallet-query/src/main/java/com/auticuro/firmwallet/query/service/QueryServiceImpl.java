package com.auticuro.firmwallet.query.service;

import com.auticuro.firmwallet.common.model.Balance;
import com.auticuro.firmwallet.common.model.Transaction;
import com.auticuro.firmwallet.common.service.QueryService;
import com.auticuro.firmwallet.query.repository.AccountQueryRepository;
import com.auticuro.firmwallet.query.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryServiceImpl implements QueryService {

    @Autowired
    private AccountQueryRepository accountQueryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public Balance queryBalance(String accountId) {
        return accountQueryRepository.findBalanceByAccountId(accountId);
    }

    @Override
    public List<Transaction> queryTransactions(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Override
    public Transaction queryTransaction(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElse(null);
    }
}
