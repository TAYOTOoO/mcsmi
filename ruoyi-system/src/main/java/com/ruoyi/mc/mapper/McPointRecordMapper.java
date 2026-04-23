package com.ruoyi.mc.mapper;

import java.util.List;
import com.ruoyi.mc.domain.McPointRecord;

/**
 * 积分消费记录Mapper接口
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public interface McPointRecordMapper
{
    /**
     * 查询积分消费记录
     *
     * @param recordId 积分消费记录ID
     * @return 积分消费记录
     */
    public McPointRecord selectMcPointRecordByRecordId(Long recordId);

    /**
     * 查询积分消费记录列表
     *
     * @param mcPointRecord 积分消费记录
     * @return 积分消费记录集合
     */
    public List<McPointRecord> selectMcPointRecordList(McPointRecord mcPointRecord);

    /**
     * 新增积分消费记录
     *
     * @param mcPointRecord 积分消费记录
     * @return 结果
     */
    public int insertMcPointRecord(McPointRecord mcPointRecord);

    /**
     * 修改积分消费记录
     *
     * @param mcPointRecord 积分消费记录
     * @return 结果
     */
    public int updateMcPointRecord(McPointRecord mcPointRecord);

    /**
     * 删除积分消费记录
     *
     * @param recordId 积分消费记录ID
     * @return 结果
     */
    public int deleteMcPointRecordByRecordId(Long recordId);

    /**
     * 批量删除积分消费记录
     *
     * @param recordIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteMcPointRecordByRecordIds(Long[] recordIds);
}
