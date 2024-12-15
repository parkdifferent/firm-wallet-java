#!/bin/bash

# Common module
mkdir -p firm-wallet-common/src/main/java/com/auticuro/firmwallet/common/{model,exception,utils}
mv src/main/java/com/auticuro/firmwallet/entity/*.java firm-wallet-common/src/main/java/com/auticuro/firmwallet/common/model/
mv src/main/java/com/auticuro/firmwallet/exception/*.java firm-wallet-common/src/main/java/com/auticuro/firmwallet/common/exception/
mv src/main/java/com/auticuro/firmwallet/util/*.java firm-wallet-common/src/main/java/com/auticuro/firmwallet/common/utils/

# Access module
mkdir -p firm-wallet-access/src/main/java/com/auticuro/firmwallet/access/{gateway,translator}
mv src/main/java/com/auticuro/firmwallet/gateway/*.java firm-wallet-access/src/main/java/com/auticuro/firmwallet/access/gateway/

# Transaction module
mkdir -p firm-wallet-transaction/src/main/java/com/auticuro/firmwallet/transaction/{marker,saga}
mv src/main/java/com/auticuro/firmwallet/service/WalletOperationProcessor.java firm-wallet-transaction/src/main/java/com/auticuro/firmwallet/transaction/marker/
mv src/main/java/com/auticuro/firmwallet/message/*.java firm-wallet-transaction/src/main/java/com/auticuro/firmwallet/transaction/marker/

# Storage module
mkdir -p firm-wallet-storage/src/main/java/com/auticuro/firmwallet/storage/{auticuro,raft}
mv src/main/java/com/auticuro/firmwallet/storage/*.java firm-wallet-storage/src/main/java/com/auticuro/firmwallet/storage/auticuro/
mv src/main/java/com/auticuro/firmwallet/raft/*.java firm-wallet-storage/src/main/java/com/auticuro/firmwallet/storage/raft/

# Query module
mkdir -p firm-wallet-query/src/main/java/com/auticuro/firmwallet/query/{service,repository}
mv src/main/java/com/auticuro/firmwallet/repository/*.java firm-wallet-query/src/main/java/com/auticuro/firmwallet/query/repository/
mv src/main/java/com/auticuro/firmwallet/service/EventLogGCService.java firm-wallet-query/src/main/java/com/auticuro/firmwallet/query/service/
