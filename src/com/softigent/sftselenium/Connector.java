package com.softigent.sftselenium;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

public class Connector {
	private WebDriver driver;
	private boolean isHeadless;
	private Config config;
	private String driverName;
	
	public static Connector instance;
	
	protected Logger log = CacheLogger.getLogger(Connector.class.getName());
	
	public Connector(String driverName, boolean headless, Config config) {
		this.driverName = driverName;
		this.isHeadless = headless;
		this.config = config;
		if (Connector.instance == null) {
			Connector.instance = this;
		}
	}
	
	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}
	
	public WebDriver getDriver() {
		return driver;
	}
	
	public void closeDriver() {
		if (driver != null) {
			log.debug("Close Driver");
			driver.close();
			log.debug("Quit Driver");
			try { driver.quit();
			} catch (Exception e) {}
			driver = null;
		}
	}

	public Config getConfig() {
		return config;
	}

	public String getDriverName() {
		return driverName;
	}
	
	public boolean isHeadless() {
		return isHeadless;
	}
}