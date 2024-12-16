package com.auticuro.firmwallet.query.repository;

import com.auticuro.firmwallet.common.model.Transaction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TransactionRepository extends BaseMapper<Transaction> {
    
    @Select("SELECT * FROM transaction WHERE id = #{id}")
    Optional<Transaction> findById(@Param("id") String id);
    
    @Select("SELECT * FROM transaction WHERE from_account_id = #{accountId} OR to_account_id = #{accountId} ORDER BY created_at DESC")
    List<Transaction> findByAccountId(@Param("accountId") String accountId);
}
