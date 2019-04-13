package com.softigent.sftselenium.rest;

import java.util.HashMap;
import java.util.Map;

public class HostConfig {
	
	private boolean isSecure;
	private String host;
	private String port;
	
	private Map<String, String> headers = new HashMap<String, String>();
	
	public HostConfig(String host, String port, boolean isSecure) {
		this(host, port, isSecure, new HashMap<String, String>());
	}
	
	public HostConfig(String host, String port, boolean isSecure, Map<String, String> headers) {
		this.host = host;
		this.port = port;
		this.isSecure = isSecure;
		this.headers = headers;
	}
	
	public boolean isSecure() {
		return isSecure;
	}

	public String getHost() {
		return host;
	}
	
	public String getPort() {
		return port;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
}
