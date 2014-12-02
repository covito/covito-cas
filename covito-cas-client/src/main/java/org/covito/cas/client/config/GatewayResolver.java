package org.covito.cas.client.config;

import javax.servlet.http.HttpServletRequest;

/**
 * Gateway解析器
 *
 */
public interface GatewayResolver {

	/**
	 * Gateway是否已经联通过
	 * @param request
	 * @param serviceUrl
	 * @return
	 */
	boolean hasAlready(HttpServletRequest request, String serviceUrl);

	/**
	 * 保存GateWay信息
	 * @param request
	 * @param serviceUrl
	 */
	String storeGatewayInfo(HttpServletRequest request, String serviceUrl);
}
