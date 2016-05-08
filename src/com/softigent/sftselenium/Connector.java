package com.softigent.sftselenium;

import org.openqa.selenium.WebDriver;

public class Connector {
	private WebDriver driver;
	private Config config;
	
	public static Connector instance;
	
	public Connector(WebDriver driver, Config config) {
		this.driver = driver;
		this.config = config;
		if (Connector.instance == null) {
			Connector.instance = this;
		}
	}

	public WebDriver getDriver() {
		return driver;
	}

	public Config getConfig() {
		return config;
	}
}