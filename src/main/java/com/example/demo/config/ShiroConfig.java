package com.example.demo.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.example.demo.listener.ShiroSessionListener;
import com.example.demo.realm.CredentialsMatcher;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import com.example.demo.realm.ShiroRealm;
import org.apache.shiro.codec.Base64;

import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.ExecutorServiceSessionValidationScheduler;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.ValidatingSessionManager;
import org.apache.shiro.session.mgt.eis.*;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
//import org.apache.shiro.session.mgt.quartz.QuartzSessionValidationScheduler


/**
 * shiro的配置类,需要注意一点filterChainDefinitionMap必须是LinkedHashMap因为它必须保证有序
 * @author Administrator
 *
 */
@Configuration
public class ShiroConfig {

	//授权缓存管理器
	@Bean
	public EhCacheManager getEhCacheManager() {
		EhCacheManager em = new EhCacheManager();
		em.setCacheManagerConfigFile("classpath:config/ehcache-shiro.xml");
		return em;
	}

	/**
	 * 使用上面的Ehcache或下面的shiro自带的内存缓存实现
	 */
//	@Bean
//	public MemoryConstrainedCacheManager getMemoryConstrainedCacheManager() {
//		return new MemoryConstrainedCacheManager();
//	}

	/**
	 * ShiroFilterFactoryBean 处理拦截资源文件问题。
	 * 注意：单独一个ShiroFilterFactoryBean配置是或报错的，因为在
	 * 初始化ShiroFilterFactoryBean的时候需要注入：SecurityManager

	 *
	 * Filter Chain定义说明 1、一个URL可以配置多个Filter，使用逗号分隔 2、当设置多个过滤器时，全部验证通过，才视为通过
	 * 3、部分过滤器可指定参数，如perms，roles
	 *
	 *     <bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
	 *			<property name="securityManager" ref="securityManager" />
	 *			<!-- 配置登录页 -->
	 *			<property name="loginUrl" value="/login.jsp" />
	 *			<!-- 配置登录成功后的页面 -->
	 *			<property name="successUrl" value="/list.jsp" />
	 *			<property name="unauthorizedUrl" value="/unauthorized.jsp" />
	 *			<property name="filterChainDefinitions">
	 *				<value>
	 *					<!-- 静态资源允许访问 -->
	 *					<!-- 登录页允许访问 -->
	 *					/login.jsp = anon
	 *					/test/login = anon
	 *					/user/delete = perms["delete"]
	 *					/logout = logout
	 *					<!-- 其他资源都需要认证 -->
	 *					 /** = authc
	 *				</value>
	 *			</property>
	 *		</bean>
	 */
	/**
	 * Shiro主过滤器本身功能十分强大,其强大之处就在于它支持任何基于URL路径表达式的、自定义的过滤器的执行
	 * Web应用中,Shiro可控制的Web请求必须经过Shiro主过滤器的拦截,Shiro对基于Spring的Web应用提供了完美的支持
	 * @param securityManager
	 * @return
	 */
	@Bean(name="shiroFilter")
	public ShiroFilterFactoryBean shiroFilterFactoryBean(@Qualifier("securityManager") SecurityManager securityManager) {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
		// 必须设置 SecurityManager,Shiro的核心安全接口
		shiroFilterFactoryBean.setSecurityManager(securityManager);
		// 配置登录的url，如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面（源码）
		shiroFilterFactoryBean.setLoginUrl("/login"); //这是后台的/控制器
		// 登录成功后要跳转的链接，本例中此属性用不到,因为登录成功后的处理逻辑在LoginController里硬编码了
		shiroFilterFactoryBean.setSuccessUrl("/index"); //这是Index.html页面
		// 未授权界面;配置不会被拦截的链接 顺序判断
		shiroFilterFactoryBean.setUnauthorizedUrl("/unauthorized"); ////这里设置403并不会起作用

// 有自定义拦截器就放开 wangzs（源码）
//		//自定义拦截器
//		LinkedHashMap<String, Filter> filtersMap = new LinkedHashMap<>();
//		//限制同一帐号同时在线的个数。
//		//filtersMap.put("kickout", kickoutSessionControlFilter());
//		shiroFilterFactoryBean.setFilters(filtersMap);

		// 配置访问权限,权限控制map.Shiro连接约束配置,即过滤链的定义，
		LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
		// 配置不会被拦截的链接 顺序判断
		// value值的'/'代表的路径是相对于HttpServletRequest.getContextPath()的值来的
		// anon：它对应的过滤器里面是空的,什么都没做,这里.do和.jsp后面的*表示参数,比方说login.jsp?main这种
		// authc：该过滤器下的页面必须验证后才能访问,它是Shiro内置的一个拦截器org.apache.shiro.web.filter.authc.FormAuthenticationFilter
		// 配置退出过滤器,其中的具体的退出代码Shiro已经替我们实现了
		// filterChainDefinitionMap.put("/logout", "logout");
		// 从数据库获取动态的权限
		// filterChainDefinitionMap.put("/add", "perms[权限添加]"); /userList=roles[admin],需要有admin这个角色，如果没有此角色访问此URL会返回无授权页面,或authc,perms[user:list]
		//       <!-- 需要权限为add的用户才能访问此请求-->
        //        /user=perms[user:add]
		//       <!-- 需要管理员角色才能访问此页面 -->
        //        /user/add=roles[admin]或roles[admin]，perms[user:add]
		// <!-- 过滤链定义，从上向下顺序执行，一般将 /**放在最为下边 -->:这是一个坑呢，一不小心代码就不好使了;
		// <!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问-->
		//logout这个拦截器是shiro已经实现好了的。
		// 从数据库获取
        /*List<SysPermissionInit> list = sysPermissionInitService.selectAll();

        for (SysPermissionInit sysPermissionInit : list) {
            filterChainDefinitionMap.put(sysPermissionInit.getUrl(),
                    sysPermissionInit.getPermissionInit());
        }*/

		/**
		 * 可自定义过滤器，比如myFilter替代authc
		 * <bean id="myFilter" class="com.cmcc.hygcc.comm.shiro.MyFilter"></bean>
		 * <property name="filters">
		 * 		<map>
		 * 			<entry key="myFilter" value-ref="myFilter" />
		 * 				<!-- 覆盖authc过滤器，使得未登录的ajax请求返回401状态 -->
		 * 			<entry key="authc" value-ref="loginFilter" />
		 * 		</map>
		 * </property>
		 *
		 * /**=myFilter
		 */
		//静态资源允许访问//登录页允许访问,一个URL可以配置多个Filter,使用逗号分隔,当设置多个过滤器时，全部验证通过，才视为通过,部分过滤器可指定参数，如perms，roles
//		filterChainDefinitionMap.put("/login.html*", "anon"); //表示可以匿名访问，*表示参数如?error等
		filterChainDefinitionMap.put("/login", "anon");
		filterChainDefinitionMap.put("/unauthorized", "anon");
		filterChainDefinitionMap.put("/css/**", "anon");
		filterChainDefinitionMap.put("/js/**", "anon");
		filterChainDefinitionMap.put("/fonts/**", "anon");
		filterChainDefinitionMap.put("/plugins/**", "anon");
		filterChainDefinitionMap.put("/img/**", "anon");
		filterChainDefinitionMap.put("/druid/**", "anon");
		filterChainDefinitionMap.put("/user/regist", "anon");
		filterChainDefinitionMap.put("/gifCode", "anon");
		filterChainDefinitionMap.put("/logout", "logout"); //logout是shiro提供的过滤器
		filterChainDefinitionMap.put("/user/delete", "perms[\"user:delete\"]"); //此时访问/user/delete需要delete权限,在自定义Realm中为用户授权。
		//其他资源都需要认证
		filterChainDefinitionMap.put("/**", "authc");

		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

		return shiroFilterFactoryBean; //Shiro拦截器工厂类注入成功
	}

	//配置核心安全事务管理器
	@Bean(name="securityManager")
	public SecurityManager securityManager(){
		DefaultWebSecurityManager securityManager =  new DefaultWebSecurityManager();
		// 设置realm.
		securityManager.setRealm(shiroRealm()); //如果方法加上参数@Qualifier("shiroRealm") ShiroRealm shiroRealm，可以直接securityManager.setRealm(shiroRealm);
		//注入记住我管理器;
		securityManager.setRememberMeManager(rememberMeManager());
		// 自定义缓存实现 可使用redis
//		securityManager.setCacheManager(cacheManager());
		securityManager.setCacheManager(getEhCacheManager()); //缓存管理器
		// 自定义session管理 可使用redis
		securityManager.setSessionManager(sessionManager());
		return securityManager;
	}

	//Shiro生命周期处理器
	@Bean(name = "lifecycleBeanPostProcessor")
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	//配置自定义的权限登录器，本段不需要配置自定义的密码比较器，可以换成下面的~~里面的写法，需要创建自定义的密码比较器CredentialsMatcher.java
	@Bean //必须,身份认证realm; (这个需要自己写，账号密码校验；权限等)
	public ShiroRealm shiroRealm(){
		ShiroRealm shiroRealm = new ShiroRealm();
		return shiroRealm;
	}

//需要这种方式就放开
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//	//配置自定义的权限登录器
//	@Bean(name="shiroRealm")
//	public ShiroRealm shiroRealm(@Qualifier("credentialsMatcher") CredentialsMatcher matcher) {
//		ShiroRealm shiroRealm=new ShiroRealm();
//		shiroRealm.setCredentialsMatcher(matcher);
//		return shiroRealm;
//	}
//	//配置自定义的密码比较器
//	@Bean(name="credentialsMatcher")
//	public CredentialsMatcher credentialsMatcher() {
//		return new CredentialsMatcher();
//	}
//	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * shiro的密码比较器
	 * @return
	 */
//	@Bean
//	public HashedCredentialsMatcher hashedCredentialsMatcher() {
//		HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
//		hashedCredentialsMatcher.setHashAlgorithmName("md5");//散列算法:这里使用MD5算法;
//		hashedCredentialsMatcher.setHashIterations(2);//散列的次数，比如散列两次，相当于 md5(md5(""));
//		return hashedCredentialsMatcher;
//	}



	/** //必须
	 * cookie对象;会话Cookie模板 ,默认为: JSESSIONID 问题: 与SERVLET容器名冲突,重新定义为sid或rememberMe，自定义
	 * @return
	 */
	public SimpleCookie rememberMeCookie() {
		//这个参数是cookie的名称，对应前端的checkbox的name = rememberMe
		SimpleCookie cookie = new SimpleCookie("rememberMe");
		//<!-- 记住我cookie生效时间30天 ,单位秒;-->
		cookie.setHttpOnly(true);
		cookie.setMaxAge(86400);
		return cookie;
	}

	/** //必须
	 * cookie管理对象;记住我功能,rememberMe管理器
	 * @return
	 */
	public CookieRememberMeManager rememberMeManager() {
		CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
		cookieRememberMeManager.setCookie(rememberMeCookie());
		//rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度(128 256 512 位) //3AvVhmFLUs0KTA3Kprsdag==
		cookieRememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
		return cookieRememberMeManager;
	}

	/**
	 * 使用shiro注解为用户授权 1. 在shiro-config.xml开启shiro注解(硬编码,修改权限码很麻烦)
	 * 在方法上配置注解@RequiresPermissions("xxx:yyy")
	 * <bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator" depends-on="lifecycleBeanPostProcessor"/>
	 * <bean class="org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor">
	 * 		<property name="securityManager" ref="securityManager"/>
	 * </bean>
	 * 编程方式实现用户权限控制
	 *     Subject subject = SecurityUtils.getSubject();
	 *     if(subject.hasRole("admin")){
	 *     		//有权限
	 *		}else{
	 *			//无权限
	 *		}
	 * @return AOP式方法级权限检查 1
	 */
	@Bean
	@DependsOn({"lifecycleBeanPostProcessor"})
	public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		advisorAutoProxyCreator.setProxyTargetClass(true);
		return advisorAutoProxyCreator;
	}
	// AOP式方法级权限检查 2
	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) { //@Qualifier("securityManager") SecurityManager manager
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}

	@Bean //必须（thymeleaf页面使用shiro标签控制按钮是否显示） //未引入thymeleaf包，Caused by: java.lang.ClassNotFoundException: org.thymeleaf.dialect.AbstractProcessorDialect
	public ShiroDialect shiroDialect() {
		return new ShiroDialect();
	}

	//SessionManager和SessionDAO可以不配置，会话DAO
	@Bean
	public SessionDAO sessionDAO() {
		MemorySessionDAO sessionDAO = new MemorySessionDAO();
		return sessionDAO;
	}

	/**
	 * sessionDao的方法2
	 * @return
	 */
//	@Bean //
//	public SessionIdGenerator sessionIdGenerator() {
//		return new JavaUuidSessionIdGenerator();
//	}
//	@Bean
//	public SessionDAO sessionDAO() {
//		EnterpriseCacheSessionDAO cacheSessionDAO=new EnterpriseCacheSessionDAO();
//		cacheSessionDAO.setActiveSessionsCacheName("shiro-activeSessionCache");
//		cacheSessionDAO.setSessionIdGenerator(sessionIdGenerator());
//		return cacheSessionDAO;
//	}

	//// 会话管理器，设定会话超时及保存
	@Bean
	public SessionManager sessionManager() {
		DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
		Collection<SessionListener> listeners = new ArrayList<SessionListener>();
		listeners.add(new ShiroSessionListener());
		sessionManager.setSessionListeners(listeners);
		sessionManager.setGlobalSessionTimeout(1800000); //全局会话超时时间（单位毫秒），默认30分钟
		sessionManager.setSessionDAO(sessionDAO());
		sessionManager.setDeleteInvalidSessions(true);
		sessionManager.setSessionValidationSchedulerEnabled(true);
		//定时清理失效会话, 清理用户直接关闭浏览器造成的孤立会话
		sessionManager.setSessionValidationInterval(1800000);
//		sessionManager.setSessionValidationScheduler(executorServiceSessionValidationScheduler());
		sessionManager.setSessionIdCookieEnabled(true);
		sessionManager.setSessionIdCookie(rememberMeCookie());
		return sessionManager;
	}
	//会话验证调度器，每30分钟执行一次验证
	@Bean(name="sessionValidationScheduler")
	public ExecutorServiceSessionValidationScheduler executorServiceSessionValidationScheduler() {
		ExecutorServiceSessionValidationScheduler sessionValidationScheduler=new ExecutorServiceSessionValidationScheduler();
		sessionValidationScheduler.setInterval(1800000);
		sessionValidationScheduler.setSessionManager((ValidatingSessionManager)sessionManager());
		return sessionValidationScheduler;
	}
}
