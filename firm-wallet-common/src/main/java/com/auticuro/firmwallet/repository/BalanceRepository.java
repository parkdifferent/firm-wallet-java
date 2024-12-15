package com.auticuro.firmwallet.repository;

import com.auticuro.firmwallet.common.model.Balance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BalanceRepository extends BaseMapper<Balance> {
}
