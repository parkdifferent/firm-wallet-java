package com.auticuro.firmwallet.query.repository;

import com.auticuro.firmwallet.common.model.EventLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EventLogRepository extends BaseMapper<EventLog> {
    
    @Delete("DELETE FROM event_logs WHERE id IN " +
            "(SELECT id FROM event_logs ORDER BY created_at ASC LIMIT #{batchSize})")
    int deleteOldestEvents(@Param("batchSize") long batchSize);
}
