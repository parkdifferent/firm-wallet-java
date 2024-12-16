CREATE TABLE IF NOT EXISTS account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(64) NOT NULL,
    state VARCHAR(20) NOT NULL,
    asset_class VARCHAR(20) NOT NULL,
    balance DECIMAL(20,8) NOT NULL DEFAULT 0,
    min_available DECIMAL(20,8) NOT NULL,
    max_available DECIMAL(20,8) NOT NULL,
    max_reserved DECIMAL(20,8) NOT NULL,
    updated_at DATETIME,
    version BIGINT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT uk_account_id UNIQUE (account_id)
);

CREATE TABLE IF NOT EXISTS balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(64) NOT NULL,
    available DECIMAL(20,8) NOT NULL,
    reserved DECIMAL(20,8) NOT NULL,
    updated_at DATETIME,
    version BIGINT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT uk_balance_account_id UNIQUE (account_id)
);

CREATE TABLE IF NOT EXISTS event_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    event_time DATETIME NOT NULL,
    account_id VARCHAR(64),
    event_data CLOB,
    created_at DATETIME,
    version BIGINT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT uk_event_id UNIQUE (event_id)
);
