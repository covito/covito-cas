package org.covito.cas.client;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Constant {
	
	public final static Map<String, String> constantsMap=new HashMap<String, String>();
	static {
		Field[] fs = Constant.class.getDeclaredFields();
		for (Field f : fs) {
			try {
				constantsMap.put(f.getName(), String.valueOf(f.get(Constant.class)));
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 是否启用
	 */
	public static final String enable="enable";
	
	public static final String configFile="configFile";
	
	public static final String configManagerClass="configManagerClass";
	
	public static final String serverName="serverName";
	
	public static final String service="service";
	
	public static final String artifactParameterName="artifactParameterName";
	
	public static final String serviceParameterName="serviceParameterName";
	
	public static final String encodeServiceUrl="encodeServiceUrl";
	
	public static final String renew="renew";
	
	public static final String gateway="gateway";
	
	public static final String serverUrl="serverUrl";
	
	public static final String gatewayResolverClass="gatewayResolverClass";
	
	public static final String authRedirectClass="authRedirectClass";
	
	public static final String urlPattern="urlPattern";
	
	public static final String urlMatcherClass="urlMatcherClass";
	
	public static final String isContains="isContains";
	
	/**======================Validator============================*/
	
	public static final String exceptionOnValidationFailure="exceptionOnValidationFailure";
	
	public static final String redirectAfterValidation="redirectAfterValidation";
	
	public static final String useSession="useSession";
	
	public static final String ticketValidatorClass="ticketValidatorClass";
	
}
