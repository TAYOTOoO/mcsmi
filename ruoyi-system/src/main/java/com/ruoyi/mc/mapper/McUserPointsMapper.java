package com.ruoyi.mc.mapper;

import java.util.List;
import com.ruoyi.mc.domain.McUserPoints;
import org.apache.ibatis.annotations.Param;

/**
 * 用户积分Mapper接口
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public interface McUserPointsMapper
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
     * 删除用户积分
     *
     * @param pointId 用户积分ID
     * @return 结果
     */
    public int deleteMcUserPointsByPointId(Long pointId);

    /**
     * 批量删除用户积分
     *
     * @param pointIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteMcUserPointsByPointIds(Long[] pointIds);

    /**
     * 扣除用户积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @return 结果
     */
    public int deductPoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 增加用户积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @return 结果
     */
    public int addPoints(@Param("userId") Long userId, @Param("points") Integer points);
}
