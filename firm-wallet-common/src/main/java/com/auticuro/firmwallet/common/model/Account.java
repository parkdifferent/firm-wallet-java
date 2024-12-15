package com.auticuro.firmwallet.common.model;

import com.baomidou.mybatisplus.annotation.*;
import com.google.protobuf.InvalidProtocolBufferException;
import firm_wallet.accountpb.Accountpb;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("account")
public class Account {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("account_id")
    private String accountId;
    
    @TableField("state")
    private AccountState state;
    
    @TableField("asset_class")
    private AssetClass assetClass;
    
    @TableField("min_available")
    private BigDecimal minAvailable;
    
    @TableField("max_available")
    private BigDecimal maxAvailable;
    
    @TableField("max_reserved")
    private BigDecimal maxReserved;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    @TableLogic
    private Integer deleted;
    
    public enum AccountState {
        ACTIVE,
        LOCKED,
        DELETED
    }
    
    public enum AssetClass {
        UNKNOWN,
        FIAT,
        CRYPTO
    }
    
    public byte[] toBytes() {
        Accountpb.Account.Builder builder = Accountpb.Account.newBuilder()
            .setAccountId(accountId)
            .setState(Accountpb.AccountState.valueOf(state.name()));

        Accountpb.AccountConfig.Builder configBuilder = Accountpb.AccountConfig.newBuilder()
            .setAssetClass(Accountpb.AssetClass.valueOf(assetClass.name()));

        Accountpb.BalanceLimit.Builder limitBuilder = Accountpb.BalanceLimit.newBuilder()
            .setMinAvailable(minAvailable.toString())
            .setMaxAvailable(maxAvailable.toString())
            .setMaxReserved(maxReserved.toString());

        configBuilder.setBalanceLimit(limitBuilder);
        builder.setConfig(configBuilder);
        
        return builder.build().toByteArray();
    }
    
    public static Account fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        Accountpb.Account proto = Accountpb.Account.parseFrom(bytes);
        Account account = new Account();
        account.setAccountId(proto.getAccountId());
        account.setState(AccountState.valueOf(proto.getState().name()));
        
        Accountpb.AccountConfig config = proto.getConfig();
        account.setAssetClass(AssetClass.valueOf(config.getAssetClass().name()));
        
        Accountpb.BalanceLimit limit = config.getBalanceLimit();
        account.setMinAvailable(new BigDecimal(limit.getMinAvailable()));
        account.setMaxAvailable(new BigDecimal(limit.getMaxAvailable()));
        account.setMaxReserved(new BigDecimal(limit.getMaxReserved()));
        
        return account;
    }
}
