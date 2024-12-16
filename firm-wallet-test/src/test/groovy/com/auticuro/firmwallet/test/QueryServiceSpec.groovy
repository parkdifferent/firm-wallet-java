package com.auticuro.firmwallet.test

import com.auticuro.firmwallet.access.gateway.AccountManagementGateway
import com.auticuro.firmwallet.access.gateway.BalanceOperationGateway
import com.auticuro.firmwallet.common.event.Event
import com.auticuro.firmwallet.common.model.Balance
import com.auticuro.firmwallet.common.service.QueryService
import com.auticuro.firmwallet.storage.auticuro.EventStore
import com.auticuro.firmwallet.test.config.TestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification
import spock.lang.Stepwise

@SpringBootTest(classes = TestConfiguration.class)
@Stepwise
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class QueryServiceSpec extends Specification {

    @Autowired
    private AccountManagementGateway accountManagementGateway

    @Autowired
    private BalanceOperationGateway balanceOperationGateway

    @Autowired
    private QueryService queryGateway

    @Autowired
    private EventStore eventStore

    def setup() {
        // Spring handles cleanup with @DirtiesContext
    }

    def "should query account balance"() {
        given: "an account with balance"
        def accountId = "query_balance_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())
        balanceOperationGateway.increaseBalance(accountId, new Balance("100.00"), UUID.randomUUID().toString())

        when: "querying balance"
        def balance = queryGateway.getBalance(accountId)

        then: "should return correct balance"
        balance != null
        balance.amount == "100.00"
        1 * eventStore.append(_ as Event)
    }

    def "should query transaction history"() {
        given: "an account with transactions"
        def accountId = "query_history_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())
        balanceOperationGateway.increaseBalance(accountId, new Balance("100.00"), UUID.randomUUID().toString())
        balanceOperationGateway.decreaseBalance(accountId, new Balance("30.00"), UUID.randomUUID().toString())

        when: "querying transaction history"
        def history = queryGateway.getTransactionHistory(accountId)

        then: "should return transaction list"
        history != null
        !history.empty
        history.size() == 2
        2 * eventStore.append(_ as Event)
    }

    def "should handle pagination in transaction history"() {
        given: "an account with multiple transactions"
        def accountId = "query_pagination_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())
        
        and: "perform multiple transactions"
        (1..5).each { 
            balanceOperationGateway.increaseBalance(accountId, new Balance("10.00"), UUID.randomUUID().toString())
        }

        when: "querying with pagination"
        def page1 = queryGateway.getTransactionHistory(accountId, 0, 2)
        def page2 = queryGateway.getTransactionHistory(accountId, 2, 2)

        then: "should return paginated results"
        page1.size() == 2
        page2.size() == 2
        5 * eventStore.append(_ as Event)
    }

    def "should query account status"() {
        given: "an account"
        def accountId = "query_status_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())

        when: "querying account status"
        def status = queryGateway.getAccountStatus(accountId)

        then: "should return correct status"
        status != null
        status.active
        1 * eventStore.append(_ as Event)
    }
}
