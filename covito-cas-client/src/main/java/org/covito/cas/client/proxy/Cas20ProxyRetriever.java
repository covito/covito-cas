package org.covito.cas.client.proxy;

import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.covito.cas.client.ssl.HttpURLConnectionFactory;
import org.covito.cas.client.util.Assert;
import org.covito.cas.client.util.LinkUtils;
import org.covito.cas.client.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cas20ProxyRetriever implements ProxyRetriever {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1001940118561145252L;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Url to CAS server.
	 */
	private final String casServerUrl;

	private final String encoding;

	private final HttpURLConnectionFactory urlConnectionFactory;

	public Cas20ProxyRetriever(final String casServerUrl, final String encoding,
			final HttpURLConnectionFactory urlFactory) {
		Assert.assertNotNull( "casServerUrl cannot be null.",casServerUrl);
		this.casServerUrl = casServerUrl;
		this.encoding = encoding;
		this.urlConnectionFactory = urlFactory;
	}

	public String getProxyTicket(final String proxyGrantingTicketId, final String targetService) {
		Assert.assertNotNull( "proxyGrantingTicketId cannot be null.",proxyGrantingTicketId);
		Assert.assertNotNull( "targetService cannot be null.",targetService);

		final URL url = constructUrl(proxyGrantingTicketId, targetService);
		final String response = LinkUtils.getResponseFromServer(url, this.urlConnectionFactory, this.encoding);
		final String error = XmlUtils.getTextForElement(response, "proxyFailure");

		if (StringUtils.isNotEmpty(error)) {
			logger.debug(error);
			return null;
		}

		return XmlUtils.getTextForElement(response, "proxyTicket");
	}

	private URL constructUrl(final String proxyGrantingTicketId, final String targetService) {
		try {
			return new URL(this.casServerUrl + (this.casServerUrl.endsWith("/") ? "" : "/") + "proxy" + "?pgt="
					+ proxyGrantingTicketId + "&targetService=" + URLEncoder.encode(targetService, "UTF-8"));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
