package org.covito.cas.client.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.FilterConfig;

import org.apache.commons.lang3.StringUtils;
import org.covito.cas.client.Constant;
import org.covito.cas.client.config.ConfigManager;

public class PropertiesConfigManager extends ConfigManager {
	
	protected String configFile="/cas-client.properties";

	@Override
	protected void initInternal(FilterConfig filterConfig) {
		configFile=getProperty(filterConfig, Constant.configFile, configFile);
		Properties p=new Properties();
		InputStream in=getClass().getResourceAsStream(configFile);
		try {
			p.load(in);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		Collection<String> paras=Constant.constantsMap.values();
		for(String pa:paras){
			String value=p.getProperty(pa);
			if(StringUtils.isNotEmpty(value)){
				config.put(pa, value);
			}
		}
	}

}
