package com.unacademy.cache.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {
	@Value("${req.timeout}")
	private int reqTimeout;

	@Value("${conn.timeout}")
	private int connectionTimeout;

	@Value("${socket.timeout}")
	private int socketTimeout;

	@Value("${http.conn.pool.size}")
	private int httpConnectionPoolSize;

	@Bean(name = "connectorClient")
	public HttpClient getConnectorClient() {
		RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(reqTimeout)
				.setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeout).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setDefaultMaxPerRoute(httpConnectionPoolSize);
		connManager.setMaxTotal(httpConnectionPoolSize);
		return HttpClients.custom().setDefaultRequestConfig(config).setConnectionManager(connManager).build();
	}
}
