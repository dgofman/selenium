package com.softigent.selenium.core.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import org.json.JSONObject;

import com.mashape.unirest.http.JsonNode;

/**
 * Encapsulates <code>org.json.JSONObject</code> methods
 *
 * @author  dgofman
 * @since   1.0
 */
public class JObject {
	private JSONObject json;
	
	public JObject(JSONObject json) {
		this.json = json;
	}
	
	public JObject(String json) {
		this(new JsonNode(json).getObject());
	}

	public JObject getJObject(String key) throws Exception {
		return new JObject(json.getJSONObject(key));
	}

	public JArray getJArray(String key) throws Exception {
		return new JArray(json.getJSONArray(key));
	}

	public Object getObject(String key) throws Exception {
		return json.get(key);
	}

	public String toString(String key) throws Exception {
		return json.getString(key);
	}
	
	@Override
	public String toString() {
		return json.toString();
	}

	public <E extends Enum<E>> E toEnum(Class<E> clazz, String key) throws Exception {
		return json.getEnum(clazz, key);
	}

	public boolean toBoolean(String key) throws Exception {
		return json.getBoolean(key);
	}

	public BigInteger toBigInteger(String key) throws Exception {
		return json.getBigInteger(key);
	}

	public BigDecimal toBigDecimal(String key) throws Exception {
		return json.getBigDecimal(key);
	}

	public double toDouble(String key) throws Exception {
		return json.getDouble(key);
	}

	public int toInt(String key) throws Exception {
		return json.getInt(key);
	}

	public long toLong(String key) throws Exception {
		return json.getLong(key);
	}
	
	public boolean has(String key) {
		return json.has(key);
	}
	
	public boolean isNull(String key) {
		return json.isNull(key);
	}
	
	public boolean isNull() {
		return json == null || json == JSONObject.NULL || JSONObject.NULL.equals(json);
	}
	
	public int length() {
		return json.length();
	}

	public Iterator<String> keys() {
		return json.keys();
	}
	
	public JSONObject getObject() {
		return json;
	}
}