package com.example.demo.controller;

import com.example.demo.entity.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Properties;

@Controller
public class LoginController {

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    /**
     * 和shiro框架的交互完全通过Subject这个类去交互,用它完成登录,注销,获取当前的用户对象等操作
     * login请求调用subject.login之后，shiro会将token传递给自定义realm,此时realm会先调用doGetAuthenticationInfo(AuthenticationToken authcToken )登录验证的方法，验证通过后会接着调用 doGetAuthorizationInfo(PrincipalCollection principals)获取角色和权限的方法（授权），最后返回视图。
     * 当其他请求进入shiro时，shiro会调用doGetAuthorizationInfo(PrincipalCollection principals)去获取授权信息，若是没有权限或角色，会跳转到未授权页面，若有权限或角色，shiro会放行，此时进入真正的请求方法……
     * @param username
     * @param password
     * @param model
     * @param session
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
//    public String loginUser(String username,String password,boolean remeberMe,HttpSession session) {
    public String loginUser(HttpServletRequest request, String username, String password, Model model, HttpSession session) {
//        password=new SimpleHash("md5", password, ByteSource.Util.bytes(username.toLowerCase() + "shiro"),2).toHex();
//        UsernamePasswordToken usernamePasswordToken=new UsernamePasswordToken(username,password,remeberMe);
        UsernamePasswordToken usernamePasswordToken=new UsernamePasswordToken(username,password);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(usernamePasswordToken);   //完成登录
            User user=(User) subject.getPrincipal();
            //更新用户登录时间，也可以在ShiroRealm里面做
            session.setAttribute("user", user);
            model.addAttribute("user",user);
            return "index";
        } catch(Exception e) {
            String exception = (String) request.getAttribute("shiroLoginFailure");
            //logger.info("登录失败从request中获取shiro处理的异常信息,shiroLoginFailure:就是shiro异常类的全类名");
            model.addAttribute("msg",e.getMessage());
            return "login";//返回登录页面
        }
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session,Model model) {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
//        session.removeAttribute("user");
        model.addAttribute("msg","安全退出！");
        return "login";
    }
}