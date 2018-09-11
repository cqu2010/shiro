package com.example.demo.resolver;

import org.apache.shiro.mgt.SecurityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import java.util.Properties;

/**
 * 定制的异常处理类
 */

//    private void applyUnauthorizedUrlIfNecessary(Filter filter) {
//        String unauthorizedUrl = getUnauthorizedUrl();
//        if (StringUtils.hasText(unauthorizedUrl) && (filter instanceof AuthorizationFilter)) {
//            AuthorizationFilter authzFilter = (AuthorizationFilter) filter;
//            //only apply the unauthorizedUrl if they haven't explicitly configured one already:
//            String existingUnauthorizedUrl = authzFilter.getUnauthorizedUrl();
//            if (existingUnauthorizedUrl == null) {
//                authzFilter.setUnauthorizedUrl(unauthorizedUrl);
//            }
//        }
//    }

//shiro默认过滤器(10个)
//        anon -- org.apache.shiro.web.filter.authc.AnonymousFilter
//        authc -- org.apache.shiro.web.filter.authc.FormAuthenticationFilter
//        authcBasic -- org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
//        perms -- org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter
//        port -- org.apache.shiro.web.filter.authz.PortFilter
//        rest -- org.apache.shiro.web.filter.authz.HttpMethodPermissionFilter
//        roles -- org.apache.shiro.web.filter.authz.RolesAuthorizationFilter
//        ssl -- org.apache.shiro.web.filter.authz.SslFilter
//        user -- org.apache.shiro.web.filter.authc.UserFilter
//        logout -- org.apache.shiro.web.filter.authc.LogoutFilter
@Configuration
public class DzExceptionResolver {
    /**
     * shiro中unauthorizedUrl不起作用,这是因为shiro源代码private void applyUnauthorizedUrlIfNecessary(Filter filter)中判断了filter是否为AuthorizationFilter，只有perms，roles，ssl，rest，port才是属于AuthorizationFilter，而anon，authcBasic，auchc，user是AuthenticationFilter，所以unauthorizedUrl设置后不起作用。
     * 解决方法：在shiro配置文件中添加（异常全路径做key，错误页面做value）
     * <bean class="org.springframework.web.servlet.handler.SimpleMappingExceptionResolver">
     *       <property name="exceptionMappings">
     *          <props>
     *               <prop key="org.apache.shiro.authz.UnauthorizedException">/403</prop>
     *          </props>
     *       </property>
     *   </bean>
     */
    @Bean
    public SimpleMappingExceptionResolver getSimpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver=new SimpleMappingExceptionResolver();
        Properties properties=new Properties();
        properties.setProperty("org.apache.shiro.authz.UnauthorizedException","/unauthorized");
        simpleMappingExceptionResolver.setExceptionMappings(properties);
        return simpleMappingExceptionResolver;
    }

    /**
     * 相当于调用SecurityUtils.setSecurityManager(securityManager)
     * @param securityManager
     * @return
     */
    @Bean
    public MethodInvokingFactoryBean getMethodInvokingFactoryBean(@Qualifier("securityManager")SecurityManager securityManager) {
        MethodInvokingFactoryBean methodInvokingFactoryBean=new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
        methodInvokingFactoryBean.setArguments(securityManager);
        return methodInvokingFactoryBean;
    }
}
