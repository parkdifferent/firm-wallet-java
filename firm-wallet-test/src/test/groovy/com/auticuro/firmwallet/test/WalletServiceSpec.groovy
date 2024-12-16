package com.auticuro.firmwallet.test

import com.auticuro.firmwallet.common.event.Event
import com.auticuro.firmwallet.common.model.Account
import com.auticuro.firmwallet.common.model.Balance
import com.auticuro.firmwallet.common.model.BalanceOperation
import com.auticuro.firmwallet.access.gateway.AccountManagementGateway
import com.auticuro.firmwallet.access.gateway.BalanceOperationGateway
import com.auticuro.firmwallet.access.gateway.InternalGateway
import com.auticuro.firmwallet.test.config.TestConfiguration
import com.auticuro.firmwallet.storage.auticuro.EventStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Stepwise
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(classes = TestConfiguration.class)
@Stepwise
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WalletServiceSpec extends Specification {

    @Autowired
    private AccountManagementGateway accountManagementGateway

    @Autowired
    private BalanceOperationGateway balanceOperationGateway

    @Autowired
    private InternalGateway internalGateway

    @Autowired
    private EventStore eventStore

    def setup() {
        // No need to clear mocks, Spring will handle it with @DirtiesContext
    }

    def cleanup() {
        // Clean up after each test if needed
    }

    def "should create account successfully"() {
        given: "a new account request"
        def accountId = "test_account_" + System.currentTimeMillis()
        def dedupId = UUID.randomUUID().toString()
        eventStore.append(_ as Event) >> null

        when: "creating the account"
        def response = accountManagementGateway.createAccount(accountId, dedupId)

        then: "account should be created"
        response != null
        response.success

        and: "account should exist"
        def account = accountManagementGateway.getAccount(accountId)
        account != null
        account.accountId == accountId
        account.state == Account.AccountState.ACTIVE
        1 * eventStore.append(_ as Event)
    }

    def "should perform transfer between accounts"() {
        given: "two accounts"
        def fromAccountId = "from_account_" + System.currentTimeMillis()
        def toAccountId = "to_account_" + System.currentTimeMillis()
        def dedupId = UUID.randomUUID().toString()
        eventStore.append(_ as Event) >> null

        and: "create accounts"
        accountManagementGateway.createAccount(fromAccountId, UUID.randomUUID().toString())
        accountManagementGateway.createAccount(toAccountId, UUID.randomUUID().toString())

        and: "initial balance for from account"
        balanceOperationGateway.increaseBalance(fromAccountId, new Balance("100.00"), UUID.randomUUID().toString())

        when: "performing transfer"
        def response = balanceOperationGateway.transfer(fromAccountId, toAccountId, new Balance("50.00"), dedupId)

        then: "transfer should be successful"
        response != null
        response.success

        and: "balances should be updated"
        def fromAccount = accountManagementGateway.getAccount(fromAccountId)
        def toAccount = accountManagementGateway.getAccount(toAccountId)
        fromAccount.balance.amount == "50.00"
        toAccount.balance.amount == "50.00"
        1 * eventStore.append(_ as Event)
    }

    def "should handle batch balance operations"() {
        given: "multiple accounts"
        def account1 = "batch_account1_" + System.currentTimeMillis()
        def account2 = "batch_account2_" + System.currentTimeMillis()
        def dedupId = UUID.randomUUID().toString()
        eventStore.append(_ as Event) >> null

        and: "create accounts"
        accountManagementGateway.createAccount(account1, UUID.randomUUID().toString())
        accountManagementGateway.createAccount(account2, UUID.randomUUID().toString())

        and: "initial balance"
        balanceOperationGateway.increaseBalance(account1, new Balance("100.00"), UUID.randomUUID().toString())

        when: "performing batch operation"
        def operations = [
            new BalanceOperation(accountId: account1, operationType: "DEBIT", amount: new BigDecimal("30.00")),
            new BalanceOperation(accountId: account2, operationType: "CREDIT", amount: new BigDecimal("30.00"))
        ]
        def response = balanceOperationGateway.batchBalanceOperation(operations, dedupId)

        then: "batch operation should be successful"
        response != null
        response.success

        and: "balances should be updated"
        def account1Balance = accountManagementGateway.getAccount(account1).balance
        def account2Balance = accountManagementGateway.getAccount(account2).balance
        account1Balance.amount == "70.00"
        account2Balance.amount == "30.00"
        1 * eventStore.append(_ as Event)
    }

    def "should handle account management operations"() {
        given: "an account"
        def accountId = "manage_account_" + System.currentTimeMillis()
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())
        eventStore.append(_ as Event) >> null

        when: "locking the account"
        def lockResponse = accountManagementGateway.lockAccount(accountId, UUID.randomUUID().toString())

        then: "account should be locked"
        lockResponse.success
        def lockedAccount = accountManagementGateway.getAccount(accountId)
        lockedAccount.state == Account.AccountState.LOCKED
        1 * eventStore.append(_ as Event)

        when: "unlocking the account"
        def unlockResponse = accountManagementGateway.unlockAccount(accountId, UUID.randomUUID().toString())

        then: "account should be unlocked"
        unlockResponse.success
        def unlockedAccount = accountManagementGateway.getAccount(accountId)
        unlockedAccount.state == Account.AccountState.ACTIVE
        1 * eventStore.append(_ as Event)

        when: "deleting the account"
        def deleteResponse = accountManagementGateway.deleteAccount(accountId, UUID.randomUUID().toString())

        then: "account should be deleted"
        deleteResponse.success
        def deletedAccount = accountManagementGateway.getAccount(accountId)
        deletedAccount.state == Account.AccountState.DELETED
        1 * eventStore.append(_ as Event)
    }

    def "should handle reserve and release operations"() {
        given: "an account with balance"
        def accountId = "reserve_account_" + System.currentTimeMillis()
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())
        balanceOperationGateway.increaseBalance(accountId, new Balance("100.00"), UUID.randomUUID().toString())
        eventStore.append(_ as Event) >> null

        when: "reserving amount"
        def reservationId = UUID.randomUUID().toString()
        def reserveResponse = balanceOperationGateway.reserve(accountId, new Balance("30.00"), reservationId, UUID.randomUUID().toString())

        then: "reservation should be successful"
        reserveResponse.success
        def accountAfterReserve = accountManagementGateway.getAccount(accountId)
        accountAfterReserve.balance.amount == "70.00"
        accountAfterReserve.reservedBalance.amount == "30.00"
        1 * eventStore.append(_ as Event)

        when: "releasing reservation"
        def releaseResponse = balanceOperationGateway.release(accountId, new Balance("30.00"), reservationId, UUID.randomUUID().toString())

        then: "release should be successful"
        releaseResponse.success
        def accountAfterRelease = accountManagementGateway.getAccount(accountId)
        accountAfterRelease.balance.amount == "100.00"
        accountAfterRelease.reservedBalance.amount == "0.00"
        1 * eventStore.append(_ as Event)
    }
}
