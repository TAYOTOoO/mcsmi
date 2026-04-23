package com.ruoyi.mc.mapper;

import java.util.List;
import com.ruoyi.mc.domain.McGenerationLog;

/**
 * 材质生成日志Mapper接口
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public interface McGenerationLogMapper
{
    /**
     * 查询材质生成日志
     *
     * @param logId 日志ID
     * @return 材质生成日志
     */
    public McGenerationLog selectMcGenerationLogByLogId(Long logId);

    /**
     * 查询材质生成日志列表
     *
     * @param mcGenerationLog 材质生成日志
     * @return 材质生成日志集合
     */
    public List<McGenerationLog> selectMcGenerationLogList(McGenerationLog mcGenerationLog);

    /**
     * 新增材质生成日志
     *
     * @param mcGenerationLog 材质生成日志
     * @return 结果
     */
    public int insertMcGenerationLog(McGenerationLog mcGenerationLog);

    /**
     * 修改材质生成日志
     *
     * @param mcGenerationLog 材质生成日志
     * @return 结果
     */
    public int updateMcGenerationLog(McGenerationLog mcGenerationLog);

    /**
     * 删除材质生成日志
     *
     * @param logId 日志ID
     * @return 结果
     */
    public int deleteMcGenerationLogByLogId(Long logId);

    /**
     * 批量删除材质生成日志
     *
     * @param logIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteMcGenerationLogByLogIds(Long[] logIds);

    /**
     * 统计总调用次数
     *
     * @return 总次数
     */
    public Integer countTotalCalls();

    /**
     * 统计成功调用次数
     *
     * @return 成功次数
     */
    public Integer countSuccessCalls();

    /**
     * 计算平均生成耗时
     *
     * @return 平均耗时
     */
    public Double avgGenerationTime();
}
