package org.covito.cas.client.proxy;

import java.io.Serializable;

/**
 * 代理猎犬
 */
public interface ProxyRetriever extends Serializable {

	/**
	 * 获取代理票据ID
	 * @param proxyTicketId
	 * @param targetService
	 * @return
	 */
	String getProxyTicket(String proxyTicketId, String targetService);
}
