package com.ruoyi.mc.mapper;

import java.util.List;
import com.ruoyi.mc.domain.McTextureRecord;

/**
 * 材质记录Mapper接口
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public interface McTextureRecordMapper
{
    /**
     * 查询材质记录
     *
     * @param recordId 材质记录ID
     * @return 材质记录
     */
    public McTextureRecord selectMcTextureRecordByRecordId(Long recordId);

    /**
     * 查询材质记录列表
     *
     * @param mcTextureRecord 材质记录
     * @return 材质记录集合
     */
    public List<McTextureRecord> selectMcTextureRecordList(McTextureRecord mcTextureRecord);

    /**
     * 根据任务ID查询材质记录
     *
     * @param taskId 任务ID
     * @return 材质记录集合
     */
    public List<McTextureRecord> selectMcTextureRecordByTaskId(Long taskId);

    /**
     * 新增材质记录
     *
     * @param mcTextureRecord 材质记录
     * @return 结果
     */
    public int insertMcTextureRecord(McTextureRecord mcTextureRecord);

    /**
     * 修改材质记录
     *
     * @param mcTextureRecord 材质记录
     * @return 结果
     */
    public int updateMcTextureRecord(McTextureRecord mcTextureRecord);

    /**
     * 删除材质记录
     *
     * @param recordId 材质记录ID
     * @return 结果
     */
    public int deleteMcTextureRecordByRecordId(Long recordId);

    /**
     * 批量删除材质记录
     *
     * @param recordIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteMcTextureRecordByRecordIds(Long[] recordIds);
}
