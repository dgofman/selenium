package com.softigent.sftselenium;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

public abstract class BaseTest  {
	
	protected  Connector connector;

	protected Logger log;
	protected String className;

	protected static BaseTest testClass;

	public BaseTest(Connector connector) {
		if (connector == null) {
			throw new NullPointerException("Please initialize a default connector.");
		}
		this.connector = connector;
		this.className = getClass().getName();
		log = Logger.getLogger(className);
	}
	
	public static Connector createConnector(Config config, String driverName) {
		WebDriver driver = SeleniumUtils.getDriver(driverName, true);
		return new Connector(driver, config);
	}

	public static Connector createConnector(Config config, String driverName, boolean isFullScreen) {
		WebDriver driver = SeleniumUtils.getDriver(driverName, isFullScreen);
		return new Connector(driver, config);
	}
	
	public Container createContainer(String selector) {
		return new Container(connector.getDriver(), connector.getConfig(), selector);
	}
	
	public void openPage(String url) {
		log.info("Opening " + url);
		connector.getDriver().get(url);
	}
	
	public void setUp() throws Exception {
		log.info(">>>>> Start test: " + className);
		testClass = this;
	}
	
	public void tearDown(boolean isClose) throws Exception {
		log.info("<<<<< Finish test: " + className);
		if (isClose) {
			connector.getDriver().quit();
		}
	}
}