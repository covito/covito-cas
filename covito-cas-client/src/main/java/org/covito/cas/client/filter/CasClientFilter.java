package org.covito.cas.client.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.covito.cas.client.AuthRedirect;
import org.covito.cas.client.GatewayResolver;
import org.covito.cas.client.UrlMatcher;
import org.covito.cas.client.auth.DefaultAuthRedirect;
import org.covito.cas.client.auth.DefaultGatewayResolver;
import org.covito.cas.client.util.LinkUtils;
import org.covito.cas.client.util.ReflectUtils;
import org.covito.cas.client.validation.Assertion;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CasClientFilter implements Filter {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String CONST_CAS_ASSERTION = "_const_cas_assertion_";

	/**
	 * 登录请求跳回参数名称
	 */
	private String serviceParameterName = "service";

	/**
	 * 登录请求票据参数名称
	 */
	private String artifactParameterName = "ticket";

	/**
	 * 是否对url进行编码
	 */
	private boolean encodeServiceUrl = true;

	/**
	 * 认证通过之后跳到访问页面
	 * 网站访问域名如covito.cn多个域名之间有空格格开
	 * 如果访问的域名在serverName没有找到，跳回来的页面使用第一个域名访问
	 */
	private String serverName;

	/**
	 * 认证通过之后，统一跳到固定页面
	 */
	private String service;
	
	/**
	 * 当为true时，强制重新登录
	 * LoginUrl后面带&renew=true
	 */
	private boolean renew = false;
	    
    /**
     * 如果设定这个参数为true，服务端将不会向客户端索要凭据。 
     * LoginUrl后面带&gateway=true
     */
    private boolean gateway = false;

	/**
	 * 登录请求地址
	 */
	private String serverLoginUrl;

	/**
	 * Gateway解析器
	 */
	private GatewayResolver gatewayResolver=new DefaultGatewayResolver();

	/**
	 * 认证转发器
	 */
	private AuthRedirect authRedirect=new DefaultAuthRedirect();
	
	/**
	 * 是否以包含的规则过虑
	 */
	private boolean isContains=false;
	
	/**
	 * url拦截过滤
	 */
	private UrlMatcher urlMatcher;

	protected String getProperty(FilterConfig filterConfig, String propertyName, String defaultValue) {
		final String value = filterConfig.getInitParameter(propertyName);
		if (StringUtils.isNotBlank(value)) {
			logger.debug("Property [{}] loaded from FilterConfig with value [{}]", propertyName, value);
			return value;
		}
		final String value2 = filterConfig.getServletContext().getInitParameter(propertyName);
		if (StringUtils.isNotBlank(value2)) {
			logger.debug("Property [{}] loaded from ServletContext with value [{}]", propertyName, value2);
			return value2;
		}
		logger.debug("Property [{}] not found.  Using default value [{}]", propertyName, defaultValue);
		return defaultValue;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		serverName = getProperty(filterConfig, "serverName", null);
		logger.debug("Loading serverName property: {}", this.serverName);

		service = getProperty(filterConfig, "service", null);
		logger.debug("Loading service property: {}", this.service);

		artifactParameterName = getProperty(filterConfig, "artifactParameterName", artifactParameterName);
		logger.debug("Loading artifact parameter name property: {}", this.artifactParameterName);

		serviceParameterName = getProperty(filterConfig, "serviceParameterName", serviceParameterName);
		logger.debug("Loading serviceParameterName property: {} ", this.serviceParameterName);

		encodeServiceUrl = BooleanUtils.toBoolean(getProperty(filterConfig, "encodeServiceUrl", "true"));
		logger.debug("Loading encodeServiceUrl property: {}", this.encodeServiceUrl);
		
		renew=BooleanUtils.toBoolean(getProperty(filterConfig, "renew", "false"));
        logger.debug("Loaded renew parameter: {}", this.renew);
        
        gateway=BooleanUtils.toBoolean(getProperty(filterConfig, "gateway", "false"));
        logger.debug("Loaded gateway parameter: {}", this.gateway);

		serverLoginUrl = getProperty(filterConfig, "serverLoginUrl", serverLoginUrl);
		logger.debug("Loading serverLoginUrl property: {}", this.serverLoginUrl);

		final String gatewayResolverClass = getProperty(filterConfig, "gatewayResolverClass", null);
		if (StringUtils.isNotEmpty(gatewayResolverClass)) {
			this.gatewayResolver = ReflectUtils.newInstance(gatewayResolverClass);
		}
		logger.debug("Loading gatewayResolverClass property: {}", gatewayResolverClass);

		final String authRedirectClass = getProperty(filterConfig, "authRedirectClass", null);
		if (authRedirectClass != null) {
			this.authRedirect = ReflectUtils.newInstance(authRedirectClass);
		}
		logger.debug("Loading authenticationRedirectStrategyClass property: {}", authRedirectClass);

		final String urlPattern = getProperty(filterConfig, "urlPattern", null);
		logger.debug("Loaded urlPattern parameter: {}", urlPattern);
		
		final String urlMatcherClass = getProperty(filterConfig, "urlMatcherClass", "");
		logger.debug("Loaded urlMatcherClass parameter: {}", urlMatcherClass);

         
		isContains = BooleanUtils.toBoolean(getProperty(filterConfig, "isContains", "false"));
		logger.debug("Loaded isContains parameter: {}", isContains);

		if (StringUtils.isNotEmpty(urlMatcherClass)) {
			this.urlMatcher =ReflectUtils.newInstance(urlMatcherClass);
		}
		
		if (StringUtils.isNotEmpty(urlPattern)) {
			if(this.urlMatcher==null){
				urlMatcher=new RegexUrlMatcher();
			}
			urlMatcher.setPattern(urlPattern.split(";"));
		}
		
		initInternal(filterConfig);

		Assert.assertNotNull("artifactParameterName cannot be null.", this.artifactParameterName);
		Assert.assertNotNull("serviceParameterName cannot be null.", this.serviceParameterName);
		Assert.assertTrue("serverName or service must be set.",StringUtils.isNotEmpty(this.serverName) || StringUtils.isNotEmpty(this.service));
		Assert.assertTrue("serverName and service cannot both be set. ",StringUtils.isBlank(this.serverName) || StringUtils.isBlank(this.service));
		Assert.assertTrue("renew and gateway cannot both be true. ",!this.renew || !this.gateway);
		Assert.assertNotNull("serverLoginUrl cannot be null.",this.serverLoginUrl);
	}

	/**
	 * 自定义参数获取方法(当不想在web.xml配置时重写此方法)
	 * 
	 * @param filterConfig
	 * @throws ServletException
	 */
	protected void initInternal(final FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException,
			ServletException {
		
		final HttpServletRequest request = (HttpServletRequest) servletRequest;
		final HttpServletResponse response = (HttpServletResponse) servletResponse;

		if (isRequestUrlExcluded(request)) {
			logger.debug("Request is ignored.");
			chain.doFilter(request, response);
			return;
		}

		final HttpSession session = request.getSession(false);
		final Assertion assertion = session != null ? (Assertion) session.getAttribute(CONST_CAS_ASSERTION) : null;

		if (assertion == null) {
			final String serviceUrl = constructServiceUrl(request, response);
			
			final String ticket = LinkUtils.getParameter(request, this.artifactParameterName);
			
			final boolean wasGatewayed = this.gateway && this.gatewayResolver.hasAlready(request, serviceUrl);

			if (StringUtils.isBlank(ticket) && !wasGatewayed) {
				final String modifiedServiceUrl;

				logger.debug("no ticket and no assertion found");
				if (this.gateway) {
					logger.debug("setting gateway attribute in session");
					modifiedServiceUrl = this.gatewayResolver.storeGatewayInfo(request, serviceUrl);
				} else {
					modifiedServiceUrl = serviceUrl;
				}

				logger.debug("Constructed service url: {}", modifiedServiceUrl);

				final String urlToRedirectTo = constructRedirectUrl(modifiedServiceUrl);

				logger.debug("redirecting to \"{}\"", urlToRedirectTo);
				this.authRedirect.redirect(request, response, urlToRedirectTo);
				return;
			}
		}
		chain.doFilter(request, response);
		return;
	}

	

	/**
	 * 构建转发路径
	 * @param serverLoginUrl2
	 * @param serviceParameterName2
	 * @param modifiedServiceUrl
	 * @param renew2
	 * @param gateway2
	 * @return
	 */
	private String constructRedirectUrl(String serviceUrl) {
		 try {
			return serverLoginUrl + (serverLoginUrl.contains("?") ? "&" : "?") + serviceParameterName + "="
			            + URLEncoder.encode(serviceUrl, "UTF-8") + (renew ? "&renew=true" : "") + (gateway ? "&gateway=true" : "");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 构建ServiceUrl
	 * @param request
	 * @param response
	 * @return
	 */
	private String constructServiceUrl(HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isNotBlank(service)) {
			return encodeServiceUrl ? response.encodeURL(service) : service;
		}

		final StringBuilder buffer = new StringBuilder();

		final String serverName = LinkUtils.findMatchingServerName(request, this.serverName);

		boolean containsScheme = true;
		if (!serverName.startsWith("https://") && !serverName.startsWith("http://")) {
			buffer.append(request.isSecure() ? "https://" : "http://");
			containsScheme = false;
		}

		buffer.append(serverName);

		if (!LinkUtils.serverNameContainsPort(containsScheme, serverName) && !LinkUtils.requestIsOnStandardPort(request)) {
			buffer.append(":");
			buffer.append(request.getServerPort());
		}

		buffer.append(request.getRequestURI());

		if (StringUtils.isNotBlank(request.getQueryString())) {
			final int location = request.getQueryString().indexOf(artifactParameterName + "=");

			if (location == 0) {
				final String returnValue = encodeServiceUrl ? response.encodeURL(buffer.toString()) : buffer.toString();
				logger.debug("serviceUrl generated: {}", returnValue);
				return returnValue;
			}

			buffer.append("?");

			if (location == -1) {
				buffer.append(request.getQueryString());
			} else if (location > 0) {
				final int actualLocation = request.getQueryString().indexOf("&" + artifactParameterName + "=");

				if (actualLocation == -1) {
					buffer.append(request.getQueryString());
				} else if (actualLocation > 0) {
					buffer.append(request.getQueryString().substring(0, actualLocation));
				}
			}
		}

		final String returnValue = encodeServiceUrl ? response.encodeURL(buffer.toString()) : buffer.toString();
		logger.debug("serviceUrl generated: {}", returnValue);
		return returnValue;
	}

	/**
	 * 路径是否被过滤
	 * @param request
	 * @return
	 */
	private boolean isRequestUrlExcluded(HttpServletRequest request) {
		if (this.urlMatcher == null) {
            return false;
        }
        
        String url = request.getRequestURI();
        url=StringUtils.removeStart(url, request.getContextPath());
        StringBuffer urlBuffer =new StringBuffer(url);
        if (request.getQueryString() != null) {
            urlBuffer.append("?").append(request.getQueryString());
        }
        final String requestUri = urlBuffer.toString();
        if(isContains){
        	return !this.urlMatcher.matches(requestUri);
        }else{
        	return this.urlMatcher.matches(requestUri);
        }
	}

	@Override
	public void destroy() {

	}

}
