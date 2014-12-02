package org.covito.cas.client.config;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证转发器
 */
public interface AuthRedirect {

	/**
	 * 转发
	 * @param request
	 * @param response
	 * @param redirectUrl
	 */
	void redirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl)throws IOException;
}
