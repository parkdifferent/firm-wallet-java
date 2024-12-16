package com.auticuro.firmwallet.test

import com.auticuro.firmwallet.common.model.Balance
import com.auticuro.firmwallet.access.gateway.AccountManagementGateway
import com.auticuro.firmwallet.access.gateway.BalanceOperationGateway
import com.auticuro.firmwallet.access.gateway.TransactionGateway
import com.auticuro.firmwallet.test.config.TestConfiguration
import com.auticuro.firmwallet.storage.auticuro.EventStore
import com.auticuro.firmwallet.common.event.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Stepwise
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(classes = TestConfiguration.class)
@Stepwise
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionServiceSpec extends Specification {

    @Autowired
    private AccountManagementGateway accountManagementGateway

    @Autowired
    private BalanceOperationGateway balanceOperationGateway

    @Autowired
    private TransactionGateway transactionGateway

    @Autowired
    private EventStore eventStore

    def setup() {
        // Spring handles cleanup with @DirtiesContext
    }

    def "should create and confirm transaction"() {
        given: "accounts with balance"
        def fromAccountId = "tx_from_" + System.currentTimeMillis()
        def toAccountId = "tx_to_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null

        accountManagementGateway.createAccount(fromAccountId, UUID.randomUUID().toString())
        accountManagementGateway.createAccount(toAccountId, UUID.randomUUID().toString())
        balanceOperationGateway.increaseBalance(fromAccountId, new Balance("100.00"), UUID.randomUUID().toString())

        when: "creating transaction"
        def txId = UUID.randomUUID().toString()
        def createResponse = transactionGateway.createTransaction(
            txId,
            fromAccountId,
            toAccountId,
            new Balance("30.00"),
            "Test transfer",
            UUID.randomUUID().toString()
        )

        then: "transaction should be created"
        createResponse.success
        1 * eventStore.append(_ as Event)

        when: "confirming transaction"
        def confirmResponse = transactionGateway.confirmTransaction(txId, UUID.randomUUID().toString())

        then: "transaction should be confirmed"
        confirmResponse.success
        1 * eventStore.append(_ as Event)

        and: "balances should be updated"
        def fromAccount = accountManagementGateway.getAccount(fromAccountId)
        def toAccount = accountManagementGateway.getAccount(toAccountId)
        fromAccount.balance.amount == "70.00"
        toAccount.balance.amount == "30.00"
    }

    def "should handle transaction rollback"() {
        given: "accounts with balance"
        def fromAccountId = "rollback_from_" + System.currentTimeMillis()
        def toAccountId = "rollback_to_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null

        accountManagementGateway.createAccount(fromAccountId, UUID.randomUUID().toString())
        accountManagementGateway.createAccount(toAccountId, UUID.randomUUID().toString())
        balanceOperationGateway.increaseBalance(fromAccountId, new Balance("100.00"), UUID.randomUUID().toString())

        and: "a created transaction"
        def txId = UUID.randomUUID().toString()
        transactionGateway.createTransaction(
            txId,
            fromAccountId,
            toAccountId,
            new Balance("30.00"),
            "Test rollback",
            UUID.randomUUID().toString()
        )

        when: "rolling back transaction"
        def rollbackResponse = transactionGateway.rollbackTransaction(txId, "Test reason", UUID.randomUUID().toString())

        then: "rollback should succeed"
        rollbackResponse.success
        2 * eventStore.append(_ as Event)

        and: "balances should remain unchanged"
        def fromAccount = accountManagementGateway.getAccount(fromAccountId)
        def toAccount = accountManagementGateway.getAccount(toAccountId)
        fromAccount.balance.amount == "100.00"
        toAccount.balance.amount == "0.00"
    }
}
