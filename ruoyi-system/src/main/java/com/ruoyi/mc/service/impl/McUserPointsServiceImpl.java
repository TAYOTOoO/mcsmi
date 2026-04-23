package com.ruoyi.mc.service.impl;

import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.mc.mapper.McUserPointsMapper;
import com.ruoyi.mc.mapper.McPointRecordMapper;
import com.ruoyi.mc.domain.McUserPoints;
import com.ruoyi.mc.domain.McPointRecord;
import com.ruoyi.mc.service.IMcUserPointsService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.exception.ServiceException;

/**
 * 用户积分Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-26
 */
@Service
public class McUserPointsServiceImpl implements IMcUserPointsService
{
    @Autowired
    private McUserPointsMapper mcUserPointsMapper;

    @Autowired
    private McPointRecordMapper pointRecordMapper;

    @Override
    public McUserPoints selectMcUserPointsByPointId(Long pointId)
    {
        return mcUserPointsMapper.selectMcUserPointsByPointId(pointId);
    }

    @Override
    public McUserPoints selectMcUserPointsByUserId(Long userId)
    {
        return mcUserPointsMapper.selectMcUserPointsByUserId(userId);
    }

    @Override
    public List<McUserPoints> selectMcUserPointsList(McUserPoints mcUserPoints)
    {
        return mcUserPointsMapper.selectMcUserPointsList(mcUserPoints);
    }

    @Override
    public int insertMcUserPoints(McUserPoints mcUserPoints)
    {
        mcUserPoints.setCreateTime(DateUtils.getNowDate());
        return mcUserPointsMapper.insertMcUserPoints(mcUserPoints);
    }

    @Override
    public int updateMcUserPoints(McUserPoints mcUserPoints)
    {
        mcUserPoints.setUpdateTime(DateUtils.getNowDate());
        return mcUserPointsMapper.updateMcUserPoints(mcUserPoints);
    }

    @Override
    public int deleteMcUserPointsByPointIds(Long[] pointIds)
    {
        return mcUserPointsMapper.deleteMcUserPointsByPointIds(pointIds);
    }

    @Override
    public int deleteMcUserPointsByPointId(Long pointId)
    {
        return mcUserPointsMapper.deleteMcUserPointsByPointId(pointId);
    }

    @Override
    public boolean hasEnoughPoints(Long userId, Integer points)
    {
        McUserPoints userPoints = mcUserPointsMapper.selectMcUserPointsByUserId(userId);
        return userPoints != null && userPoints.getRemainingPoints() >= points;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deductPoints(Long userId, Integer points, Long taskId, String taskNo, String remark)
    {
        McUserPoints userPoints = mcUserPointsMapper.selectMcUserPointsByUserId(userId);
        if (userPoints == null)
        {
            throw new ServiceException("用户积分记录不存在");
        }

        if (userPoints.getRemainingPoints() < points)
        {
            throw new ServiceException("积分不足");
        }

        // 扣除积分
        int result = mcUserPointsMapper.deductPoints(userId, points);

        if (result > 0)
        {
            // 记录消费记录
            McPointRecord record = new McPointRecord();
            record.setUserId(userId);
            record.setUserName(userPoints.getUserName());
            record.setChangeType(2); // 消费
            record.setChangePoints(-points);
            record.setBeforePoints(userPoints.getRemainingPoints());
            record.setAfterPoints(userPoints.getRemainingPoints() - points);
            record.setTaskId(taskId);
            record.setTaskNo(taskNo);
            record.setRemark(remark);
            record.setCreateTime(new Date());
            pointRecordMapper.insertMcPointRecord(record);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addPoints(Long userId, Integer points, String remark)
    {
        McUserPoints userPoints = mcUserPointsMapper.selectMcUserPointsByUserId(userId);
        if (userPoints == null)
        {
            // 用户积分记录不存在，自动创建
            userPoints = new McUserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(0);
            userPoints.setUsedPoints(0);
            userPoints.setRemainingPoints(0);
            userPoints.setCreateTime(new Date());
            mcUserPointsMapper.insertMcUserPoints(userPoints);
        }

        // 增加积分
        int result = mcUserPointsMapper.addPoints(userId, points);

        if (result > 0)
        {
            // 记录充值记录
            McPointRecord record = new McPointRecord();
            record.setUserId(userId);
            record.setUserName(userPoints.getUserName());
            record.setChangeType(1); // 增加
            record.setChangePoints(points);
            record.setBeforePoints(userPoints.getRemainingPoints());
            record.setAfterPoints(userPoints.getRemainingPoints() + points);
            record.setRemark(remark);
            record.setCreateTime(new Date());
            pointRecordMapper.insertMcPointRecord(record);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addPoints(Long userId, Integer points, Long orderId, String orderNo, String remark)
    {
        McUserPoints userPoints = mcUserPointsMapper.selectMcUserPointsByUserId(userId);
        if (userPoints == null)
        {
            // 用户积分记录不存在，自动创建（新用户首次充值场景）
            userPoints = new McUserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(0);
            userPoints.setUsedPoints(0);
            userPoints.setRemainingPoints(0);
            userPoints.setCreateTime(new Date());
            mcUserPointsMapper.insertMcUserPoints(userPoints);
        }

        // 增加积分
        int result = mcUserPointsMapper.addPoints(userId, points);

        if (result > 0)
        {
            // 记录充值记录（orderId和orderNo存储在taskId和taskNo字段中）
            McPointRecord record = new McPointRecord();
            record.setUserId(userId);
            record.setUserName(userPoints.getUserName());
            record.setChangeType(1); // 增加
            record.setChangePoints(points);
            record.setBeforePoints(userPoints.getRemainingPoints());
            record.setAfterPoints(userPoints.getRemainingPoints() + points);
            record.setTaskId(orderId); // 复用taskId存储orderId
            record.setTaskNo(orderNo); // 复用taskNo存储orderNo
            record.setRemark(remark);
            record.setCreateTime(new Date());
            pointRecordMapper.insertMcPointRecord(record);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refundPoints(Long userId, Integer points, Long taskId, String remark)
    {
        McUserPoints userPoints = mcUserPointsMapper.selectMcUserPointsByUserId(userId);
        if (userPoints == null)
        {
            throw new ServiceException("用户积分记录不存在");
        }

        // 退还积分
        int result = mcUserPointsMapper.addPoints(userId, points);

        if (result > 0)
        {
            // 记录退还记录
            McPointRecord record = new McPointRecord();
            record.setUserId(userId);
            record.setUserName(userPoints.getUserName());
            record.setChangeType(3); // 退还
            record.setChangePoints(points);
            record.setBeforePoints(userPoints.getRemainingPoints());
            record.setAfterPoints(userPoints.getRemainingPoints() + points);
            record.setTaskId(taskId);
            record.setRemark(remark);
            record.setCreateTime(new Date());
            pointRecordMapper.insertMcPointRecord(record);
        }

        return result;
    }
}
