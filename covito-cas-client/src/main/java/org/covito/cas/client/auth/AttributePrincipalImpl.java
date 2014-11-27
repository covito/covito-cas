package org.covito.cas.client.auth;

import java.util.Collections;
import java.util.Map;

import org.covito.cas.client.ProxyRetriever;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributePrincipalImpl extends SimplePrincipal implements AttributePrincipal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<String, Object> attributes;

	/**
	 * 代理授予票据
	 */
	private final String proxyGrantingTicket;

	private final ProxyRetriever proxyRetriever;

	public AttributePrincipalImpl(String name) {
		this(name,Collections.<String, Object>emptyMap());
	}

	public AttributePrincipalImpl(String name, Map<String, Object> attributes) {
		this(name, attributes, null, null);
	}

	public AttributePrincipalImpl(String name, String proxyGrantingTicket, ProxyRetriever proxyRetriever) {
		this(name, Collections.<String, Object> emptyMap(), proxyGrantingTicket, proxyRetriever);
	}

	public AttributePrincipalImpl(String name, Map<String, Object> attributes, String proxyGrantingTicket,
			ProxyRetriever proxyRetriever) {
		super(name);
		this.attributes = attributes;
		this.proxyGrantingTicket = proxyGrantingTicket;
		this.proxyRetriever = proxyRetriever;
		Assert.assertNotNull("attributes cannot be null.", this.attributes);
	}

	@Override
	public String getProxyTicket(String service) {
		if(proxyGrantingTicket!=null&&proxyRetriever!=null){
			return proxyRetriever.getProxyTicket(proxyGrantingTicket, service);
		}
		logger.debug("No ProxyGrantingTicket or No proxyRetriever was supplied, so no Proxy Ticket can be retrieved.");
		return null;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

}
