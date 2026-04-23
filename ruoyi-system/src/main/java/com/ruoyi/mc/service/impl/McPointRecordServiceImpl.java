package com.ruoyi.mc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.mapper.McPointRecordMapper;
import com.ruoyi.mc.domain.McPointRecord;
import com.ruoyi.mc.service.IMcPointRecordService;

/**
 * 积分消费记录Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class McPointRecordServiceImpl implements IMcPointRecordService
{
    @Autowired
    private McPointRecordMapper mcPointRecordMapper;

    /**
     * 查询积分消费记录列表
     *
     * @param mcPointRecord 积分消费记录
     * @return 积分消费记录
     */
    @Override
    public List<McPointRecord> selectMcPointRecordList(McPointRecord mcPointRecord)
    {
        return mcPointRecordMapper.selectMcPointRecordList(mcPointRecord);
    }

    /**
     * 根据用户ID查询积分消费记录
     *
     * @param userId 用户ID
     * @return 积分消费记录集合
     */
    @Override
    public List<McPointRecord> selectMcPointRecordListByUserId(Long userId)
    {
        McPointRecord query = new McPointRecord();
        query.setUserId(userId);
        return mcPointRecordMapper.selectMcPointRecordList(query);
    }

    /**
     * 新增积分消费记录
     *
     * @param mcPointRecord 积分消费记录
     * @return 结果
     */
    @Override
    public int insertMcPointRecord(McPointRecord mcPointRecord)
    {
        return mcPointRecordMapper.insertMcPointRecord(mcPointRecord);
    }
}
