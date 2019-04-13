package com.softigent.sftselenium.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;

import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Option;
import com.mashape.unirest.http.options.Options;
import com.mashape.unirest.http.utils.ClientFactory;
import com.mashape.unirest.http.utils.ResponseUtils;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.HttpRequest;
import com.softigent.sftselenium.CacheLogger;
import com.softigent.sftselenium.Config;

public class Client {

	private static Logger logger = CacheLogger.getLogger(Client.class);
	
	private static final String CONTENT_TYPE = "content-type";
	private static final String ACCEPT_ENCODING_HEADER = "accept-encoding";
	private static final String USER_AGENT_HEADER = "user-agent";
	private static final String USER_AGENT = "reset-service/1.0.0";
	
	public static void defaultSSL() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		Unirest.setHttpClient(httpclient);
	}

	public static HttpResponse<JsonNode> getRequest(HostConfig config, String path) throws UnirestException {
		return getRequest(config, path, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> getRequest(HostConfig config, String path, Class<T> responseClass) throws UnirestException {
		return getRequest(getURL(config, path), config.getHeaders(), responseClass);
	}
	
	public static <T> HttpResponse<T> getRequest(String url, Map<String, String> headers, Class<T> responseClass) throws UnirestException {
		logger.info("GET " + url);
		return call(Unirest.get(url).headers(headers), responseClass);
	}

	public static HttpResponse<JsonNode> optionsRequest(HostConfig config, String path, JsonNode body) throws UnirestException {
		return optionsRequest(config, path, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> optionsRequest(HostConfig config, String path, T body, Class<T> responseClass) throws UnirestException {
		return optionsRequest(responseClass, getURL(config, path), config.getHeaders(), body);
	}
	
	public static <T> HttpResponse<T> optionsRequest(Class<T> responseClass, String url, Map<String, String> headers, T body) throws UnirestException {
		logger.info("OPTIONS " + url);
		return call(Unirest.options(url).headers(headers).body(body), responseClass);
	}

	public static HttpResponse<JsonNode> optionsRequest(HostConfig config, String path, String body) throws UnirestException {
		return optionsRequest(config, path, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> optionsRequest(HostConfig config, String path, String body, Class<T> responseClass) throws UnirestException {
		return optionsRequest(getURL(config, path), config.getHeaders(), body, responseClass);
	}
	
	public static <T> HttpResponse<T> optionsRequest(String url, Map<String, String> headers, String body, Class<T> responseClass) throws UnirestException {
		logger.info("OPTIONS " + url);
		return call(Unirest.options(url).headers(headers).body(body), responseClass);
	}
	
	public static HttpResponse<JsonNode> postRequest(HostConfig config, String path, JsonNode body) throws UnirestException {
		return postRequest(config, path, body, JsonNode.class);
	}

	public static <T> HttpResponse<T> postRequest(HostConfig config, String path, T body, Class<T> responseClass) throws UnirestException {
		return postRequest(responseClass, getURL(config, path), config.getHeaders(), body);
	}
	
	public static <T> HttpResponse<T> postRequest(Class<T> responseClass, String url, Map<String, String> headers, T body) throws UnirestException {
		logger.info("POST " + url);
		return call(Unirest.post(url).headers(headers).body(body), responseClass);
	}
	
	public static HttpResponse<JsonNode> postRequest(HostConfig config, String path, String body) throws UnirestException {
		return postRequest(config, path, body, JsonNode.class);
	}

	public static <T> HttpResponse<T> postRequest(HostConfig config, String path, String body, Class<T> responseClass) throws UnirestException {
		return postRequest(getURL(config, path), config.getHeaders(), body, responseClass);
	}
	
	public static <T> HttpResponse<T> postRequest(String url, Map<String, String> headers, String body, Class<T> responseClass) throws UnirestException {
		logger.info("POST " + url);
		return call(Unirest.post(url).headers(headers).body(body), responseClass);
	}

	public static HttpResponse<JsonNode> putRequest(HostConfig config, String path, JsonNode body) throws UnirestException {
		return putRequest(config, path, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> putRequest(HostConfig config, String path, T body, Class<T> responseClass) throws UnirestException {
		return putRequest(responseClass, getURL(config, path), config.getHeaders(), body);
	}
	
	public static <T> HttpResponse<T> putRequest(Class<T> responseClass, String url, Map<String, String> headers, T body) throws UnirestException {
		logger.info("PUT " + url);
		return call(Unirest.put(url).headers(headers).body(body), responseClass);
	}

	public static HttpResponse<JsonNode> putRequest(HostConfig config, String path, String body) throws UnirestException {
		return putRequest(config, path, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> putRequest(HostConfig config, String path, String body, Class<T> responseClass) throws UnirestException {
		return putRequest(getURL(config, path), config.getHeaders(), body, responseClass);
	}
	
	public static <T> HttpResponse<T> putRequest(String url, Map<String, String> headers, String body, Class<T> responseClass) throws UnirestException {
		logger.info("PUT " + url);
		return call(Unirest.put(url).headers(headers).body(body), responseClass);
	}

	public static HttpResponse<JsonNode> patchRequest(HostConfig config, String path, JsonNode body) throws UnirestException {
		return patchRequest(config, path, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> patchRequest(HostConfig config, String path, T body, Class<T> responseClass) throws UnirestException {
		return patchRequest(responseClass, getURL(config, path), config.getHeaders(), body);
	}
	
	public static <T> HttpResponse<T> patchRequest(Class<T> responseClass, String url, Map<String, String> headers, T body) throws UnirestException {
		logger.info("PATCH " + url);
		return call(Unirest.patch(url).headers(headers).body(body), responseClass);
	}

	public static HttpResponse<JsonNode> patchRequest(HostConfig config, String path, String body) throws UnirestException {
		return patchRequest(config, path, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> patchRequest(HostConfig config, String path, String body, Class<T> responseClass) throws UnirestException {
		return patchRequest(getURL(config, path), config.getHeaders(), body, responseClass);
	}
	
	public static <T> HttpResponse<T> patchRequest(String url, Map<String, String> headers, String body, Class<T> responseClass) throws UnirestException {
		logger.info("PATCH " + url);
		return call(Unirest.patch(url).headers(headers).body(body), responseClass);
	}

	public static HttpResponse<JsonNode> deleteRequest(HostConfig config, String path, JsonNode body) throws UnirestException {
		return deleteRequest(config, path, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> deleteRequest(HostConfig config, String path, T body, Class<T> responseClass) throws UnirestException {
		return deleteRequest(responseClass, getURL(config, path), config.getHeaders(), body);
	}
	
	public static <T> HttpResponse<T> deleteRequest(Class<T> responseClass, String url, Map<String, String> headers, T body) throws UnirestException {
		logger.info("DELETE " + url);
		return call(Unirest.delete(url).headers(headers).body(body), responseClass);
	}
	
	public static HttpResponse<JsonNode> deleteRequest(HostConfig config, String path, String body) throws UnirestException {
		return deleteRequest(config, path, body, JsonNode.class);
	}

	public static <T> HttpResponse<T> deleteRequest(HostConfig config, String path, String body, Class<T> responseClass) throws UnirestException {
		String url = getURL(config, path);
		logger.info("DELETE " + url);
		return call(Unirest.delete(url).headers(config.getHeaders()).body(body), responseClass);
	}
	
	public static HttpResponse<JsonNode> deleteRequest(HostConfig config, String path) throws UnirestException {
		return deleteRequest(config, path, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> deleteRequest(HostConfig config, String path,  Class<T> responseClass) throws UnirestException {
		return deleteRequest(getURL(config, path), config.getHeaders(), responseClass);
	}
	
	public static <T> HttpResponse<T> deleteRequest(String url, Map<String, String> headers,  Class<T> responseClass) throws UnirestException {
		logger.info("DELETE " + url);
		return call(Unirest.delete(url).headers(headers), responseClass);
	}

	public static String getURL(HostConfig config, String path) {
		return (config.isSecure() ? "https://" : "http://") + config.getHost() + ":" + config.getPort() + path;
	}
	
	public static String readJsonFile(String path) throws IOException {
		String absPath = Config.getAbsolutePath(path);
		logger.info("File: " + absPath);
		return new String(Files.readAllBytes(Paths.get(absPath)), StandardCharsets.UTF_8);
	}
	
	public static JsonElement getJsonElement(String str) {
		return new Gson().fromJson(str, JsonElement.class);
	}

	public static String replaceValue(String parent, Object key, Object value) throws IllegalStateException {
		return parent.replaceAll("\\{" + String.valueOf(key)  + "\\}", String.valueOf(value));
	}

	public static JsonObject getJsonObject(Object node) {
		return ((JsonObject)node).getAsJsonObject();
	}

	public static JsonObject getJsonObject(Object parent, String path)  throws IllegalStateException {
		return getJsonObject(((JsonElement)parent).getAsJsonObject(), path);
	}

	public static JsonArray getJsonArray(Object node) {
		return ((JsonElement)node).getAsJsonArray();
	}

	public static JsonArray getJsonArray(Object parent, String path)  throws IllegalStateException {
		return getJsonArray(((JsonElement)parent).getAsJsonObject(), path);
	}

	private static <T> HttpResponse<T> call(BaseRequest rest, Class<T> responseClass) throws UnirestException {
		HttpRequest request = rest.getHttpRequest();
		request.header("correlation-id", getCorrelationId());
		HttpRequestBase requestObj = prepareRequest(request);
		HttpClient client = ClientFactory.getHttpClient(); // The
		org.apache.http.HttpResponse response;
		try {
			response = client.execute(requestObj);
			HttpResponse<T> httpResponse = new HttpResponse<T>(response, responseClass);
			requestObj.releaseConnection();
			return httpResponse;
		} catch (Exception e) {
			throw new UnirestException(e);
		} finally {
			requestObj.releaseConnection();
		}
	}

	private static String getCorrelationId() {
		return String.valueOf(new Date().getTime());
	}
	
	private static HttpRequestBase prepareRequest(HttpRequest request) {

		Object defaultHeaders = Options.getOption(Option.DEFAULT_HEADERS);
		if (defaultHeaders != null) {
			@SuppressWarnings("unchecked")
			Set<Entry<String, String>> entrySet = ((Map<String, String>) defaultHeaders).entrySet();
			for (Entry<String, String> entry : entrySet) {
				request.header(entry.getKey(), entry.getValue());
			}
		}

		if (!request.getHeaders().containsKey(USER_AGENT_HEADER)) {
			request.header(USER_AGENT_HEADER, USER_AGENT);
		}
		if (!request.getHeaders().containsKey(ACCEPT_ENCODING_HEADER)) {
			request.header(ACCEPT_ENCODING_HEADER, "gzip");
		}
		if (!request.getHeaders().containsKey(CONTENT_TYPE)) {
			request.header(CONTENT_TYPE, "application/json");
		}

		HttpRequestBase reqObj = null;

		String urlToRequest = null;
		try {
			URL url = new URL(request.getUrl());
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), URLDecoder.decode(url.getPath(), "UTF-8"), "", url.getRef());
			urlToRequest = uri.toURL().toString();
			if (url.getQuery() != null && !url.getQuery().trim().equals("")) {
				if (!urlToRequest.substring(urlToRequest.length() - 1).equals("?")) {
					urlToRequest += "?";
				}
				urlToRequest += url.getQuery();
			} else if (urlToRequest.substring(urlToRequest.length() - 1).equals("?")) {
				urlToRequest = urlToRequest.substring(0, urlToRequest.length() - 1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		switch (request.getHttpMethod()) {
		case GET:
			reqObj = new HttpGet(urlToRequest);
			break;
		case POST:
			reqObj = new HttpPost(urlToRequest);
			break;
		case PUT:
			reqObj = new HttpPut(urlToRequest);
			break;
		case DELETE:
			reqObj = new HttpWithBody("DELETE", urlToRequest);
			break;
		case PATCH:
			reqObj = new HttpWithBody("PATCH", urlToRequest);
			break;
		case OPTIONS:
			reqObj = new HttpWithBody("OPTIONS", urlToRequest);
			break;
		case HEAD:
			reqObj = new HttpHead(urlToRequest);
			break;
		}

		Set<Entry<String, List<String>>> entrySet = request.getHeaders().entrySet();
		for (Entry<String, List<String>> entry : entrySet) {
			List<String> values = entry.getValue();
			if (values != null) {
				for (String value : values) {
					reqObj.addHeader(entry.getKey(), value);
				}
			}
		}

		// Set body
		if (!(request.getHttpMethod() == HttpMethod.GET || request.getHttpMethod() == HttpMethod.HEAD)) {
			if (request.getBody() != null) {
				HttpEntity entity = request.getBody().getEntity();
				((HttpEntityEnclosingRequestBase) reqObj).setEntity(entity);
			}
		}

		return reqObj;
	}
	
	public static String responseToString(HttpResponse<InputStream> res) throws IOException {
		return new String(ResponseUtils.getBytes(res.getBody()), "UTF-8");
	}
}

class HttpWithBody extends HttpEntityEnclosingRequestBase {

	private String method;
	
	public HttpWithBody() {
		super();
	}
	
	public HttpWithBody(final String uri) {
		super();
		setURI(URI.create(uri));
	}

	public HttpWithBody(final URI uri) {
		super();
		setURI(uri);
	}

	public HttpWithBody(String method, final String uri) {
		this(uri);
		this.method = method;
	}

	public String getMethod() {
		return method;
	}
}