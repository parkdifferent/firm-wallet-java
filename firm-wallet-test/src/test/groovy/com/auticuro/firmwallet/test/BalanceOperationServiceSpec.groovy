package com.auticuro.firmwallet.test

import com.auticuro.firmwallet.common.model.Balance
import com.auticuro.firmwallet.access.gateway.AccountManagementGateway
import com.auticuro.firmwallet.access.gateway.BalanceOperationGateway
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
class BalanceOperationServiceSpec extends Specification {

    @Autowired
    private BalanceOperationGateway balanceOperationGateway

    @Autowired
    private AccountManagementGateway accountManagementGateway

    @Autowired
    private EventStore eventStore

    def setup() {
        // Spring handles cleanup with @DirtiesContext
    }

    def "should handle balance increase"() {
        given: "an account"
        def accountId = "balance_increase_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())

        when: "increasing balance"
        def response = balanceOperationGateway.increaseBalance(
            accountId,
            new Balance("50.00"),
            UUID.randomUUID().toString()
        )

        then: "balance should be increased"
        response.success
        def account = accountManagementGateway.getAccount(accountId)
        account.balance.amount == "50.00"
        1 * eventStore.append(_ as Event)
    }

    def "should handle balance decrease"() {
        given: "an account with balance"
        def accountId = "balance_decrease_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())
        balanceOperationGateway.increaseBalance(accountId, new Balance("100.00"), UUID.randomUUID().toString())

        when: "decreasing balance"
        def response = balanceOperationGateway.decreaseBalance(
            accountId,
            new Balance("30.00"),
            UUID.randomUUID().toString()
        )

        then: "balance should be decreased"
        response.success
        def account = accountManagementGateway.getAccount(accountId)
        account.balance.amount == "70.00"
        1 * eventStore.append(_ as Event)
    }

    def "should prevent negative balance"() {
        given: "an account with balance"
        def accountId = "negative_balance_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())
        balanceOperationGateway.increaseBalance(accountId, new Balance("50.00"), UUID.randomUUID().toString())

        when: "attempting to decrease more than available"
        def response = balanceOperationGateway.decreaseBalance(
            accountId,
            new Balance("60.00"),
            UUID.randomUUID().toString()
        )

        then: "operation should fail"
        !response.success
        response.hasError()
        response.error.message.contains("insufficient balance")
        def account = accountManagementGateway.getAccount(accountId)
        account.balance.amount == "50.00"
        1 * eventStore.append(_ as Event)
    }

    def "should handle concurrent balance operations"() {
        given: "an account"
        def accountId = "concurrent_balance_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())
        balanceOperationGateway.increaseBalance(accountId, new Balance("100.00"), UUID.randomUUID().toString())

        when: "performing concurrent operations"
        def operations = (1..5).collect { i ->
            [
                balanceOperationGateway.decreaseBalance(accountId, new Balance("10.00"), UUID.randomUUID().toString()),
                balanceOperationGateway.increaseBalance(accountId, new Balance("10.00"), UUID.randomUUID().toString())
            ]
        }.flatten()

        then: "all operations should complete"
        operations.every { it.success }
        def finalAccount = accountManagementGateway.getAccount(accountId)
        finalAccount.balance.amount == "100.00"
        10 * eventStore.append(_ as Event)
    }

    def "should handle idempotent operations"() {
        given: "an account"
        def accountId = "idempotent_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())

        and: "operation details"
        def amount = new Balance("50.00")
        def requestId = UUID.randomUUID().toString()

        when: "performing same operation twice"
        def response1 = balanceOperationGateway.increaseBalance(accountId, amount, requestId)
        def response2 = balanceOperationGateway.increaseBalance(accountId, amount, requestId)

        then: "both operations should succeed"
        response1.success
        response2.success
        def account = accountManagementGateway.getAccount(accountId)
        account.balance.amount == "50.00"
        1 * eventStore.append(_ as Event)
    }
}
