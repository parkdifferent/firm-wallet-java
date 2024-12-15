# Build stage
FROM maven:3.8.4-openjdk-8 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:8-jre-slim
WORKDIR /app

# Create directories for data
RUN mkdir -p /data/firm-wallet/rocksdb /data/firm-wallet/raft

# Copy artifacts from builder stage
COPY --from=builder /app/firm-wallet-access/target/firm-wallet-access-*.jar /app/access.jar
COPY --from=builder /app/firm-wallet-storage/target/firm-wallet-storage-*.jar /app/storage.jar
COPY --from=builder /app/firm-wallet-query/target/firm-wallet-query-*.jar /app/query.jar

# Environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1g"
ENV SPRING_PROFILES_ACTIVE="prod"

# Expose ports
EXPOSE 8080 9090 9092 8081

# Entry point script
COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
