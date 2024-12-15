package com.auticuro.firmwallet.transaction.marker;

import com.auticuro.firmwallet.common.metrics.MetricsService;
import com.auticuro.firmwallet.common.model.Balance;
import com.auticuro.firmwallet.common.model.Account;
import com.auticuro.firmwallet.repository.AccountRepository;
import com.auticuro.firmwallet.repository.BalanceRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletOperationProcessor {
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final MetricsService metricsService;

    @Transactional
    public void createAccount(String accountId, Account.AssetClass assetClass, BigDecimal minAvailable, BigDecimal maxAvailable, BigDecimal maxReserved) {
        // Check if account already exists
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account_id", accountId);
        if (accountRepository.selectOne(queryWrapper) != null) {
            throw new IllegalArgumentException("Account already exists: " + accountId);
        }

        // Create new account
        Account account = Account.builder()
                .accountId(accountId)
                .state(Account.AccountState.ACTIVE)
                .assetClass(assetClass)
                .minAvailable(minAvailable)
                .maxAvailable(maxAvailable)
                .maxReserved(maxReserved)
                .updatedAt(LocalDateTime.now())
                .build();

        // Create initial balance
        Balance balance = Balance.builder()
                .accountId(accountId)
                .available(BigDecimal.ZERO)
                .reserved(BigDecimal.ZERO)
                .build();

        accountRepository.insert(account);
        balanceRepository.insert(balance);
        
        metricsService.incrementOperationCounter("account_create");
    }

    @Transactional
    public void deleteAccount(String accountId) {
        // Get account and verify it exists
        Account account = getAndValidateAccount(accountId);

        // Get balance and verify it's zero
        QueryWrapper<Balance> balanceQuery = new QueryWrapper<>();
        balanceQuery.eq("account_id", accountId);
        Balance balance = balanceRepository.selectOne(balanceQuery);
        
        if (balance != null && (balance.getAvailable().compareTo(BigDecimal.ZERO) != 0 ||
            balance.getReserved().compareTo(BigDecimal.ZERO) != 0)) {
            throw new IllegalStateException("Cannot delete account with non-zero balance");
        }

        // Update account state to DELETED
        account.setState(Account.AccountState.DELETED);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.updateById(account);

        metricsService.incrementOperationCounter("account_delete");
    }

    @Transactional
    public void lockAccount(String accountId) {
        // Get account and verify it exists
        Account account = getAndValidateAccount(accountId);

        // Verify account is not already locked
        if (account.getState() == Account.AccountState.LOCKED) {
            throw new IllegalStateException("Account is already locked: " + accountId);
        }

        // Update account state to LOCKED
        account.setState(Account.AccountState.LOCKED);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.updateById(account);

        metricsService.incrementOperationCounter("account_lock");
    }

    @Transactional
    public void unlockAccount(String accountId) {
        // Get account and verify it exists
        Account account = getAndValidateAccount(accountId);

        // Verify account is locked
        if (account.getState() != Account.AccountState.LOCKED) {
            throw new IllegalStateException("Account is not locked: " + accountId);
        }

        // Update account state to ACTIVE
        account.setState(Account.AccountState.ACTIVE);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.updateById(account);

        metricsService.incrementOperationCounter("account_unlock");
    }

    @Transactional
    public void transfer(String fromAccountId, String toAccountId, String currency, BigDecimal amount) {
        // Get and validate both accounts
        Account fromAccount = getAndValidateAccount(fromAccountId);
        Account toAccount = getAndValidateAccount(toAccountId);

        // Verify accounts are active
        verifyAccountActive(fromAccount);
        verifyAccountActive(toAccount);

        // Get balances
        QueryWrapper<Balance> fromQuery = new QueryWrapper<>();
        fromQuery.eq("account_id", fromAccountId);
        Balance from = balanceRepository.selectOne(fromQuery);

        QueryWrapper<Balance> toQuery = new QueryWrapper<>();
        toQuery.eq("account_id", toAccountId);
        Balance to = balanceRepository.selectOne(toQuery);

        if (from == null || to == null) {
            throw new IllegalStateException("Balance not found for one or both accounts");
        }

        if (from.getAvailable().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Verify balance limits
        if (to.getAvailable().add(amount).compareTo(toAccount.getMaxAvailable()) > 0) {
            throw new IllegalArgumentException("Transfer would exceed maximum available balance");
        }

        from.setAvailable(from.getAvailable().subtract(amount));
        to.setAvailable(to.getAvailable().add(amount));

        from.setUpdatedAt(LocalDateTime.now());
        to.setUpdatedAt(LocalDateTime.now());

        balanceRepository.updateById(from);
        balanceRepository.updateById(to);

        metricsService.recordBalanceChange(fromAccountId, currency, amount.negate());
        metricsService.recordBalanceChange(toAccountId, currency, amount);
    }

    @Transactional
    public void reserve(String accountId, String currency, BigDecimal amount) {
        // Get and validate account
        Account account = getAndValidateAccount(accountId);
        verifyAccountActive(account);

        // Get balance
        QueryWrapper<Balance> balanceQuery = new QueryWrapper<>();
        balanceQuery.eq("account_id", accountId);
        Balance balance = balanceRepository.selectOne(balanceQuery);

        if (balance == null) {
            throw new IllegalStateException("Balance not found for account");
        }

        if (balance.getAvailable().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Verify reserve limit
        if (balance.getReserved().add(amount).compareTo(account.getMaxReserved()) > 0) {
            throw new IllegalArgumentException("Reserve would exceed maximum reserved amount");
        }

        balance.setAvailable(balance.getAvailable().subtract(amount));
        balance.setReserved(balance.getReserved().add(amount));
        balance.setUpdatedAt(LocalDateTime.now());

        balanceRepository.updateById(balance);
        metricsService.recordBalanceChange(accountId, currency, amount);
    }

    @Transactional
    public void unreserve(String accountId, String currency, BigDecimal amount) {
        // Get and validate account
        Account account = getAndValidateAccount(accountId);
        verifyAccountActive(account);

        // Get balance
        QueryWrapper<Balance> balanceQuery = new QueryWrapper<>();
        balanceQuery.eq("account_id", accountId);
        Balance balance = balanceRepository.selectOne(balanceQuery);

        if (balance == null) {
            throw new IllegalStateException("Balance not found for account");
        }

        if (balance.getReserved().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient reserved amount");
        }

        balance.setReserved(balance.getReserved().subtract(amount));
        balance.setAvailable(balance.getAvailable().add(amount));
        balance.setUpdatedAt(LocalDateTime.now());

        balanceRepository.updateById(balance);
        metricsService.recordBalanceChange(accountId, currency, amount.negate());
    }

    private Account getAndValidateAccount(String accountId) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account_id", accountId);
        Account account = accountRepository.selectOne(queryWrapper);
        
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        
        return account;
    }

    private void verifyAccountActive(Account account) {
        if (account.getState() != Account.AccountState.ACTIVE) {
            throw new IllegalStateException("Account is not active: " + account.getAccountId());
        }
    }
}
