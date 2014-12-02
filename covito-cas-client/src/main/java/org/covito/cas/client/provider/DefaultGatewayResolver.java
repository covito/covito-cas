package org.covito.cas.client.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.covito.cas.client.config.GatewayResolver;

public class DefaultGatewayResolver implements GatewayResolver {

	public static final String CONST_CAS_GATEWAY = "_const_cas_gateway_";

	@Override
	public boolean hasAlready(HttpServletRequest request, String serviceUrl) {
		final HttpSession session = request.getSession(false);

		if (session == null) {
			return false;
		}

		final boolean result = session.getAttribute(CONST_CAS_GATEWAY) != null;
		session.removeAttribute(CONST_CAS_GATEWAY);
		return result;
	}

	@Override
	public String storeGatewayInfo(HttpServletRequest request, String serviceUrl) {
		request.getSession(true).setAttribute(CONST_CAS_GATEWAY, "yes");
		return serviceUrl;
	}

}
