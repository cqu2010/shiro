package com.example.demo.filter;

import java.io.IOException;
import javax.servlet.ServletRequest;  
import javax.servlet.ServletResponse;  
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.subject.Subject;  
import org.apache.shiro.util.StringUtils;  
import org.apache.shiro.web.filter.authz.AuthorizationFilter;  
import org.apache.shiro.web.util.WebUtils;
/** 
 * @Type LoginFilter.java 
 * @Desc 用于自定义过滤器，过滤用户请求时是否是登录状态 loginFilter主要是覆盖了自带的authc过滤器，让未登录的请求统一返回401
 */  
public class LoginFilter extends AuthorizationFilter {
    @Override  
    protected boolean isAccessAllowed(ServletRequest req, ServletResponse resp, Object arg2) throws Exception {
        Subject subject = getSubject(req, resp);  
        if (null != subject.getPrincipals()) {  
            return true;  
        }  
        return false;  
    }  
  
    /** 
     * 会话超时或权限校验未通过的，统一返回401，由前端页面弹窗提示 
     */  
    @Override  
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        if (isAjax((HttpServletRequest) request)) {  
            WebUtils.toHttp(response).sendError(401);  
        } else {  
            String unauthorizedUrl = getUnauthorizedUrl();  
            if (StringUtils.hasText(unauthorizedUrl)) {  
                WebUtils.issueRedirect(request, response, unauthorizedUrl);  
            } else {  
                WebUtils.toHttp(response).sendError(401);  
            }  
        }
        return false;  
    }  
  
    private boolean isAjax(HttpServletRequest request) {  
        String header = request.getHeader("x-requested-with");  
        if (null != header && "XMLHttpRequest".endsWith(header)) {  
            return true;  
        }  
        return false;  
    }  
}  