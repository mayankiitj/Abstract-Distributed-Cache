package com.unacademy.cache.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RestConnector {

	public HttpEntity postMessages(HttpClient httpClient, String requestBody, Map<String, String> headers,
			String relativeURL) throws Exception {
		if (relativeURL == null)
			throw new Exception();
		HttpPost httpPost = new HttpPost(relativeURL);
		try {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				httpPost.addHeader(header.getKey(), header.getValue());
			}
			HttpEntity httpEntity = new StringEntity(requestBody);
			httpPost.setEntity(httpEntity);
			HttpResponse response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();

			if (!(statusCode == HttpStatus.OK.value() || statusCode == HttpStatus.CREATED.value())) {
				EntityUtils.consume(response.getEntity());
				throw new Exception();
			}
			return response.getEntity();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
	}

	public Map<String, String> getMessage(String requestUrl, HashMap<String, String> requestHeader) {
		HttpURLConnection connection = null;
		try {
			HashMap<String, String> resp = new HashMap<>();
			URL url = new URL(requestUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			Set<String> keyHeader = requestHeader.keySet();
			for (String header : keyHeader) {
				connection.setRequestProperty(header, requestHeader.get(header));
			}

			resp.put("responseCode", Integer.toString(connection.getResponseCode()));

			if (Integer.parseInt(resp.get("responseCode")) != 200) {
				return null;
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			resp.put("responseBody", response.toString());
			return resp;
		} catch (Exception e) {
			return null;
		}
	}

}