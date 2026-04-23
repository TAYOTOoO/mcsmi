package com.ruoyi.web.controller.system;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysPostService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.mc.service.IMcUserSubscriptionService;
import com.ruoyi.mc.service.IMcUserPointsService;
import com.ruoyi.mc.service.IMcPointRecordService;
import com.ruoyi.mc.domain.McUserSubscription;
import com.ruoyi.mc.domain.McUserPoints;
import com.ruoyi.mc.domain.McPointRecord;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * 用户信息
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysPostService postService;

    @Autowired
    private IMcUserSubscriptionService userSubscriptionService;

    @Autowired
    private IMcUserPointsService userPointsService;

    @Autowired
    private IMcPointRecordService pointRecordService;

    /**
     * 获取用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user)
    {
        startPage();

        // 支持邮箱搜索
        if (StringUtils.isNotEmpty(user.getEmail())) {
            user.getParams().put("email", user.getEmail());
        }

        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUser user)
    {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.importTemplateExcel(response, "用户数据");
    }

    /**
     * 根据用户编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = { "/", "/{userId}" })
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (StringUtils.isNotNull(userId))
        {
            userService.checkUserDataScope(userId);
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SecurityUtils.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.selectPostAll());
        return ajax;
    }

    /**
     * 新增用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user)
    {
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        if (ArrayUtils.contains(userIds, getUserId()))
        {
            return error("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(getUsername());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 根据用户编号获取授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId)
    {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SecurityUtils.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 用户授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds)
    {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds);
        userService.insertUserAuth(userId, roleIds);
        return success();
    }

    /**
     * 获取部门树列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree(SysDept dept)
    {
        return success(deptService.selectDeptTreeList(dept));
    }

    /**
     * 更新用户订阅信息（管理员）
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "更新用户订阅", businessType = BusinessType.UPDATE)
    @PostMapping("/subscription/update")
    public AjaxResult updateUserSubscription(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String subscriptionType = params.get("subscriptionType").toString();
            Integer isPermanent = Integer.valueOf(params.get("isPermanent").toString());
            Date expireTime = null;

            if (isPermanent == 0 && params.get("expireTime") != null) {
                // 前端传来的是时间戳（毫秒）
                Long timestamp = Long.valueOf(params.get("expireTime").toString());
                expireTime = new Date(timestamp);
            }

            McUserSubscription subscription = userSubscriptionService.selectMcUserSubscriptionByUserId(userId);

            if (subscription == null) {
                // 创建新订阅
                subscription = new McUserSubscription();
                subscription.setUserId(userId);
                subscription.setSubscriptionType(subscriptionType);
                subscription.setIsPermanent(isPermanent);
                subscription.setExpireTime(expireTime);
                if ("diamond".equals(subscriptionType) && subscription.getStartTime() == null) {
                    subscription.setStartTime(new Date());
                }
                userSubscriptionService.insertMcUserSubscription(subscription);
            } else {
                // 更新现有订阅
                subscription.setSubscriptionType(subscriptionType);
                subscription.setIsPermanent(isPermanent);
                subscription.setExpireTime(expireTime);
                userSubscriptionService.updateMcUserSubscription(subscription);
            }

            return success("更新订阅成功");
        } catch (Exception e) {
            logger.error("更新用户订阅失败", e);
            return error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 赠送用户积分（管理员）
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "赠送用户积分", businessType = BusinessType.GRANT)
    @PostMapping("/points/grant")
    public AjaxResult grantUserPoints(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Integer points = Integer.valueOf(params.get("points").toString());
            String remark = params.getOrDefault("remark", "管理员赠送").toString();

            if (points <= 0) {
                return error("积分数必须大于0");
            }

            // 查询用户积分记录
            McUserPoints userPoints = userPointsService.selectMcUserPointsByUserId(userId);

            if (userPoints == null) {
                // 创建新积分记录
                userPoints = new McUserPoints();
                userPoints.setUserId(userId);
                userPoints.setTotalPoints(points);
                userPoints.setUsedPoints(0);
                userPoints.setRemainingPoints(points);
                userPointsService.insertMcUserPoints(userPoints);
            } else {
                // 更新现有积分记录
                userPoints.setTotalPoints(userPoints.getTotalPoints() + points);
                userPoints.setRemainingPoints(userPoints.getRemainingPoints() + points);
                userPointsService.updateMcUserPoints(userPoints);
            }

            // 记录积分变动
            McPointRecord record = new McPointRecord();
            record.setUserId(userId);
            record.setChangeType(4); // 赠送类型
            record.setChangePoints(points);
            record.setBeforePoints(userPoints.getRemainingPoints() - points);
            record.setAfterPoints(userPoints.getRemainingPoints());
            record.setRemark(remark);
            pointRecordService.insertMcPointRecord(record);

            logger.info("管理员 {} 赠送用户 {} 积分 {}，原因：{}", getUsername(), userId, points, remark);

            return success("赠送积分成功");
        } catch (Exception e) {
            logger.error("赠送用户积分失败", e);
            return error("赠送失败：" + e.getMessage());
        }
    }
}
