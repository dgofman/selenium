package com.softigent.selenium.core.test;

import org.json.JSONArray;

import com.mashape.unirest.http.JsonNode;

/**
 * Encapsulates <code>org.json.JSONArray</code> methods
 *
 * @author  dgofman
 * @since   1.0
 */
public class JArray {
	private JSONArray json;
	
	public JArray(JSONArray json) {
		this.json = json;
	}
	
	public JArray(String json) {
		this(new JsonNode(json).getArray());
	}
	
	public Object get(int index) throws Exception {
		return json.get(index);
	}
	
	public JObject getJObject(int index) throws Exception {
		return new JObject(json.getJSONObject(index));
	}

	public int length() {
		return json.length();
	}
	
	public JSONArray getObject() {
		return json;
	}
}