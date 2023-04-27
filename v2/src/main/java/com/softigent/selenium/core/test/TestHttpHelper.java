package com.softigent.selenium.core.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.mashape.unirest.http.HttpResponse;

/**
 * The abstract class provides access to the static methods in <code>TestHttpClient</code>.
 * Extends in the Test classes
 *
 * @author  dgofman
 * @since   1.0
 */
public abstract class TestHttpHelper {
	
	protected static Logger logger;

	static {
		logger = Logger.initRoot();
	}

	/**
	 * Defined a common request header for the HTTP/HTTPs requests
	 * @return map of the field keys and values
	 * <h3>Example:</h3>
	 * <pre>
	 * @Override
	 * protected Map<String, String> getDefaultHeader() {
	 *		Map<String, String> header = super.getDefaultHeader();
	 *		header.put("X-AUTH-APIKEY", "ABC123");
	 *		return header;
	 * }
	 * </pre>
	 */
	protected Map<String, String> getDefaultHeader() {
		return new HashMap<>();
	}
	
	/**
	 * Create a JSON body passing in the name and assigned value of the node
	 * @return represent JSON as a string
	 * <h3>Example:</h3>
	 * <pre>
	 * createJson(
	 * 		"key1": "val1", 
	 * 		"key2": true, 
	 *      "key3": new String[] {"ENUM1", "ENUM2", "ENUM3" },
	 * 		"key4": node("child1", "val1", "child2", "val2"),
	 *      "key5": new StringBuilder[] {node("child1", "val1"), node("child2", "val2")}
	 * );
	 * </pre>
	 */
	protected String createJson(Object ...keyval) {
		return TestHttpClient.createJson(keyval);
	}
	
	/**
	 * Construct a JSON array from an array values
	 * @return represent JSON as a string
	 * <h3>Example:</h3>
	 * <pre>
	 * createJsonArray(node("child1", "val1"), node("child2", "val2"));
	 * or
	 * createJsonArray(100, 1.0, true, "Hello World!", new String[] {"Hello", "World"});
	 * </pre>
	 */
	protected String createJsonArray(Object ...jsons) {
		return TestHttpClient.createJsonArray(jsons);
	}
	
	/**
	 * Return <code>JsonBody</code> object
	 * @return <code>JsonBody</code> object
	 * <h3>Example:</h3>
	 * <pre>
	 * node("child1", "val1", "child2", "val2");
	 * </pre>
	 */
	protected JNode node(Object ...keyval) {
		return new JNode(keyval);
	}
	
	/**
	 * Log a beginning of Tests.
     * @param msg the message string to be logged
	 */
	protected void testInfo(String msg) {
		logger.info("\n<****************** " + msg + " ******************>");
	}
	
	/**
	 * Log a message at the INFO level.
     * @param msg the message string to be logged
	 */
	protected void info(String msg) {
		logger.info(msg);
	}
	
	/**
	 * Log a message at the DEBUG level.
     * @param msg the message string to be logged
	 */
	protected void debug(String msg) {
		logger.debug(msg);
	}
	
	/**
	 * Log a message at the ERROR level.
     * @param msg the message string to be logged
	 */
	protected void error(String msg) {
		logger.error(msg);
	}
	
	//GET
	/**
	 * HTTP Get request provides result as a JSON object.
     * @param url the request URL
	 */
	protected JObject getJSON(String url) throws Exception {
		return getJSON(url, 200);
	}
	
	/**
	 * HTTP Get request provides result as a JSON object.
     * @param url the request URL
     * @param expected result status code
	 */
	protected JObject getJSON(String url, int status) throws Exception {
		return getJSON(url, getDefaultHeader(), status);
	}
	
	/**
	 * HTTP Get request provides result as a JSON object.
     * @param url the request URL
     * @param the custom header
     * @param expected result status code
	 */
	protected JObject getJSON(String url, Map<String, String> header, int status) throws Exception {
		return new JObject(getString(url, header, status));
	}
	
	/**
	 * HTTP Get request provides result as a JSON array.
     * @param url the request URL
	 */
	protected JArray getArray(String url) throws Exception {
		return getArray(url, 200);
	}
	
	/**
	 * HTTP Get request provides result as a JSON array.
     * @param url the request URL
     * @param expected result status code
	 */
	protected JArray getArray(String url, int status) throws Exception {
		return getArray(url, getDefaultHeader(), status);
	}
	
	/**
	 * HTTP Get request provides result as a JSON array.
     * @param url the request URL
     * @param the custom header
     * @param expected result status code
	 */
	protected JArray getArray(String url, Map<String, String> header, int status) throws Exception {
		return new JArray(getString(url, header, status));
	}
	
	/**
	 * HTTP Get request provides result as a JSON string.
     * @param url the request URL
     * @param the custom header
     * @param expected result status code
	 */
	protected String getString(String url, Map<String, String> header, int status) throws Exception {
		HttpResponse<InputStream> res = TestHttpClient.getRequest(url, header, InputStream.class);
		if (res.getStatus() != status) {
			throw new ResponseException(res, "GET", url);
		}
		return TestHttpClient.responseToString(res);
	}
	
	//POST
	/**
	 * HTTP POST request provides result as a JSON object.
     * @param url the request URL
     * @param body the request body
	 */
	protected JObject postJSON(String url, String body) throws Exception {
		return postJSON(url, body, 200);
	}
	
	/**
	 * HTTP POST request provides result as a JSON object.
     * @param url the request URL
     * @param body the request body
     * @param expected result status code
	 */
	protected JObject postJSON(String url, String body, int status) throws Exception {
		return postJSON(url, getDefaultHeader(), body, status);
	}
	
	/**
	 * HTTP POST request provides result as a JSON object.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected JObject postJSON(String url, Map<String, String> header, String body, int status) throws Exception {
		return new JObject(postString(url, header, body, status));
	}
	
	/**
	 * HTTP POST request provides result as a JSON array.
     * @param url the request URL
     * @param body the request body
	 */
	protected JArray postArray(String url, String body) throws Exception {
		return postArray(url, body, 200);
	}
	
	/**
	 * HTTP POST request provides result as a JSON array.
     * @param url the request URL
     * @param body the request body
     * @param expected result status code
	 */
	protected JArray postArray(String url, String body, int status) throws Exception {
		return postArray(url, getDefaultHeader(), body, status);
	}
	
	/**
	 * HTTP POST request provides result as a JSON array.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected JArray postArray(String url, Map<String, String> header, String body, int status) throws Exception {
		return new JArray(postString(url, header, body, status));
	}
	
	/**
	 * HTTP POST request provides result as a JSON string.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected String postString(String url, Map<String, String> header, String body, int status) throws Exception {
		HttpResponse<InputStream> res = TestHttpClient.postRequest(url, header, body, InputStream.class);
		if (res.getStatus() != status) {
			throw new ResponseException(res, "POST", url);
		}
		return TestHttpClient.responseToString(res);
	}
	
	//PATCH
	/**
	 * HTTP PATCH request provides result as a JSON object.
     * @param url the request URL
     * @param body the request body
	 */
	protected JObject patchJSON(String url, String body) throws Exception {
		return patchJSON(url, body, 200);
	}
	
	/**
	 * HTTP PATCH request provides result as a JSON object.
     * @param url the request URL
     * @param body the request body
     * @param expected result status code
	 */
	protected JObject patchJSON(String url, String body, int status) throws Exception {
		return patchJSON(url, getDefaultHeader(), body, status);
	}
	
	/**
	 * HTTP PATCH request provides result as a JSON object.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected JObject patchJSON(String url, Map<String, String> header, String body, int status) throws Exception {
		return new JObject(patchString(url, header, body, status));
	}
	
	/**
	 * HTTP PATCH request provides result as a JSON array.
     * @param url the request URL
     * @param body the request body
	 */
	protected JArray patchArray(String url, String body) throws Exception {
		return patchArray(url, body, 200);
	}
	
	/**
	 * HTTP PATCH request provides result as a JSON array.
     * @param url the request URL
     * @param body the request body
     * @param expected result status code
	 */
	protected JArray patchArray(String url, String body, int status) throws Exception {
		return patchArray(url, getDefaultHeader(), body, status);
	}
	
	/**
	 * HTTP PATCH request provides result as a JSON array.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected JArray patchArray(String url, Map<String, String> header, String body, int status) throws Exception {
		return new JArray(patchString(url, header, body, status));
	}
	
	/**
	 * HTTP PATCH request provides result as a JSON string.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected String patchString(String url, Map<String, String> header, String body, int status) throws Exception {
		HttpResponse<InputStream> res = TestHttpClient.patchRequest(url, header, body, InputStream.class);
		if (res.getStatus() != status) {
			throw new ResponseException(res, "PATCH", url);
		}
		return TestHttpClient.responseToString(res);
	}
	
	//PUT
	/**
	 * HTTP PUT request provides result as a JSON object.
     * @param url the request URL
     * @param body the request body
	 */
	protected JObject putJSON(String url, String body) throws Exception {
		return putJSON(url, body, 201);
	}
	
	/**
	 * HTTP PUT request provides result as a JSON object.
     * @param url the request URL
     * @param body the request body
     * @param expected result status code
	 */
	protected JObject putJSON(String url, String body, int status) throws Exception {
		return putJSON(url, getDefaultHeader(), body, status);
	}
	
	/**
	 * HTTP PUT request provides result as a JSON object.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected JObject putJSON(String url, Map<String, String> header, String body, int status) throws Exception {
		return new JObject(putString(url, header, body, status));
	}
	
	/**
	 * HTTP PUT request provides result as a JSON array.
     * @param url the request URL
     * @param body the request body
	 */
	protected JArray putArray(String url, String body) throws Exception {
		return putArray(url, body, 201);
	}
	
	/**
	 * HTTP PUT request provides result as a JSON array.
     * @param url the request URL
     * @param body the request body
     * @param expected result status code
	 */
	protected JArray putArray(String url, String body, int status) throws Exception {
		return putArray(url, getDefaultHeader(), body, status);
	}
	
	/**
	 * HTTP PUT request provides result as a JSON array.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected JArray putArray(String url, Map<String, String> header, String body, int status) throws Exception {
		return new JArray(putString(url, header, body, status));
	}
	
	/**
	 * HTTP PUT request provides result as a JSON string.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected String putString(String url, Map<String, String> header, String body, int status) throws Exception {
		HttpResponse<InputStream> res = TestHttpClient.putRequest(url, header, body, InputStream.class);
		if (res.getStatus() != status) {
			throw new ResponseException(res, "PUT", url);
		}
		return TestHttpClient.responseToString(res);
	}
	
	//Options
	/**
	 * HTTP OPTIONS request provides result as a JSON object.
     * @param url the request URL
     * @param body the request body
	 */
	protected JObject optionsJSON(String url, String body) throws Exception {
		return optionsJSON(url, body, 200);
	}
	
	/**
	 * HTTP OPTIONS request provides result as a JSON object.
     * @param url the request URL
     * @param body the request body
     * @param expected result status code
	 */
	protected JObject optionsJSON(String url, String body, int status) throws Exception {
		return optionsJSON(url, getDefaultHeader(), body, status);
	}
	
	/**
	 * HTTP OPTIONS request provides result as a JSON object.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected JObject optionsJSON(String url, Map<String, String> header, String body, int status) throws Exception {
		return new JObject(optionsString(url, header, body, status));
	}
	
	/**
	 * HTTP OPTIONS request provides result as a JSON array.
     * @param url the request URL
     * @param body the request body
	 */
	protected JArray optionsArray(String url, String body) throws Exception {
		return optionsArray(url, body, 200);
	}
	
	/**
	 * HTTP OPTIONS request provides result as a JSON array.
     * @param url the request URL
     * @param body the request body
     * @param expected result status code
	 */
	protected JArray optionsArray(String url, String body, int status) throws Exception {
		return optionsArray(url, getDefaultHeader(), body, status);
	}
	
	/**
	 * HTTP OPTIONS request provides result as a JSON array.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected JArray optionsArray(String url, Map<String, String> header, String body, int status) throws Exception {
		return new JArray(optionsString(url, header, body, status));
	}
	
	/**
	 * HTTP OPTIONS request provides result as a JSON string.
     * @param url the request URL
     * @param the custom header
     * @param body the request body
     * @param expected result status code
	 */
	protected String optionsString(String url, Map<String, String> header, String body, int status) throws Exception {
		HttpResponse<InputStream> res = TestHttpClient.optionsRequest(url, header, body, InputStream.class);
		if (res.getStatus() != status) {
			throw new ResponseException(res, "OPTIONS", url);
		}
		return TestHttpClient.responseToString(res);
	}

	//DELETE
	/**
	 * HTTP DELETE request provides result as a JSON object.
     * @param url the request URL
	 */
	protected JObject deleteJSON(String url) throws Exception {
		return deleteJSON(url, 204);
	}
	
	/**
	 * HTTP DELETE request provides result as a JSON object.
     * @param url the request URL
     * @param expected result status code
	 */
	protected JObject deleteJSON(String url, int status) throws Exception {
		return deleteJSON(url, getDefaultHeader(), status);
	}
	
	/**
	 * HTTP DELETE request provides result as a JSON object.
     * @param url the request URL
     * @param the custom header
     * @param expected result status code
	 */
	protected JObject deleteJSON(String url, Map<String, String> header, int status) throws Exception {
		return new JObject(deleteString(url, header, status));
	}
	
	/**
	 * HTTP DELETE request provides result as a JSON string.
     * @param url the request URL
     * @param the custom header
     * @param expected result status code
	 */
	protected String deleteString(String url, Map<String, String> header, int status) throws Exception {
		HttpResponse<InputStream> res = TestHttpClient.deleteRequest(url, header, InputStream.class);
		if (res.getStatus() != status) {
			throw new ResponseException(res, "DELETE", url);
		}
		return TestHttpClient.responseToString(res);
	}
	
	/**
	 * Sets the system property indicated by the specified key.
     * @param key   the name of the system property.
     * @param value the value of the system property.
	 */
	protected String setProp(String key, String value) {
		return System.setProperty(key, value);
	}
	
	/**
	 * Get the system property.
     * @param key   the name of the system property.
	 */
	protected String getProp(String key) {
		return System.getProperty(key);
	}
	
	/**
	 * Delete the system property.
     * @param key   the name of the system property.
	 */
	protected String delProp(String key) {
		return System.clearProperty(key);
	}
}