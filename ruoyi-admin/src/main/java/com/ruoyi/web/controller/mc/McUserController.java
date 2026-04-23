package com.ruoyi.web.controller.mc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mc.domain.McPointRecord;
import com.ruoyi.mc.domain.McUserPoints;
import com.ruoyi.mc.service.IMcPointRecordService;
import com.ruoyi.mc.service.IMcUserPointsService;
import com.ruoyi.mc.service.IMcSystemConfigService;
import com.ruoyi.mc.service.IMcUserSubscriptionService;
import com.ruoyi.mc.domain.McUserSubscription;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.framework.web.service.EmailService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户个人中心Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mc/user")
public class McUserController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private IMcUserPointsService userPointsService;

    @Autowired
    private IMcPointRecordService pointRecordService;

    @Autowired
    private IMcSystemConfigService configService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IMcUserSubscriptionService userSubscriptionService;

    /**
     * 获取用户信息
     */
    @GetMapping("/profile")
    public AjaxResult getProfile()
    {
        Long userId = SecurityUtils.getUserId();
        SysUser user = userService.selectUserById(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userName", user.getUserName());
        result.put("nickName", user.getNickName());
        result.put("email", user.getEmail());
        result.put("phonenumber", user.getPhonenumber());
        result.put("sex", user.getSex());
        result.put("createTime", user.getCreateTime());
        result.put("status", user.getStatus());

        // 统计生成次数
        // TODO: 添加生成次数统计
        result.put("taskCount", 0);

        return success(result);
    }

    /**
     * 获取积分信息
     */
    @GetMapping("/points")
    public AjaxResult getPoints()
    {
        Long userId = SecurityUtils.getUserId();
        McUserPoints userPoints = userPointsService.selectMcUserPointsByUserId(userId);

        if (userPoints == null) {
            // 如果用户没有积分记录，创建一个
            userPoints = new McUserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(0);
            userPoints.setUsedPoints(0);
            userPoints.setRemainingPoints(0);
            userPointsService.insertMcUserPoints(userPoints);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("currentPoints", userPoints.getRemainingPoints());
        result.put("totalSpent", userPoints.getUsedPoints());
        result.put("totalRecharged", userPoints.getTotalPoints());

        return success(result);
    }

    /**
     * 获取积分变动记录
     */
    @GetMapping("/points/records")
    public TableDataInfo getPointsRecords(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize)
    {
        Long userId = SecurityUtils.getUserId();
        startPage();
        List<McPointRecord> list = pointRecordService.selectMcPointRecordListByUserId(userId);
        return getDataTable(list);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public AjaxResult updatePassword(@RequestBody Map<String, String> params)
    {
        Long userId = SecurityUtils.getUserId();
        String newPassword = params.get("newPassword");
        String code = params.get("code");

        if (newPassword == null || code == null) {
            return error("参数错误");
        }

        // 验证新密码长度
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            return error("新密码长度必须在6-20位之间");
        }

        // 验证邮箱验证码
        SysUser user = userService.selectUserById(userId);
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return error("请先绑定邮箱");
        }

        if (!emailService.verifyCode(user.getEmail(), code)) {
            return error("验证码错误或已过期");
        }

        // 更新密码
        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        int result = userService.updateUser(user);

        return toAjax(result);
    }

    /**
     * 修改邮箱
     */
    @PutMapping("/email")
    public AjaxResult updateEmail(@RequestBody Map<String, String> params)
    {
        Long userId = SecurityUtils.getUserId();
        String oldCode = params.get("oldCode");
        String newEmail = params.get("newEmail");
        String newCode = params.get("newCode");

        if (oldCode == null || newEmail == null || newCode == null) {
            return error("参数错误");
        }

        // 验证新邮箱格式
        if (!newEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return error("邮箱格式不正确");
        }

        // 检查新邮箱是否已被其他用户使用
        SysUser checkUser = userService.selectUserByEmail(newEmail);
        if (checkUser != null && !checkUser.getUserId().equals(userId)) {
            return error("该邮箱已被其他用户使用");
        }

        // 验证旧邮箱验证码
        SysUser user = userService.selectUserById(userId);
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return error("当前账号未绑定邮箱");
        }

        if (!emailService.verifyCode(user.getEmail(), oldCode)) {
            return error("旧邮箱验证码错误或已过期");
        }

        // 验证新邮箱验证码
        if (!emailService.verifyCode(newEmail, newCode)) {
            return error("新邮箱验证码错误或已过期");
        }

        // 更新邮箱
        user.setEmail(newEmail);
        int result = userService.updateUser(user);

        return toAjax(result);
    }

    // ==================== 管理后台用户管理接口 ====================

    /**
     * 查询用户列表（包含积分信息）
     */
    @PreAuthorize("@ss.hasPermi('mc:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUserList(user);

        // 为每个用户添加积分信息和订阅套餐信息
        for (SysUser sysUser : list) {
            McUserPoints userPoints = userPointsService.selectMcUserPointsByUserId(sysUser.getUserId());
            Map<String, Object> params = new HashMap<>();
            if (userPoints != null) {
                params.put("totalPoints", userPoints.getTotalPoints());
                params.put("usedPoints", userPoints.getUsedPoints());
                params.put("remainingPoints", userPoints.getRemainingPoints());
            } else {
                // 没有积分记录时设置为0
                params.put("totalPoints", 0);
                params.put("usedPoints", 0);
                params.put("remainingPoints", 0);
            }

            // 查询用户订阅信息（包含套餐名称）
            if ("diamond".equals(sysUser.getSubscriptionType())) {
                McUserSubscription subscription = userSubscriptionService.selectMcUserSubscriptionByUserId(sysUser.getUserId());
                if (subscription != null) {
                    params.put("packageId", subscription.getPackageId());
                    params.put("packageName", subscription.getPackageName());
                }
            }

            sysUser.setParams(params);
        }

        return getDataTable(list);
    }

    /**
     * 修改用户积分（管理员）
     */
    @PreAuthorize("@ss.hasPermi('mc:user:edit')")
    @Log(title = "修改用户积分", businessType = BusinessType.UPDATE)
    @PutMapping("/points")
    public AjaxResult updateUserPoints(@RequestBody Map<String, Object> params)
    {
        Long userId = Long.parseLong(params.get("userId").toString());
        Integer totalPoints = Integer.parseInt(params.get("totalPoints").toString());

        if (totalPoints < 0) {
            return error("积分不能为负数");
        }

        // 查询用户积分记录
        McUserPoints userPoints = userPointsService.selectMcUserPointsByUserId(userId);

        if (userPoints == null) {
            // 如果没有积分记录，创建一个
            userPoints = new McUserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(totalPoints);
            userPoints.setUsedPoints(0);
            userPoints.setRemainingPoints(totalPoints);
            userPointsService.insertMcUserPoints(userPoints);

            // 记录积分变动
            McPointRecord record = new McPointRecord();
            record.setUserId(userId);
            record.setChangeType(1);
            record.setChangePoints(totalPoints);
            record.setAfterPoints(totalPoints);
            record.setRemark("管理员初始化积分");
            pointRecordService.insertMcPointRecord(record);
        } else {
            // 计算新的剩余积分 = 新的总积分 - 已消费积分
            Integer remainingPoints = totalPoints - userPoints.getUsedPoints();
            Integer pointsChange = remainingPoints - userPoints.getRemainingPoints();

            userPoints.setTotalPoints(totalPoints);
            userPoints.setRemainingPoints(remainingPoints);
            userPointsService.updateMcUserPoints(userPoints);

            // 记录积分变动
            if (pointsChange != 0) {
                McPointRecord record = new McPointRecord();
                record.setUserId(userId);
                record.setChangeType(pointsChange > 0 ? 1 : 2);
                record.setChangePoints(Math.abs(pointsChange));
                record.setBeforePoints(userPoints.getRemainingPoints() - pointsChange);
                record.setAfterPoints(remainingPoints);
                record.setRemark("管理员调整积分");
                pointRecordService.insertMcPointRecord(record);
            }
        }

        return success("修改成功");
    }

    /**
     * 修改用户状态（管理员）
     */
    @PreAuthorize("@ss.hasPermi('mc:user:edit')")
    @Log(title = "修改用户状态", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult updateUserStatus(@RequestBody Map<String, Object> params)
    {
        Long userId = Long.parseLong(params.get("userId").toString());
        String status = params.get("status").toString();

        SysUser user = userService.selectUserById(userId);
        if (user == null) {
            return error("用户不存在");
        }

        user.setStatus(status);
        int result = userService.updateUser(user);

        return toAjax(result);
    }

    /**
     * 批量修改用户状态（管理员）
     */
    @PreAuthorize("@ss.hasPermi('mc:user:edit')")
    @Log(title = "批量修改用户状态", businessType = BusinessType.UPDATE)
    @PutMapping("/batchStatus")
    public AjaxResult batchUpdateStatus(@RequestBody Map<String, Object> params)
    {
        @SuppressWarnings("unchecked")
        List<Integer> userIdsInt = (List<Integer>) params.get("userIds");
        String status = params.get("status").toString();

        if (userIdsInt == null || userIdsInt.isEmpty()) {
            return error("请选择要操作的用户");
        }

        // 转换为Long类型
        List<Long> userIds = new ArrayList<>();
        for (Integer id : userIdsInt) {
            userIds.add(id.longValue());
        }

        int successCount = 0;
        for (Long userId : userIds) {
            SysUser user = userService.selectUserById(userId);
            if (user != null) {
                user.setStatus(status);
                if (userService.updateUser(user) > 0) {
                    successCount++;
                }
            }
        }

        return success("成功修改" + successCount + "个用户状态");
    }

    /**
     * 重置用户密码（管理员）
     */
    @PreAuthorize("@ss.hasPermi('mc:user:edit')")
    @Log(title = "重置用户密码", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetUserPwd(@RequestBody Map<String, String> params)
    {
        Long userId = Long.parseLong(params.get("userId"));
        String password = params.get("password");

        if (password == null || password.trim().isEmpty()) {
            return error("密码不能为空");
        }

        if (password.length() < 6 || password.length() > 20) {
            return error("密码长度必须介于 6 和 20 之间");
        }

        SysUser user = userService.selectUserById(userId);
        if (user == null) {
            return error("用户不存在");
        }

        user.setPassword(SecurityUtils.encryptPassword(password));
        int result = userService.updateUser(user);

        return toAjax(result);
    }

    /**
     * 初始化用户积分（管理员）
     */
    @PreAuthorize("@ss.hasPermi('mc:user:edit')")
    @Log(title = "初始化用户积分", businessType = BusinessType.UPDATE)
    @PostMapping("/initPoints")
    public AjaxResult initUserPoints()
    {
        // 获取初始积分配置
        Integer initPoints = configService.selectConfigIntValue("system.user.initPoints", 50);

        // 查询所有用户
        List<SysUser> userList = userService.selectUserList(new SysUser());

        int count = 0;
        for (SysUser user : userList) {
            // 检查是否已有积分记录
            McUserPoints userPoints = userPointsService.selectMcUserPointsByUserId(user.getUserId());
            if (userPoints == null) {
                // 创建积分记录
                userPoints = new McUserPoints();
                userPoints.setUserId(user.getUserId());
                userPoints.setTotalPoints(initPoints);
                userPoints.setUsedPoints(0);
                userPoints.setRemainingPoints(initPoints);
                userPointsService.insertMcUserPoints(userPoints);

                // 记录积分变动
                McPointRecord record = new McPointRecord();
                record.setUserId(user.getUserId());
                record.setChangeType(1);
                record.setChangePoints(initPoints);
                record.setAfterPoints(initPoints);
                record.setRemark("系统初始化积分");
                pointRecordService.insertMcPointRecord(record);

                count++;
            }
        }

        return success("成功为 " + count + " 个用户初始化积分（每人 " + initPoints + " 积分）");
    }

    /**
     * 更新用户订阅（管理员）
     */
    @PreAuthorize("@ss.hasPermi('mc:user:edit')")
    @Log(title = "用户订阅", businessType = BusinessType.UPDATE)
    @PostMapping("/subscription/update")
    public AjaxResult updateUserSubscription(@RequestBody Map<String, Object> params)
    {
        try
        {
            Long userId = Long.valueOf(params.get("userId").toString());
            String subscriptionType = params.get("subscriptionType").toString();
            Integer isPermanent = params.containsKey("isPermanent") ? (Integer) params.get("isPermanent") : 0;
            String expireTime = params.containsKey("expireTime") && params.get("expireTime") != null
                ? params.get("expireTime").toString() : null;
            Long packageId = params.containsKey("packageId") && params.get("packageId") != null
                ? Long.valueOf(params.get("packageId").toString()) : null;

            // 查询或创建用户订阅记录
            McUserSubscription subscription = userSubscriptionService.selectMcUserSubscriptionByUserId(userId);
            if (subscription == null)
            {
                subscription = new McUserSubscription();
                subscription.setUserId(userId);
                subscription.setSubscriptionType(subscriptionType);
                subscription.setIsPermanent(isPermanent);
                subscription.setPackageId(packageId);

                // 处理到期时间
                if ("normal".equals(subscriptionType))
                {
                    // 普通用户不需要到期时间
                    subscription.setExpireTime(null);
                }
                else if ("diamond".equals(subscriptionType))
                {
                    // 钻石订阅用户必须提供有效的到期时间
                    // 永久订阅：2999-12-31 23:59:59
                    // 限时订阅：用户选择的到期时间
                    if (expireTime == null || expireTime.trim().isEmpty())
                    {
                        return error("钻石订阅必须设置到期时间");
                    }
                    subscription.setExpireTime(java.sql.Timestamp.valueOf(expireTime));
                }
                userSubscriptionService.insertMcUserSubscription(subscription);
            }
            else
            {
                subscription.setSubscriptionType(subscriptionType);
                subscription.setIsPermanent(isPermanent);
                subscription.setPackageId(packageId);

                // 处理到期时间
                if ("normal".equals(subscriptionType))
                {
                    // 普通用户不需要到期时间
                    subscription.setExpireTime(null);
                }
                else if ("diamond".equals(subscriptionType))
                {
                    // 钻石订阅用户必须提供有效的到期时间
                    // 永久订阅：2999-12-31 23:59:59
                    // 限时订阅：用户选择的到期时间
                    if (expireTime == null || expireTime.trim().isEmpty())
                    {
                        return error("钻石订阅必须设置到期时间");
                    }
                    subscription.setExpireTime(java.sql.Timestamp.valueOf(expireTime));
                }
                userSubscriptionService.updateSubscriptionByAdmin(subscription);
            }

            logger.info("管理员 {} 更新用户 {} 的订阅信息", SecurityUtils.getUsername(), userId);

            return success("订阅信息更新成功");
        }
        catch (Exception e)
        {
            logger.error("更新用户订阅失败", e);
            return error("更新订阅失败：" + e.getMessage());
        }
    }

    /**
     * 赠送用户积分（管理员）
     */
    @PreAuthorize("@ss.hasPermi('mc:user:edit')")
    @Log(title = "赠送积分", businessType = BusinessType.INSERT)
    @PostMapping("/points/grant")
    public AjaxResult grantUserPoints(@RequestBody Map<String, Object> params)
    {
        try
        {
            Long userId = Long.valueOf(params.get("userId").toString());
            Integer points = Integer.valueOf(params.get("points").toString());
            String remark = params.get("remark").toString();

            if (points <= 0)
            {
                return error("积分数量必须大于0");
            }

            // 添加积分
            userPointsService.addPoints(userId, points, remark);

            logger.info("管理员 {} 赠送用户 {} 积分 {}", SecurityUtils.getUsername(), userId, points);

            return success("赠送积分成功");
        }
        catch (Exception e)
        {
            logger.error("赠送积分失败", e);
            return error("赠送积分失败：" + e.getMessage());
        }
    }
}
