package com.example.demo.resolver;

import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * setUnauthorizedUrl("/403")不起作用的处理方式有
 * 1、DzExceptionResolver，
 * 2、实现该类MyExceptionResolver，在启动类添加：
 * // 注册统一异常处理bean
 * @Bean
 * public MyExceptionResolver myExceptionResolver() {
 *      return new MyExceptionResolver();
 * }
 *
 */
public class MyExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        if (e instanceof UnauthorizedException) {
            ModelAndView mv = new ModelAndView("/403");
            return mv;
        }
        return null;
    }
}