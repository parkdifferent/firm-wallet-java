package com.auticuro.firmwallet.storage.raft;

import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.auticuro.firmwallet.config.WalletServiceConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import com.alipay.sofa.jraft.util.Endpoint;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;

@Data
@org.springframework.context.annotation.Configuration
@Slf4j
public class RaftConfig {

    @Value("${raft.dataPath}")
    private String dataPath;

    @Value("${raft.groupId}")
    private String groupId;

    @Value("${raft.serverId}")
    private String serverId;

    @Value("${raft.serverList}")
    private String serverList;

    @Value("${raft.election.timeout-ms:5000}")
    private int electionTimeoutMs;

    @Value("${raft.snapshot.interval-seconds:3600}")
    private int snapshotIntervalSeconds;

    @Bean
    public NodeOptions nodeOptions() {
        NodeOptions nodeOptions = new NodeOptions();

        // Check and create the data path if it doesn't exist
        File dataDir = new File(dataPath);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs(); // Create the directory structure
            if (!created) {
                log.error("Failed to create data directory: {}", dataPath);
                throw new IllegalStateException("Unable to create data directory");
            }
        }

        // Check if log, meta, and snapshot directories exist, create if not
        new File(dataPath + "/log").mkdirs();
        new File(dataPath + "/meta").mkdirs();
        new File(dataPath + "/snapshot").mkdirs();

        // Parameter checks
        Assert.hasText(dataPath, "Data path must not be empty");
        Assert.hasText(groupId, "Group ID must not be empty");

        // Configure storage paths
        nodeOptions.setLogUri(dataPath + "/log");
        nodeOptions.setRaftMetaUri(dataPath + "/meta");
        nodeOptions.setSnapshotUri(dataPath + "/snapshot");

        // Timeout and performance configuration
        nodeOptions.setElectionTimeoutMs(electionTimeoutMs);
        nodeOptions.setDisableCli(false);
        nodeOptions.setSnapshotIntervalSecs(snapshotIntervalSeconds);

        // Configure cluster nodes
        Configuration conf = new Configuration();
        if (StringUtils.hasText(serverList)) {
            if (!conf.parse(serverList)) {
                log.error("Failed to parse server list: {}", serverList);
                throw new IllegalArgumentException("Invalid server list configuration");
            }
            nodeOptions.setInitialConf(conf);
        } else {
            log.error("Server list is empty");
            throw new IllegalArgumentException("Server list cannot be empty");
        }

        return nodeOptions;
    }


    @Bean
    public PeerId peerId() {
        Assert.hasText(serverList, "Server list cannot be empty");
        String[] serverNodes = serverList.split(",");

        try {
            // 处理节点标识映射
            if (!serverId.contains(":")) {
                int nodeIndex = getNodeIndex(serverId);
                Assert.isTrue(nodeIndex < serverNodes.length,
                        "Node index out of bounds for server list");
                return PeerId.parsePeer(serverNodes[nodeIndex]);
            }

            // 直接解析完整的host:port
            return PeerId.parsePeer(serverId);
        } catch (Exception e) {
            log.error("Failed to parse PeerId: {}", serverId, e);
            throw new IllegalArgumentException("Invalid server ID configuration", e);
        }
    }

    // 根据节点名获取索引
    private int getNodeIndex(String nodeId) {
        switch (nodeId.toLowerCase()) {
            case "node1": return 0;
            case "node2": return 1;
            case "node3": return 2;
            default:
                throw new IllegalArgumentException("Unknown node identifier: " + nodeId);
        }
    }

    @Bean
    public RpcServer rpcServer(PeerId peerId) {
        Endpoint endpoint = peerId.getEndpoint();
        Assert.notNull(endpoint, "Endpoint cannot be null");
        return RaftRpcServerFactory.createRaftRpcServer(endpoint);
    }

    @Bean
    public Node node(RaftStateMachine fsm,
                     NodeOptions nodeOptions,
                     RpcServer rpcServer,
                     PeerId peerId) {
        log.info("Initializing RAFT Node with details:");
        log.info("Group ID: {}", groupId);
        log.info("Peer ID: {}", peerId);
        log.info("Server List: {}", serverList);
        log.info("Node Options: {}", nodeOptions);

        nodeOptions.setFsm(fsm);
        RaftGroupService raftGroupService = new RaftGroupService(
                groupId,
                peerId,
                nodeOptions,
                rpcServer
        );

        Node node = raftGroupService.start();
        if (node == null) {
            throw new IllegalStateException("Failed to start RAFT node - returned null");
        }

        log.info("RAFT Node started successfully. Group: {}, ServerId: {}",
                groupId, peerId);
        return node;
    }
}
