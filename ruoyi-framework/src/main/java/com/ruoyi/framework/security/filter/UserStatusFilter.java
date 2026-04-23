package com.ruoyi.framework.security.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 用户状态检查过滤器
 * 
 * @author ruoyi
 */
@Component
public class UserStatusFilter extends OncePerRequestFilter
{
    @Autowired
    private ISysUserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        // 获取当前认证信息
        UsernamePasswordAuthenticationToken authentication = 
            (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser)
        {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            Long userId = loginUser.getUserId();
            
            // 查询最新的用户状态
            SysUser user = userService.selectUserById(userId);
            
            // 检查用户是否被停用或删除
            if (user == null || "1".equals(user.getStatus()) || "2".equals(user.getDelFlag()))
            {
                // 清除认证信息
                SecurityContextHolder.clearContext();
                
                // 返回错误信息
                response.setStatus(HttpStatus.UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                
                AjaxResult ajaxResult = AjaxResult.error(HttpStatus.UNAUTHORIZED, "您的账号已被停用，请联系管理员");
                response.getWriter().print(JSON.toJSONString(ajaxResult));
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}
