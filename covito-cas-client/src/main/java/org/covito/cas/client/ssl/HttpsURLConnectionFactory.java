package org.covito.cas.client.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsURLConnectionFactory implements HttpURLConnectionFactory {

	Logger logger = LoggerFactory.getLogger(getClass());

	private HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

	private Properties sslConfiguration = new Properties();
	
	public HttpsURLConnectionFactory() {
	}

	public HttpsURLConnectionFactory(final Properties config) {
		setSSLConfiguration(config);
	}

	@Override
	public HttpURLConnection buildConnection(URLConnection url) {
		if (url instanceof HttpsURLConnection) {
			final HttpsURLConnection httpsConnection = (HttpsURLConnection) url;
			final SSLSocketFactory socketFactory = this.createSSLSocketFactory();
			if (socketFactory != null) {
				httpsConnection.setSSLSocketFactory(socketFactory);
			}

			if (this.hostnameVerifier != null) {
				httpsConnection.setHostnameVerifier(this.hostnameVerifier);
			}
		}
		return (HttpURLConnection) url;
	}

	/**
	 * Creates a {@link SSLSocketFactory} based on the configuration specified
	 * <p>
	 * Sample properties file:
	 * 
	 * <pre>
	 * protocol=TLS
	 * keyStoreType=JKS
	 * keyStorePath=/var/secure/location/.keystore
	 * keyStorePass=changeit
	 * certificatePassword=aGoodPass
	 * </pre>
	 * 
	 * @return the {@link SSLSocketFactory}
	 */
	private SSLSocketFactory createSSLSocketFactory() {
		InputStream keyStoreIS = null;
		try {
			final SSLContext sslContext = SSLContext.getInstance(this.sslConfiguration.getProperty("protocol", "SSL"));

			if (this.sslConfiguration.getProperty("keyStoreType") != null) {
				final KeyStore keyStore = KeyStore.getInstance(this.sslConfiguration.getProperty("keyStoreType"));
				if (this.sslConfiguration.getProperty("keyStorePath") != null) {
					keyStoreIS = new FileInputStream(this.sslConfiguration.getProperty("keyStorePath"));
					if (this.sslConfiguration.getProperty("keyStorePass") != null) {
						keyStore.load(keyStoreIS, this.sslConfiguration.getProperty("keyStorePass").toCharArray());
						logger.debug("Keystore has {} keys", keyStore.size());
						final KeyManagerFactory keyManager = KeyManagerFactory.getInstance(this.sslConfiguration
								.getProperty("keyManagerType", "SunX509"));
						keyManager.init(keyStore, this.sslConfiguration.getProperty("certificatePassword")
								.toCharArray());
						sslContext.init(keyManager.getKeyManagers(), null, null);
						return sslContext.getSocketFactory();
					}
				}
			}
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				keyStoreIS.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public final void setSSLConfiguration(final Properties config) {
		this.sslConfiguration = config;
	}

	public final void setHostnameVerifier(final HostnameVerifier verifier) {
		this.hostnameVerifier = verifier;
	}

}
