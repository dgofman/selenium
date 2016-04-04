package com.softigent.sftselenium;

import org.openqa.selenium.WebDriver;

public class Connector {
	private WebDriver driver;
	private Config config;
	
	public Connector(WebDriver driver, Config config) {
		this.driver = driver;
		this.config = config;
	}

	public WebDriver getDriver() {
		return driver;
	}

	public Config getConfig() {
		return config;
	}
}