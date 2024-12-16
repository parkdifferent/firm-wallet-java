package com.auticuro.firmwallet.test.integration

import com.auticuro.firmwallet.common.model.*
import com.auticuro.firmwallet.access.gateway.*
import com.auticuro.firmwallet.common.service.QueryService
import com.auticuro.firmwallet.test.config.TestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Stepwise

@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles("test")
@Stepwise
class WalletServiceIntegrationSpec extends Specification {

    @Autowired
    private AccountManagementGateway accountManagementGateway

    @Autowired
    private BalanceOperationGateway balanceOperationGateway

    @Autowired
    private TransactionGateway transactionGateway

    @Autowired
    private QueryService queryService

    def "should handle complete business workflow"() {
        given: "multiple accounts for testing"
        def accounts = (1..3).collect { i ->
            def accountId = "integration_${i}_" + System.currentTimeMillis()
            def response = accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())
            assert response.success
            return accountId
        }

        when: "setting up initial balances"
        accounts.each { accountId ->
            def response = balanceOperationGateway.increaseBalance(accountId, new Balance("100.00"), UUID.randomUUID().toString())
            assert response.success
        }

        then: "all accounts should have correct initial balance"
        accounts.every { accountId ->
            def account = accountManagementGateway.getAccount(accountId)
            account.balance.amount == "100.00"
        }

        when: "performing a chain of transactions"
        def tx1Id = UUID.randomUUID().toString()
        def tx1Response = transactionGateway.createTransaction(
            tx1Id,
            accounts[0],
            accounts[1],
            new Balance("30.00"),
            "First transfer",
            UUID.randomUUID().toString()
        )
        assert tx1Response.success
        transactionGateway.confirmTransaction(tx1Id, UUID.randomUUID().toString())

        def tx2Id = UUID.randomUUID().toString()
        def tx2Response = transactionGateway.createTransaction(
            tx2Id,
            accounts[1],
            accounts[2],
            new Balance("20.00"),
            "Second transfer",
            UUID.randomUUID().toString()
        )
        assert tx2Response.success
        transactionGateway.confirmTransaction(tx2Id, UUID.randomUUID().toString())

        then: "balances should be updated correctly"
        def finalBalances = accounts.collect { accountId ->
            accountManagementGateway.getAccount(accountId).balance.amount
        }
        finalBalances[0] == "70.00"   // 100 - 30
        finalBalances[1] == "110.00"  // 100 + 30 - 20
        finalBalances[2] == "120.00"  // 100 + 20

        and: "transaction history should be accurate"
        def account1History = queryService.getTransactionHistory(accounts[0], 0, 10)
        account1History.transactions.size() == 1
        account1History.transactions[0].id == tx1Id

        def account2History = queryService.getTransactionHistory(accounts[1], 0, 10)
        account2History.transactions.size() == 2
        account2History.transactions.collect { it.id }.containsAll([tx1Id, tx2Id])

        and: "account history should show all events"
        accounts.each { accountId ->
            def history = queryService.getAccountHistory(accountId, 0, 10)
            assert history.events.any { it.type == EventType.ACCOUNT_CREATED }
            assert history.events.any { it.type == EventType.BALANCE_INCREASED }
        }
    }

    def "should handle concurrent operations"() {
        given: "accounts for concurrent testing"
        def account1 = "concurrent_1_" + System.currentTimeMillis()
        def account2 = "concurrent_2_" + System.currentTimeMillis()
        accountManagementGateway.createAccount(account1, UUID.randomUUID().toString())
        accountManagementGateway.createAccount(account2, UUID.randomUUID().toString())

        and: "initial balance"
        balanceOperationGateway.increaseBalance(account1, new Balance("1000.00"), UUID.randomUUID().toString())

        when: "performing concurrent transfers"
        def futures = (1..10).collect { i ->
            def txId = UUID.randomUUID().toString()
            def dedupId = UUID.randomUUID().toString()
            transactionGateway.createTransaction(
                txId,
                account1,
                account2,
                new Balance("10.00"),
                "Concurrent transfer ${i}",
                dedupId
            )
        }

        then: "all transfers should be successful"
        futures.every { it.success }

        and: "final balances should be correct"
        def account1Balance = accountManagementGateway.getAccount(account1).balance
        def account2Balance = accountManagementGateway.getAccount(account2).balance
        account1Balance.amount == "900.00"  // 1000 - (10 * 10)
        account2Balance.amount == "100.00"  // 0 + (10 * 10)
    }
}
