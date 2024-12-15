# Firm Wallet Service

A Java Spring Boot implementation of the Firm Wallet Service, providing account management and balance operation functionalities through gRPC interfaces.

## Features

- Account Management Service
  - Create Account
  - Delete Account
  - Lock/Unlock Account
  - Update Account Configuration
  - Query Account Information

- Balance Operation Service
  - Transfer
  - Batch Balance Operations
  - Reserve/Release Funds
  - Query Balance

## Technology Stack

- Java 8
- Spring Boot 2.7.x
- gRPC
- SOFAJRaft (for distributed consensus)
- H2 Database
- JPA/Hibernate

## Getting Started

### Prerequisites

- JDK 8
- Maven 3.6+

### Building the Project

```bash
mvn clean package
```

### Running the Service

```bash
java -jar target/firm-wallet-1.0-SNAPSHOT.jar
```

The service will start on:
- gRPC port: 9090
- JRaft port: 8081

## Configuration

Configuration can be modified in `src/main/resources/application.yml`:

- Database settings
- gRPC server port
- JRaft cluster settings

## Architecture

The service uses:
- Spring Boot for the application framework
- gRPC for service interfaces
- SOFAJRaft for distributed consensus
- JPA/Hibernate for data persistence
- H2 as the embedded database

## License

Apache License 2.0
