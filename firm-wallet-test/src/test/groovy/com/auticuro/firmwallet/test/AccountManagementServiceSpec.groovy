package com.auticuro.firmwallet.test

import com.auticuro.firmwallet.common.model.Account
import com.auticuro.firmwallet.access.gateway.AccountManagementGateway
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
class AccountManagementServiceSpec extends Specification {

    @Autowired
    private AccountManagementGateway accountManagementGateway

    @Autowired
    private EventStore eventStore

    def setup() {
        // Spring handles cleanup with @DirtiesContext
    }

    def "should create and manage account"() {
        given: "account details"
        def accountId = "test_account_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null

        when: "creating account"
        def createResponse = accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())

        then: "account should be created"
        createResponse.success
        1 * eventStore.append(_ as Event)

        and: "account should exist"
        def account = accountManagementGateway.getAccount(accountId)
        account != null
        account.accountId == accountId
        account.state == Account.AccountState.ACTIVE

        when: "locking account"
        def lockResponse = accountManagementGateway.lockAccount(accountId, UUID.randomUUID().toString())

        then: "account should be locked"
        lockResponse.success
        1 * eventStore.append(_ as Event)

        and: "account state should be updated"
        def lockedAccount = accountManagementGateway.getAccount(accountId)
        lockedAccount.state == Account.AccountState.LOCKED

        when: "unlocking account"
        def unlockResponse = accountManagementGateway.unlockAccount(accountId, UUID.randomUUID().toString())

        then: "account should be unlocked"
        unlockResponse.success
        1 * eventStore.append(_ as Event)

        and: "account state should be active"
        def unlockedAccount = accountManagementGateway.getAccount(accountId)
        unlockedAccount.state == Account.AccountState.ACTIVE
    }

    def "should handle account deletion"() {
        given: "an active account"
        def accountId = "delete_account_" + System.currentTimeMillis()
        eventStore.append(_ as Event) >> null
        accountManagementGateway.createAccount(accountId, UUID.randomUUID().toString())

        when: "deleting account"
        def deleteResponse = accountManagementGateway.deleteAccount(accountId, UUID.randomUUID().toString())

        then: "deletion should succeed"
        deleteResponse.success
        2 * eventStore.append(_ as Event)

        and: "account should be marked as deleted"
        def deletedAccount = accountManagementGateway.getAccount(accountId)
        deletedAccount.state == Account.AccountState.DELETED
    }
}
