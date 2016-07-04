package com.softigent.sftselenium.rest;

public class HostConfig {
	
	private boolean isSecure;
	private String host;
	private String port;
	
	public HostConfig(String host, String port, boolean isSecure) {
		this.host = host;
		this.port = port;
		this.isSecure = isSecure;
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
}
