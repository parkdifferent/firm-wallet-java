package com.auticuro.firmwallet.common.model;

import com.baomidou.mybatisplus.annotation.*;
import firm_wallet.accountpb.Accountpb;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("balance")
public class Balance {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("account_id")
    private String accountId;
    
    @TableField("currency")
    private String currency;
    
    @TableField("available")
    private BigDecimal available;
    
    @TableField("reserved")
    private BigDecimal reserved;
    
    @TableField(exist = false)
    private Map<String, BigDecimal> reservations = new HashMap<>();
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    @TableLogic
    private Integer deleted;
    
    public BigDecimal getTotal() {
        return available.add(reserved);
    }
    
    public byte[] toBytes() {
        Accountpb.Balance.Builder builder = Accountpb.Balance.newBuilder()
                .setAvailable(available.toString())
                .setReserved(reserved.toString());

        // 将 Map<String, BigDecimal> 转换为 Map<String, String>
        reservations.forEach((key, value) ->
                builder.putReservations(key, value.toString())
        );
        return builder.build().toByteArray();
    }
    
    public static Balance fromBytes(byte[] bytes) throws Exception {
        Accountpb.Balance pbBalance = Accountpb.Balance.parseFrom(bytes);
        Balance balance = new Balance();
        balance.setAvailable(new BigDecimal(pbBalance.getAvailable()));
        balance.setReserved(new BigDecimal(pbBalance.getReserved()));

        // 将 Map<String, String> 转换为 Map<String, BigDecimal>
        Map<String, BigDecimal> reservationsMap = new HashMap<>();
        pbBalance.getReservationsMap().forEach((key, value) ->
                reservationsMap.put(key, new BigDecimal(value))
        );
        balance.setReservations(reservationsMap);
        return balance;
    }
}
