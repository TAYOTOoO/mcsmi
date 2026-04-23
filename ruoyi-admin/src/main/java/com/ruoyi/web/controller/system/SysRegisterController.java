package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.service.EmailService;
import com.ruoyi.framework.web.service.SysRegisterService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;

import java.util.Map;

/**
 * 注册验证
 *
 * @author ruoyi
 */
@RestController
public class SysRegisterController extends BaseController
{
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ISysUserService userService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendEmailCode")
    public AjaxResult sendEmailCode(@RequestBody Map<String, String> params)
    {
        String email = params.get("email");

        if (StringUtils.isEmpty(email))
        {
            return error("邮箱不能为空");
        }

        // 验证邮箱格式
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
        {
            return error("邮箱格式不正确");
        }

        try
        {
            emailService.sendVerificationCode(email);
            return success("验证码已发送到您的邮箱，请注意查收");
        }
        catch (Exception e)
        {
            return error("验证码发送失败：" + e.getMessage());
        }
    }

    /**
     * 验证用户名和邮箱是否匹配（用于忘记密码）
     */
    @PostMapping("/verifyUserEmail")
    public AjaxResult verifyUserEmail(@RequestBody Map<String, String> params)
    {
        String username = params.get("username");
        String email = params.get("email");

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(email))
        {
            return error("用户名和邮箱不能为空");
        }

        // 验证邮箱格式
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
        {
            return error("邮箱格式不正确");
        }

        // 根据用户名查询用户
        SysUser user = userService.selectUserByUserName(username);
        if (user == null)
        {
            return error("用户名不存在");
        }

        // 验证邮箱是否匹配
        if (user.getEmail() == null || !user.getEmail().equals(email))
        {
            return error("用户名与邮箱不匹配");
        }

        return success("验证通过");
    }

    /**
     * 重置密码（忘记密码功能）
     */
    @PostMapping("/resetPassword")
    public AjaxResult resetPassword(@RequestBody Map<String, String> params)
    {
        String username = params.get("username");
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(email) ||
            StringUtils.isEmpty(code) || StringUtils.isEmpty(newPassword))
        {
            return error("所有字段不能为空");
        }

        // 验证新密码长度
        if (newPassword.length() < 6 || newPassword.length() > 20)
        {
            return error("密码长度必须在6-20位之间");
        }

        // 验证邮箱格式
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
        {
            return error("邮箱格式不正确");
        }

        // 根据用户名查询用户
        SysUser user = userService.selectUserByUserName(username);
        if (user == null)
        {
            return error("用户名不存在");
        }

        // 验证邮箱是否匹配
        if (user.getEmail() == null || !user.getEmail().equals(email))
        {
            return error("用户名与邮箱不匹配");
        }

        // 验证邮箱验证码
        if (!emailService.verifyCode(email, code))
        {
            return error("验证码错误或已过期");
        }

        // 更新密码
        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        int result = userService.updateUserProfile(user);

        if (result > 0)
        {
            return success("密码重置成功，请使用新密码登录");
        }
        else
        {
            return error("密码重置失败");
        }
    }
}
