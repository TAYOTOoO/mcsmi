package com.ruoyi.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mc.service.IMcSystemConfigService;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务
 *
 * @author ruoyi
 */
@Service
public class EmailService
{
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IMcSystemConfigService configService;

    private static final String EMAIL_CODE_KEY = "email_code:";
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 获取配置的JavaMailSender
     */
    private JavaMailSenderImpl getConfiguredMailSender()
    {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        // 从数据库读取配置
        String host = configService.selectConfigValueByKey("mail.smtp.host");
        String port = configService.selectConfigValueByKey("mail.smtp.port");
        String username = configService.selectConfigValueByKey("mail.smtp.username");
        String password = configService.selectConfigValueByKey("mail.smtp.password");
        Boolean sslEnable = configService.selectConfigBoolValue("mail.smtp.ssl.enable", true);

        // 设置基本参数
        sender.setHost(host != null ? host : "smtp.qq.com");
        sender.setPort(port != null ? Integer.parseInt(port) : 465);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");

        // 设置属性
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.enable", sslEnable.toString());

        return sender;
    }

    /**
     * 获取发件人邮箱
     */
    private String getFromEmail()
    {
        String fromName = configService.selectConfigValueByKey("mail.smtp.from");
        String username = configService.selectConfigValueByKey("mail.smtp.username");

        if (StringUtils.isNotEmpty(fromName))
        {
            return fromName + " <" + username + ">";
        }
        return username;
    }

    /**
     * 发送邮箱验证码
     *
     * @param toEmail 收件人邮箱
     * @return 验证码
     */
    public String sendVerificationCode(String toEmail)
    {
        // 生成6位随机验证码
        String code = generateCode();

        // 发送邮件
        try
        {
            JavaMailSenderImpl sender = getConfiguredMailSender();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getFromEmail());
            message.setTo(toEmail);
            message.setSubject("材质工坊 - 注册验证码");
            message.setText("您的注册验证码是：" + code + "\n\n验证码有效期为 " + CODE_EXPIRE_MINUTES + " 分钟，请尽快完成注册。\n\n如非本人操作，请忽略此邮件。");

            sender.send(message);

            // 将验证码存入Redis，设置5分钟过期
            String key = EMAIL_CODE_KEY + toEmail;
            redisCache.setCacheObject(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            return code;
        }
        catch (Exception e)
        {
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送测试邮件
     *
     * @param toEmail 收件人邮箱
     */
    public void sendTestEmail(String toEmail)
    {
        try
        {
            JavaMailSenderImpl sender = getConfiguredMailSender();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getFromEmail());
            message.setTo(toEmail);
            message.setSubject("材质工坊 - 邮件测试");
            message.setText("这是一封测试邮件。\n\n如果您收到此邮件，说明邮件配置正确。\n\n材质工坊团队");

            sender.send(message);
        }
        catch (Exception e)
        {
            throw new RuntimeException("测试邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱
     * @param code 验证码
     * @return 是否正确
     */
    public boolean verifyCode(String email, String code)
    {
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(code))
        {
            return false;
        }

        String key = EMAIL_CODE_KEY + email;
        String cachedCode = redisCache.getCacheObject(key);

        if (cachedCode == null)
        {
            return false;
        }

        // 验证成功后删除验证码
        if (code.equals(cachedCode))
        {
            redisCache.deleteObject(key);
            return true;
        }

        return false;
    }

    /**
     * 生成6位随机数字验证码
     */
    private String generateCode()
    {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
