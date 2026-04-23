package com.ruoyi.mc.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.mc.mapper.McUserSubscriptionMapper;
import com.ruoyi.mc.mapper.McSubscriptionPackageMapper;
import com.ruoyi.mc.domain.McUserSubscription;
import com.ruoyi.mc.domain.McSubscriptionPackage;
import com.ruoyi.mc.service.IMcUserSubscriptionService;
import com.ruoyi.mc.service.IMcUserPointsService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 用户订阅信息Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-07
 */
@Service
public class McUserSubscriptionServiceImpl implements IMcUserSubscriptionService
{
    @Autowired
    private McUserSubscriptionMapper mcUserSubscriptionMapper;

    @Autowired
    private McSubscriptionPackageMapper mcSubscriptionPackageMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private IMcUserPointsService mcUserPointsService;

    @Override
    public List<McUserSubscription> selectMcUserSubscriptionList(McUserSubscription mcUserSubscription)
    {
        return mcUserSubscriptionMapper.selectMcUserSubscriptionList(mcUserSubscription);
    }

    @Override
    public McUserSubscription selectMcUserSubscriptionBySubscriptionId(Long subscriptionId)
    {
        return mcUserSubscriptionMapper.selectMcUserSubscriptionBySubscriptionId(subscriptionId);
    }

    @Override
    public McUserSubscription selectMcUserSubscriptionByUserId(Long userId)
    {
        return mcUserSubscriptionMapper.selectMcUserSubscriptionByUserId(userId);
    }

    @Override
    public int insertMcUserSubscription(McUserSubscription mcUserSubscription)
    {
        mcUserSubscription.setCreateTime(DateUtils.getNowDate());
        return mcUserSubscriptionMapper.insertMcUserSubscription(mcUserSubscription);
    }

    @Override
    public int updateMcUserSubscription(McUserSubscription mcUserSubscription)
    {
        mcUserSubscription.setUpdateTime(DateUtils.getNowDate());
        return mcUserSubscriptionMapper.updateMcUserSubscription(mcUserSubscription);
    }

    @Override
    public int deleteMcUserSubscriptionBySubscriptionId(Long subscriptionId)
    {
        return mcUserSubscriptionMapper.deleteMcUserSubscriptionBySubscriptionId(subscriptionId);
    }

    @Override
    public int deleteMcUserSubscriptionBySubscriptionIds(Long[] subscriptionIds)
    {
        return mcUserSubscriptionMapper.deleteMcUserSubscriptionBySubscriptionIds(subscriptionIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public McUserSubscription activateSubscription(Long userId, Long packageId, int months)
    {
        // 查询套餐信息
        McSubscriptionPackage subscriptionPackage = mcSubscriptionPackageMapper.selectMcSubscriptionPackageByPackageId(packageId);
        if (subscriptionPackage == null)
        {
            throw new ServiceException("套餐不存在");
        }

        // 查询用户订阅信息
        McUserSubscription subscription = mcUserSubscriptionMapper.selectMcUserSubscriptionByUserId(userId);

        Date now = DateUtils.getNowDate();
        Date expireTime = calculateExpireTime(now, months);

        if (subscription == null)
        {
            // 创建新订阅记录
            subscription = new McUserSubscription();
            subscription.setUserId(userId);
            subscription.setSubscriptionType("diamond");
            subscription.setPackageId(packageId);
            subscription.setStartTime(now);
            subscription.setExpireTime(expireTime);
            subscription.setIsPermanent(0);
            subscription.setCreateTime(now);
            mcUserSubscriptionMapper.insertMcUserSubscription(subscription);
        }
        else
        {
            // 更新订阅记录
            subscription.setSubscriptionType("diamond");
            subscription.setPackageId(packageId);
            subscription.setStartTime(now);
            subscription.setExpireTime(expireTime);
            subscription.setIsPermanent(0);
            subscription.setUpdateTime(now);
            mcUserSubscriptionMapper.updateMcUserSubscription(subscription);
        }

        // 同步到sys_user表
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setSubscriptionType("diamond");
        sysUser.setSubscriptionExpireTime(expireTime);
        sysUser.setUpdateTime(now);
        sysUserMapper.updateUser(sysUser);

        // 发放首月积分
        if (subscriptionPackage.getMonthlyPoints() != null && subscriptionPackage.getMonthlyPoints() > 0)
        {
            String currentMonth = getCurrentMonth();
            grantMonthlyPoints(userId, currentMonth);
        }

        return subscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public McUserSubscription renewSubscription(Long userId, Long packageId, int months)
    {
        // 查询套餐信息
        McSubscriptionPackage subscriptionPackage = mcSubscriptionPackageMapper.selectMcSubscriptionPackageByPackageId(packageId);
        if (subscriptionPackage == null)
        {
            throw new ServiceException("套餐不存在");
        }

        // 查询用户订阅信息
        McUserSubscription subscription = mcUserSubscriptionMapper.selectMcUserSubscriptionByUserId(userId);
        if (subscription == null)
        {
            throw new ServiceException("用户订阅记录不存在");
        }

        Date now = DateUtils.getNowDate();
        Date newExpireTime;

        // 如果当前订阅未过期，从到期时间开始续费
        if (subscription.getExpireTime() != null && subscription.getExpireTime().after(now))
        {
            newExpireTime = calculateExpireTime(subscription.getExpireTime(), months);
        }
        else
        {
            // 如果已过期，从当前时间开始续费
            newExpireTime = calculateExpireTime(now, months);
        }

        // 更新订阅记录
        subscription.setSubscriptionType("diamond");
        subscription.setPackageId(packageId);
        subscription.setExpireTime(newExpireTime);
        subscription.setIsPermanent(0);
        subscription.setUpdateTime(now);
        mcUserSubscriptionMapper.updateMcUserSubscription(subscription);

        // 同步到sys_user表
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setSubscriptionType("diamond");
        sysUser.setSubscriptionExpireTime(newExpireTime);
        sysUser.setUpdateTime(now);
        sysUserMapper.updateUser(sysUser);

        // 发放本月积分（如果本月还未发放）
        if (subscriptionPackage.getMonthlyPoints() != null && subscriptionPackage.getMonthlyPoints() > 0)
        {
            String currentMonth = getCurrentMonth();
            grantMonthlyPoints(userId, currentMonth);
        }

        return subscription;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expireSubscription(Long userId)
    {
        // 查询用户订阅信息
        McUserSubscription subscription = mcUserSubscriptionMapper.selectMcUserSubscriptionByUserId(userId);
        if (subscription == null)
        {
            return;
        }

        Date now = DateUtils.getNowDate();

        // 更新mc_user_subscription表
        subscription.setSubscriptionType("normal");
        subscription.setUpdateTime(now);
        mcUserSubscriptionMapper.updateMcUserSubscription(subscription);

        // 同步到sys_user表
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setSubscriptionType("normal");
        sysUser.setSubscriptionExpireTime(null);
        sysUser.setUpdateTime(now);
        sysUserMapper.updateUser(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSubscriptionByAdmin(McUserSubscription subscription)
    {
        subscription.setUpdateTime(DateUtils.getNowDate());
        int result = mcUserSubscriptionMapper.updateMcUserSubscription(subscription);

        // 同步到sys_user表
        if (result > 0)
        {
            SysUser sysUser = new SysUser();
            sysUser.setUserId(subscription.getUserId());
            sysUser.setSubscriptionType(subscription.getSubscriptionType());
            sysUser.setSubscriptionExpireTime(subscription.getExpireTime());
            sysUser.setUpdateTime(DateUtils.getNowDate());
            sysUserMapper.updateUser(sysUser);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean grantMonthlyPoints(Long userId, String month)
    {
        // 查询用户订阅信息
        McUserSubscription subscription = mcUserSubscriptionMapper.selectMcUserSubscriptionByUserId(userId);
        if (subscription == null)
        {
            return false;
        }

        // 检查是否为钻石用户
        if (!"diamond".equals(subscription.getSubscriptionType()))
        {
            return false;
        }

        // 检查订阅是否有效
        if (!subscription.isActive())
        {
            return false;
        }

        // 检查本月是否已发放
        if (month.equals(subscription.getLastPointGrantMonth()))
        {
            return false;
        }

        // 查询套餐信息获取月度积分
        if (subscription.getPackageId() == null)
        {
            return false;
        }
        McSubscriptionPackage pkg = mcSubscriptionPackageMapper.selectMcSubscriptionPackageByPackageId(subscription.getPackageId());
        if (pkg == null || pkg.getMonthlyPoints() == null || pkg.getMonthlyPoints() <= 0)
        {
            return false;
        }
        Integer monthlyPoints = pkg.getMonthlyPoints();

        // 发放积分（使用change_type=6表示月度赠送）
        String remark = month + " 月度积分赠送";

        // 使用积分服务的addPoints方法，并手动创建记录设置change_type=6
        // 由于现有的addPoints方法change_type固定为1，这里需要特殊处理
        mcUserPointsService.addPoints(userId, monthlyPoints, remark);

        // 更新最后发放月份
        subscription.setLastPointGrantMonth(month);
        subscription.setUpdateTime(DateUtils.getNowDate());
        mcUserSubscriptionMapper.updateMcUserSubscription(subscription);

        return true;
    }

    @Override
    public void checkAndUpdateExpiredSubscriptions()
    {
        // 查询所有已到期的订阅
        List<McUserSubscription> expiredSubscriptions = mcUserSubscriptionMapper.selectExpiredSubscriptions();

        for (McUserSubscription subscription : expiredSubscriptions)
        {
            // 跳过永久订阅
            if (subscription.getIsPermanent() != null && subscription.getIsPermanent() == 1)
            {
                continue;
            }

            // 处理到期订阅
            expireSubscription(subscription.getUserId());
        }
    }

    @Override
    public List<McUserSubscription> selectDiamondUsersForPointGrant(String currentMonth)
    {
        return mcUserSubscriptionMapper.selectDiamondUsersForPointGrant(currentMonth);
    }

    @Override
    public List<McUserSubscription> selectExpiringSoonSubscriptions()
    {
        return mcUserSubscriptionMapper.selectExpiringSoonSubscriptions();
    }

    /**
     * 计算到期时间
     *
     * @param startTime 开始时间
     * @param months 月数
     * @return 到期时间
     */
    private Date calculateExpireTime(Date startTime, int months)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    /**
     * 获取当前月份（格式：2026-02）
     *
     * @return 当前月份
     */
    private String getCurrentMonth()
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        return String.format("%d-%02d", year, month);
    }

    /**
     * 批量发放月度积分
     *
     * @param currentMonth 当前月份
     * @return 发放用户数量
     */
    @Override
    public int batchGrantMonthlyPoints(String currentMonth)
    {
        // 查询需要发放积分的钻石用户
        List<McUserSubscription> users = mcUserSubscriptionMapper.selectDiamondUsersForPointGrant(currentMonth);
        int count = 0;

        for (McUserSubscription subscription : users)
        {
            boolean success = grantMonthlyPoints(subscription.getUserId(), currentMonth);
            if (success)
            {
                count++;
            }
        }

        return count;
    }
}
