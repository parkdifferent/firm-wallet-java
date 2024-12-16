package com.auticuro.firmwallet.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("balance_operation")
public class BalanceOperation {
    @TableId
    private String id;
    
    @TableField("account_id")
    private String accountId;
    
    @TableField("amount")
    private BigDecimal amount;
    
    @TableField("operation_type")
    private String operationType;  // CREDIT, DEBIT
    
    @TableField("transaction_id")
    private String transactionId;
    
    @TableField("metadata")
    private String metadata;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("deleted")
    private Boolean deleted;
}
