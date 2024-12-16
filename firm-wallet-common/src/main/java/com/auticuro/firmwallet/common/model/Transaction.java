package com.auticuro.firmwallet.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("transaction")
public class Transaction {
    @TableId
    private String id;
    
    @TableField("from_account_id")
    private String fromAccountId;
    
    @TableField("to_account_id")
    private String toAccountId;
    
    @TableField("amount")
    private BigDecimal amount;
    
    @TableField("metadata")
    private String metadata;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("deleted")
    private Boolean deleted;
}
