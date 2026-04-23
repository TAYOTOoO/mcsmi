package com.ruoyi.mc.service;

import java.util.List;
import com.ruoyi.mc.domain.McPointRecord;

/**
 * 积分消费记录Service接口
 *
 * @author ruoyi
 */
public interface IMcPointRecordService
{
    /**
     * 查询积分消费记录列表
     *
     * @param mcPointRecord 积分消费记录
     * @return 积分消费记录集合
     */
    public List<McPointRecord> selectMcPointRecordList(McPointRecord mcPointRecord);

    /**
     * 根据用户ID查询积分消费记录
     *
     * @param userId 用户ID
     * @return 积分消费记录集合
     */
    public List<McPointRecord> selectMcPointRecordListByUserId(Long userId);

    /**
     * 新增积分消费记录
     *
     * @param mcPointRecord 积分消费记录
     * @return 结果
     */
    public int insertMcPointRecord(McPointRecord mcPointRecord);
}
