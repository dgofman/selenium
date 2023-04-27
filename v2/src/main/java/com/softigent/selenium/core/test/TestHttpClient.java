package com.softigent.selenium.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

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
import org.json.JSONArray;
import org.json.JSONObject;

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
import com.mashape.unirest.request.body.RequestBodyEntity;

/**
 * Lightweight HTTP client library using <code>com.mashape.unirest.http.Unirest</code>
 *
 * @author  dgofman
 * @since   1.0
 */
public class TestHttpClient {

	private static final String CONTENT_TYPE = "content-type";
	private static final String ACCEPT_ENCODING_HEADER = "accept-encoding";
	private static final String USER_AGENT_HEADER = "user-agent";
	private static final String USER_AGENT = "reset-service/1.0.0";
	
	public static final int connectionTimeout = 3000; //The timeout until a connection with the server is established (in milliseconds).
	public static final int socketTimeout  = 60000; // The timeout to receive data (in milliseconds).
	
	protected static Logger logger;
	
	public static boolean printRequest = true;
	public static boolean printResult = true;

	static {
		logger = Logger.initRoot();
	}
	
	public static void resetTimeout() {
		setTimeout(connectionTimeout, socketTimeout);
	}
	
	public static void setTimeout(int socketTimeout) {
		Unirest.setTimeouts(connectionTimeout, socketTimeout);
	}
	
	public static void setTimeout(int connectionTimeout, int socketTimeout) {
		Unirest.setTimeouts(connectionTimeout, socketTimeout);
	}
	
	public static void defaultSSL() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		Unirest.setHttpClient(httpclient);
	}

	public static HttpResponse<JsonNode> getRequest(String url) throws UnirestException {
		return getRequest(url, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> getRequest(String url, Class<T> responseClass) throws UnirestException {
		return getRequest(url, new HashMap<>(), responseClass);
	}
	
	public static <T> HttpResponse<T> getRequest(String url, Map<String, String> headers, Class<T> responseClass) throws UnirestException {
		logger.info("GET " + url);
		return call(Unirest.get(url).headers(headers), responseClass);
	}

	public static HttpResponse<JsonNode> optionsRequest(String url, JsonNode body) throws UnirestException {
		return optionsRequest(url, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> optionsRequest(String url, T body, Class<T> responseClass) throws UnirestException {
		return optionsRequest(responseClass, url, new HashMap<>(), body);
	}
	
	public static <T> HttpResponse<T> optionsRequest(Class<T> responseClass, String url, Map<String, String> headers, Object body) throws UnirestException {
		return optionsRequest(url, headers, JSONObject.valueToString(body), responseClass);
	}

	public static HttpResponse<JsonNode> optionsRequest(String url, String body) throws UnirestException {
		return optionsRequest(url, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> optionsRequest(String url, String body, Class<T> responseClass) throws UnirestException {
		return optionsRequest(url, new HashMap<>(), body, responseClass);
	}
	
	public static <T> HttpResponse<T> optionsRequest(String url, Map<String, String> headers, String body, Class<T> responseClass) throws UnirestException {
		logger.info("OPTIONS " + url);
		return call(Unirest.options(url).headers(headers).body(body), responseClass);
	}
	
	public static HttpResponse<JsonNode> postRequest(String url, JsonNode body) throws UnirestException {
		return postRequest(url, body, JsonNode.class);
	}

	public static <T> HttpResponse<T> postRequest(String url, T body, Class<T> responseClass) throws UnirestException {
		return postRequest(responseClass, url, new HashMap<>(), body);
	}
	
	public static <T> HttpResponse<T> postRequest(Class<T> responseClass, String url, Map<String, String> headers, Object body) throws UnirestException {
		return postRequest(url, headers, JSONObject.valueToString(body), responseClass);
	}
	
	public static HttpResponse<JsonNode> postRequest(String url, String body) throws UnirestException {
		return postRequest(url, body, JsonNode.class);
	}

	public static <T> HttpResponse<T> postRequest(String url, String body, Class<T> responseClass) throws UnirestException {
		return postRequest(url, new HashMap<>(), body, responseClass);
	}
	
	public static <T> HttpResponse<T> postRequest(String url, Map<String, String> headers, String body, Class<T> responseClass) throws UnirestException {
		logger.info("POST " + url);
		return call(Unirest.post(url).headers(headers).body(body), responseClass);
	}

	public static HttpResponse<JsonNode> putRequest(String url, JsonNode body) throws UnirestException {
		return putRequest(url, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> putRequest(String url, T body, Class<T> responseClass) throws UnirestException {
		return putRequest(responseClass, url, new HashMap<>(), body);
	}
	
	public static <T> HttpResponse<T> putRequest(Class<T> responseClass, String url, Map<String, String> headers, Object body) throws UnirestException {
		return putRequest(url, headers, JSONObject.valueToString(body), responseClass);
	}

	public static HttpResponse<JsonNode> putRequest(String url, String body) throws UnirestException {
		return putRequest(url, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> putRequest(String url, String body, Class<T> responseClass) throws UnirestException {
		return putRequest(url, new HashMap<>(), body, responseClass);
	}
	
	public static <T> HttpResponse<T> putRequest(String url, Map<String, String> headers, String body, Class<T> responseClass) throws UnirestException {
		logger.info("PUT " + url);
		return call(Unirest.put(url).headers(headers).body(body), responseClass);
	}

	public static HttpResponse<JsonNode> patchRequest(String url, JsonNode body) throws UnirestException {
		return patchRequest(url, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> patchRequest(String url, T body, Class<T> responseClass) throws UnirestException {
		return patchRequest(responseClass, url, new HashMap<>(), body);
	}
	
	public static <T> HttpResponse<T> patchRequest(Class<T> responseClass, String url, Map<String, String> headers, Object body) throws UnirestException {
		return patchRequest(url, headers, JSONObject.valueToString(body), responseClass);
	}

	public static HttpResponse<JsonNode> patchRequest(String url, String body) throws UnirestException {
		return patchRequest(url, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> patchRequest(String url, String body, Class<T> responseClass) throws UnirestException {
		return patchRequest(url, new HashMap<>(), body, responseClass);
	}
	
	public static <T> HttpResponse<T> patchRequest(String url, Map<String, String> headers, String body, Class<T> responseClass) throws UnirestException {
		logger.info("PATCH " + url);
		return call(Unirest.patch(url).headers(headers).body(body), responseClass);
	}

	public static HttpResponse<JsonNode> deleteRequest(String url, JsonNode body) throws UnirestException {
		return deleteRequest(url, body, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> deleteRequest(String url, T body, Class<T> responseClass) throws UnirestException {
		return deleteRequest(responseClass, url, new HashMap<>(), body);
	}
	
	public static <T> HttpResponse<T> deleteRequest(Class<T> responseClass, String url, Map<String, String> headers, Object body) throws UnirestException {
		return deleteRequest(url, headers, JSONObject.valueToString(body), responseClass);
	}
	
	public static <T> HttpResponse<T> deleteRequest(String url, Map<String, String> headers,  String body, Class<T> responseClass) throws UnirestException {
		logger.info("DELETE " + url);
		return call(Unirest.delete(url).headers(headers).body(body), responseClass);
	}
	
	public static HttpResponse<JsonNode> deleteRequest(String url, String body) throws UnirestException {
		return deleteRequest(url, body, JsonNode.class);
	}

	public static <T> HttpResponse<T> deleteRequest(String url, String body, Class<T> responseClass) throws UnirestException {
		logger.info("DELETE " + url);
		return call(Unirest.delete(url).headers(new HashMap<>()).body(body), responseClass);
	}
	
	public static HttpResponse<JsonNode> deleteRequest(String url) throws UnirestException {
		return deleteRequest(url, JsonNode.class);
	}
	
	public static <T> HttpResponse<T> deleteRequest(String url,  Class<T> responseClass) throws UnirestException {
		return deleteRequest(url, new HashMap<>(), responseClass);
	}
	
	public static <T> HttpResponse<T> deleteRequest(String url, Map<String, String> headers,  Class<T> responseClass) throws UnirestException {
		logger.info("DELETE " + url);
		return call(Unirest.delete(url).headers(headers), responseClass);
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
		HttpClient client = ClientFactory.getHttpClient();
		org.apache.http.HttpResponse response;
		try {
			response = client.execute(requestObj);
			HttpResponse<T> httpResponse = new HttpResponse<T>(response, responseClass);
			logger.info("Status code: " + httpResponse.getStatus());
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
			if (request.getBody() instanceof RequestBodyEntity && 
				((RequestBodyEntity) request.getBody()).getBody() != null) {
				HttpEntity entity = request.getBody().getEntity();
				((HttpEntityEnclosingRequestBase) reqObj).setEntity(entity);
			}
		}

		return reqObj;
	}
	
	public static String responseToString(HttpResponse<InputStream> res) throws IOException {
		if (res.getBody() == null) {
			return null;
		}
		String str =  new String(ResponseUtils.getBytes(res.getBody()), "UTF-8");
		if (printResult) {
			logger.info("Result: " + str);
		}
		return str;
	}
	
	//Example: createJsonArray(addJson1, addJson2, addJson3);
	public static String createJsonArray(Object ...jsons) {
		Object[] jsonArray = new Object[jsons.length];
		for (int i = 0; i < jsons.length; i++) {
			jsonArray[i] = getVal(jsons[i], "");
		}
		String json = "[" +  Arrays.stream(jsonArray).map(o -> String.valueOf(o)).collect(Collectors.joining(",")) + "]";
		if (printRequest) {
			logger.info("JSON: " + json);
		}
		return json;
	}
		
	//Example: createJson("key1": "val1", "key2": true, "key3": new JSONArray(new String[] {"1", "2", "3"}));
	public static String createJson(Object ...keyval) {
		return createJsonBody(keyval).toString();
	}
	
	public static JNode createJsonBody(Object ...keyval) {
		return new JNode(keyval);
	}
	
	public static String getVal(Object val, String indent) {
		if (val instanceof String || val instanceof Enum) {
			return "\"" + String.valueOf(val) + "\"";
		} else if (val instanceof Object[]) {
			Object[] values = (Object[]) val;
			Object[] newValues = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				Object e = values[i];
				if (e instanceof JNode) {
					newValues[i] = ((JNode) e).toJson(indent);
				} else if (e instanceof String || e instanceof JNode || e instanceof Enum || e instanceof StringBuilder) {
					newValues[i] = String.valueOf(e);
				} else {
					newValues[i] = e;
				}
			}
			if (val instanceof StringBuilder[] || val instanceof JNode[]) {
				return  "[\n" +  Arrays.stream(newValues).map(o -> String.valueOf(o)).collect(Collectors.joining(",")) + "]";
			}
			return new JSONArray(newValues).toString();
		} else if (val instanceof JNode) {
			return "\n" + ((JNode) val).toJson(indent);
		} else {
			return String.valueOf(val);
		}
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