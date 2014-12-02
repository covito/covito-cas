package org.covito.cas.client.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterConfig;

import org.apache.commons.lang3.StringUtils;
import org.covito.cas.client.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfigManager {
	
	protected static Logger logger=LoggerFactory.getLogger(ConfigManager.class);

	protected final Map<String,String> config=new HashMap<String, String>();
	
	protected abstract void initInternal(FilterConfig filterConfig);
	
	public void init(FilterConfig filterConfig){
		Collection<String> paras=Constant.constantsMap.values();
		for(String p:paras){
			String value=getProperty(filterConfig, p,null);
			if(StringUtils.isNotEmpty(value)){
				config.put(p, value);
			}
		}
		initInternal(filterConfig);
	}
	
	/**
	 * 从FilterConfig中获取数
	 * @param filterConfig
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	public static String getProperty(FilterConfig filterConfig, String propertyName, String defaultValue) {
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
	
	/**
	 * 获取参数
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public String getConfig(String name,String defaultValue){
		if(config.get(name)==null){
			return defaultValue;
		}
		return config.get(name);
	}
	
}
