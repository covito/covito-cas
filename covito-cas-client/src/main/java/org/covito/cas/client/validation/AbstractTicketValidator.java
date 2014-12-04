package org.covito.cas.client.validation;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.covito.cas.client.Constant;
import org.covito.cas.client.config.ConfigManager;
import org.covito.cas.client.config.TicketValidator;
import org.covito.cas.client.exception.TicketValidationException;
import org.covito.cas.client.ssl.HttpURLConnectionFactory;
import org.covito.cas.client.ssl.HttpsURLConnectionFactory;
import org.covito.cas.client.util.LinkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTicketValidator implements TicketValidator {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 强制验证
	 */
	protected boolean renew;

	/**
	 * 验证票据地址
	 */
	protected String serverUrl;
	
	/**
	 * 编码格式
	 */
	protected String encoding;
	
	/**
	 * Https Url Connection Factory
	 */
	protected HttpURLConnectionFactory urlConnectionFactory = new HttpsURLConnectionFactory();

	protected Map<String, String> customParameters;

	@Override
	public Assertion validate(String ticket, String service) {
		final String validationUrl = constructValidationUrl(ticket, service);
		logger.debug("Constructing validation url: {}", validationUrl);

		try {
			logger.debug("Retrieving response from server.");
			final String serverResponse = retrieveResponseFromServer(new URL(validationUrl), ticket);

			if (serverResponse == null) {
				throw new TicketValidationException("The CAS server returned no response.");
			}

			logger.debug("Server response: {}", serverResponse);

			return parseResponseFromServer(serverResponse);
		} catch (final MalformedURLException e) {
			throw new TicketValidationException(e);
		}
	}

	@Override
	public void initValidator(ConfigManager config) {
		this.serverUrl = config.getConfig(Constant.serverUrl, null);
		this.renew=BooleanUtils.toBoolean(config.getConfig( Constant.renew, "false"));
	}

	/**
	 * 自定义用户参数
	 * @param urlParameters
	 */
	protected void populateUrlAttributeMap(final Map<String, String> urlParameters) {
		// nothing to do
	}

	/**
	 * 构建请求地址和参数
	 * @param ticket
	 * @param serviceUrl
	 * @return
	 */
	protected final String constructValidationUrl(final String ticket, final String serviceUrl) {
		final Map<String, String> urlParameters = new HashMap<String, String>();

		logger.debug("Placing URL parameters in map.");
		urlParameters.put("ticket", ticket);
		urlParameters.put("service", serviceUrl);

		if (this.renew) {
			urlParameters.put("renew", "true");
		}

		logger.debug("Calling template URL attribute map.");
		populateUrlAttributeMap(urlParameters);

		logger.debug("Loading custom parameters from configuration.");
		if (this.customParameters != null) {
			urlParameters.putAll(this.customParameters);
		}

		final String suffix = getUrlSuffix();
		final StringBuilder buffer = new StringBuilder(urlParameters.size() * 10 + this.serverUrl.length()
				+ suffix.length() + 1);

		int i = 0;

		buffer.append(this.serverUrl);
		if (!this.serverUrl.endsWith("/")) {
			buffer.append("/");
		}
		buffer.append(suffix);

		for (Map.Entry<String, String> entry : urlParameters.entrySet()) {
			final String key = entry.getKey();
			final String value = entry.getValue();

			if (value != null) {
				buffer.append(i++ == 0 ? "?" : "&");
				buffer.append(key);
				buffer.append("=");
				final String encodedValue = encodeUrl(value);
				buffer.append(encodedValue);
			}
		}

		return buffer.toString();
	}
	
	/**
	 * 向服务器请求验证票据
	 * @param validationUrl
	 * @param ticket
	 * @return
	 */
	protected String retrieveResponseFromServer(URL validationUrl, String ticket){
		return LinkUtils.getResponseFromServer(validationUrl, urlConnectionFactory , this.encoding);
	}

	/**
	 * URL编码
	 * @param url
	 * @return
	 */
	protected final String encodeUrl(final String url) {
		if (url == null) {
			return null;
		}

		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			return url;
		}
	}
	
	/**
	 * 获取URL后缀
	 * @return
	 */
	protected abstract String getUrlSuffix();

	/**
	 * 解析服务器返回响应
	 * @param response
	 * @return
	 * @throws TicketValidationException
	 */
	protected abstract Assertion parseResponseFromServer(final String response) throws TicketValidationException;
	
}
