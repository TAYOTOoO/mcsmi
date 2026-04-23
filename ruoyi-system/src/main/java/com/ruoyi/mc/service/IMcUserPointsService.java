package com.ruoyi.mc.service;

import java.util.List;
import com.ruoyi.mc.domain.McUserPoints;

/**
 * 用户积分Service接口
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public interface IMcUserPointsService
{
    /**
     * 查询用户积分
     *
     * @param pointId 用户积分ID
     * @return 用户积分
     */
    public McUserPoints selectMcUserPointsByPointId(Long pointId);

    /**
     * 根据用户ID查询积分
     *
     * @param userId 用户ID
     * @return 用户积分
     */
    public McUserPoints selectMcUserPointsByUserId(Long userId);

    /**
     * 查询用户积分列表
     *
     * @param mcUserPoints 用户积分
     * @return 用户积分集合
     */
    public List<McUserPoints> selectMcUserPointsList(McUserPoints mcUserPoints);

    /**
     * 新增用户积分
     *
     * @param mcUserPoints 用户积分
     * @return 结果
     */
    public int insertMcUserPoints(McUserPoints mcUserPoints);

    /**
     * 修改用户积分
     *
     * @param mcUserPoints 用户积分
     * @return 结果
     */
    public int updateMcUserPoints(McUserPoints mcUserPoints);

    /**
     * 批量删除用户积分
     *
     * @param pointIds 需要删除的用户积分ID
     * @return 结果
     */
    public int deleteMcUserPointsByPointIds(Long[] pointIds);

    /**
     * 删除用户积分信息
     *
     * @param pointId 用户积分ID
     * @return 结果
     */
    public int deleteMcUserPointsByPointId(Long pointId);

    /**
     * 检查用户是否有足够积分
     *
     * @param userId 用户ID
     * @param points 需要的积分
     * @return 是否足够
     */
    public boolean hasEnoughPoints(Long userId, Integer points);

    /**
     * 扣除用户积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param taskId 关联任务ID
     * @param taskNo 任务编号
     * @param remark 备注
     * @return 结果
     */
    public int deductPoints(Long userId, Integer points, Long taskId, String taskNo, String remark);

    /**
     * 增加用户积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param remark 备注
     * @return 结果
     */
    public int addPoints(Long userId, Integer points, String remark);

    /**
     * 增加用户积分（带订单信息）
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param orderId 关联订单ID
     * @param orderNo 订单编号
     * @param remark 备注
     * @return 结果
     */
    public int addPoints(Long userId, Integer points, Long orderId, String orderNo, String remark);

    /**
     * 退还积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param taskId 关联任务ID
     * @param remark 备注
     * @return 结果
     */
    public int refundPoints(Long userId, Integer points, Long taskId, String remark);
}
