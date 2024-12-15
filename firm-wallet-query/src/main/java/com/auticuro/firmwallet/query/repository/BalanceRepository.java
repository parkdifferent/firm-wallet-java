package com.auticuro.firmwallet.query.repository;

import com.auticuro.firmwallet.common.model.Balance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BalanceRepository extends BaseMapper<Balance> {
    
    @Select("SELECT * FROM balance WHERE account_id = #{accountId} AND currency = #{currency} AND deleted = 0")
    Balance findByAccountIdAndCurrency(@Param("accountId") String accountId, @Param("currency") String currency);
    
    @Select("SELECT * FROM balance WHERE account_id = #{accountId} AND deleted = 0")
    List<Balance> findByAccountId(@Param("accountId") String accountId);
    
}
