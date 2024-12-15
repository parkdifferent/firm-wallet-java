package com.auticuro.firmwallet.query.repository;

import com.auticuro.firmwallet.common.model.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AccountRepository extends BaseMapper<Account> {
    
    @Select("SELECT * FROM account WHERE account_id = #{accountId} AND deleted = 0")
    Account findByAccountId(@Param("accountId") String accountId);
    
}
