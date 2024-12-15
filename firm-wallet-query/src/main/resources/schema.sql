CREATE TABLE IF NOT EXISTS account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(64) NOT NULL,
    state VARCHAR(20) NOT NULL,
    asset_class VARCHAR(20) NOT NULL,
    min_available DECIMAL(20,8) NOT NULL,
    max_available DECIMAL(20,8) NOT NULL,
    max_reserved DECIMAL(20,8) NOT NULL,
    updated_at DATETIME,
    version BIGINT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_account_id (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(64) NOT NULL,
    currency VARCHAR(20) NOT NULL,
    available DECIMAL(20,8) NOT NULL,
    reserved DECIMAL(20,8) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_account_currency (account_id, currency)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
