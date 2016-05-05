package com.softigent.sftselenium;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class BaseTest  {
	
	protected  Connector connector;

	protected Logger log;
	protected String className;

	protected static BaseTest testClass;
	
	public final Container body;

	public BaseTest(Connector connector) {
		if (connector == null) {
			throw new NullPointerException("Please initialize a default connector.");
		}
		this.connector = connector;
		this.className = getClass().getName();
		this.body = createContainer("body");
		log = Logger.getLogger(className);
	}
	
	public static Connector createConnector(Config config, String driverName) {
		WebDriver driver = SeleniumUtils.getDriver(driverName, config);
		return new Connector(driver, config);
	}

	public Container createContainer(String selector) {
		return new Container(connector.getDriver(), connector.getConfig(), selector);
	}
	
	public Container createContainer(WebElement element) {
		return createContainer(element, createContainer("body"));
	}
	
	public Container createContainer(WebElement element, Container container) {
		return new Container(connector.getDriver(), connector.getConfig(), element);
	}
	
	public Container createContainer(WebElement element, String xpath) {
		return new Container(connector.getDriver(), connector.getConfig(), Container.getParent(element, xpath));
	}
	
	public void waitPageLoad() {
		SeleniumUtils.wait(connector.getDriver(), connector.getConfig().getPageLoadTimeout(), null);
	}

	public void waitPageLoad(String urlPath) {
		SeleniumUtils.wait(connector.getDriver(), connector.getConfig().getPageLoadTimeout(), Pattern.compile(urlPath));
	}
	
	public String getCurrentURL() {
		return connector.getDriver().getCurrentUrl();
	}
	
	public void openPage(String url) {
		log.info("Opening " + url);
		connector.getDriver().get(url);
	}
	
	public void gotoURL(String path) {
		String url = getCurrentURL();
		String host = url.substring(0,  url.indexOf('/', url.indexOf("//") + 2));
		log.info("To URL " + host + path);
		connector.getDriver().navigate().to(host + path);
	}
	
	public void gotoPage(String pageName) {
		String url = getCurrentURL();
		String uri = url.substring(0,  url.lastIndexOf('/'));
		log.info("To Page " + uri + pageName);
		connector.getDriver().navigate().to(uri + pageName);
	}
	
	public static void print(Object message) {
		System.out.println(message);
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