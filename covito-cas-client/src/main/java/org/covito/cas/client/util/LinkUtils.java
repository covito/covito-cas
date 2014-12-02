package org.covito.cas.client.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.covito.cas.client.ssl.HttpURLConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(LinkUtils.class);

	/**
	 * 获取匹配serverName
	 * 
	 * @param request
	 * @param serverName
	 * @return
	 */
	public static String findMatchingServerName(final HttpServletRequest request, final String serverName) {
		final String[] serverNames = serverName.split(" ");

		if (serverNames == null || serverNames.length == 0 || serverNames.length == 1) {
			return serverName;
		}

		final String host = request.getHeader("Host");
		final String xHost = request.getHeader("X-Forwarded-Host");

		final String comparisonHost;
		if (xHost != null && host == "localhost") {
			comparisonHost = xHost;
		} else {
			comparisonHost = host;
		}

		if (comparisonHost == null) {
			return serverName;
		}

		for (final String server : serverNames) {
			final String lowerCaseServer = server.toLowerCase();

			if (lowerCaseServer.contains(comparisonHost)) {
				return server;
			}
		}

		return serverNames[0];
	}

	/**
	 * serverName 是否包含端口
	 * 
	 * @param containsScheme
	 * @param serverName
	 * @return
	 */
	public static boolean serverNameContainsPort(final boolean containsScheme, final String serverName) {
		if (!containsScheme && serverName.contains(":")) {
			return true;
		}

		final int schemeIndex = serverName.indexOf(":");
		final int portIndex = serverName.lastIndexOf(":");
		return schemeIndex != portIndex;
	}

	/**
	 * request端口是否为标准端口
	 * 
	 * @param request
	 * @return
	 */
	public static boolean requestIsOnStandardPort(HttpServletRequest request) {
		final int serverPort = request.getServerPort();
		return serverPort == 80 || serverPort == 443;
	}

	/**
	 * 从request中获取参数
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	public static String getParameter(final HttpServletRequest request, final String name) {
		if ("POST".equals(request.getMethod())) {
			return request.getParameter(name);
		}
		return request.getQueryString() == null || !request.getQueryString().contains(name) ? null : request
				.getParameter(name);
	}

	public static String getResponseFromServer(URL constructedUrl, HttpURLConnectionFactory factory, String encoding) {
		HttpURLConnection conn = null;
		InputStreamReader in = null;
		try {
			conn = factory.buildConnection(constructedUrl.openConnection());
			if (StringUtils.isEmpty(encoding)) {
				in = new InputStreamReader(conn.getInputStream());
			} else {
				in = new InputStreamReader(conn.getInputStream(), encoding);
			}
			final StringBuilder builder = new StringBuilder(255);
			int byteRead;
			while ((byteRead = in.read()) != -1) {
				builder.append((char) byteRead);
			}
			return builder.toString();
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

}
