package org.covito.cas.client.ssl;

import java.net.HttpURLConnection;
import java.net.URLConnection;

public interface HttpURLConnectionFactory {

	/**
	 * 创建连接
	 * @param url
	 * @return
	 */
	HttpURLConnection buildConnection(final URLConnection url);
}
