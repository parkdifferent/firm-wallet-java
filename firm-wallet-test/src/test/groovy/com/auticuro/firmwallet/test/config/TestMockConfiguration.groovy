package com.auticuro.firmwallet.test.config

import com.alipay.sofa.jraft.Node
import com.alipay.sofa.jraft.RaftGroupService
import com.alipay.sofa.jraft.conf.Configuration
import com.alipay.sofa.jraft.entity.PeerId
import com.alipay.sofa.jraft.option.NodeOptions
import com.alipay.sofa.jraft.rpc.RpcServer
import com.auticuro.firmwallet.access.gateway.AccountManagementGateway
import com.auticuro.firmwallet.access.gateway.BalanceOperationGateway
import com.auticuro.firmwallet.access.gateway.InternalGateway
import com.auticuro.firmwallet.access.gateway.TransactionGateway
import com.auticuro.firmwallet.common.service.QueryService
import com.auticuro.firmwallet.repository.AccountRepository
import com.auticuro.firmwallet.repository.BalanceRepository
import com.auticuro.firmwallet.storage.auticuro.EventStore
import com.auticuro.firmwallet.storage.raft.RaftStateMachine
import com.auticuro.firmwallet.storage.rocksdb.RocksDBEventStoreConfig
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import spock.mock.DetachedMockFactory
import net.devh.boot.grpc.server.serverfactory.GrpcServerFactory
import net.devh.boot.grpc.server.service.GrpcService

@TestConfiguration
class TestMockConfiguration {
    
    private final detachedMockFactory = new DetachedMockFactory()

    @Bean
    @Primary
    RocksDBEventStoreConfig rocksDBEventStoreConfig() {
        def eventPath = "${System.getProperty("java.io.tmpdir")}/rocksdb-test-${UUID.randomUUID()}/events"
        def rocksdbPath = "${System.getProperty("java.io.tmpdir")}/rocksdb-test-${UUID.randomUUID()}"
        def config = detachedMockFactory.Stub(RocksDBEventStoreConfig)
        config.eventPath = eventPath
        config.rocksdbPath = rocksdbPath
        return config
    }

    @Bean
    @Primary
    EventStore eventStore() {
        return detachedMockFactory.Mock(EventStore)
    }

    @Bean
    @Primary
    AccountManagementGateway accountManagementGateway() {
        return detachedMockFactory.Mock(AccountManagementGateway)
    }

    @Bean
    @Primary
    BalanceOperationGateway balanceOperationGateway() {
        return detachedMockFactory.Mock(BalanceOperationGateway)
    }

    @Bean
    @Primary
    InternalGateway internalGateway() {
        return detachedMockFactory.Mock(InternalGateway)
    }

    @Bean
    @Primary
    TransactionGateway transactionGateway() {
        return detachedMockFactory.Mock(TransactionGateway)
    }

    @Bean
    @Primary
    QueryService queryService() {
        return detachedMockFactory.Mock(QueryService)
    }

    @Bean
    @Primary
    AccountRepository accountRepository() {
        return detachedMockFactory.Mock(AccountRepository)
    }

    @Bean
    @Primary
    BalanceRepository balanceRepository() {
        return detachedMockFactory.Mock(BalanceRepository)
    }

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        return new ObjectMapper()
    }

    // Mock Raft components for testing
    @Bean
    @Primary
    Node node() {
        def node = detachedMockFactory.Stub(Node)
        node.isLeader() >> true
        return node
    }

    @Bean
    @Primary
    RaftStateMachine raftStateMachine() {
        return detachedMockFactory.Mock(RaftStateMachine)
    }

    @Bean
    @Primary
    NodeOptions nodeOptions() {
        def options = detachedMockFactory.Stub(NodeOptions)
        options.electionTimeoutMs = 5000
        options.snapshotIntervalSecs = 3600
        return options
    }

    @Bean
    @Primary
    RpcServer rpcServer() {
        return detachedMockFactory.Mock(RpcServer)
    }

    @Bean
    @Primary
    PeerId peerId() {
        return PeerId.parsePeer("localhost:10001")
    }

    @Bean
    @Primary
    Configuration raftConfiguration() {
        return new Configuration([PeerId.parsePeer("localhost:10001")])
    }

    @Bean
    @Primary
    RaftGroupService raftGroupService() {
        def service = detachedMockFactory.Mock(RaftGroupService) {
            start() >> { true }
        }
        return service
    }

    // Mock gRPC server factory to avoid port binding issues
    @Bean
    @Primary
    GrpcServerFactory grpcServerFactory() {
        return detachedMockFactory.Mock(GrpcServerFactory)
    }
}
