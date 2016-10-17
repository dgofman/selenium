package com.softigent.sftselenium;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Container extends Element {

	protected WebElement iframeElement;
	
	static Logger log = CacheLogger.getLogger(Container.class.getName());

	public Container(WebDriver driver, Config config, String selector) {
		this(driver, config, selector, findBy(selector));
	}

	public Container(WebDriver driver, Config config, WebElement element) {
		this(driver, config, null, null, element);
		this.selector = getSelector(element);
		this.locator = findBy(selector);
	}

	public Container(WebDriver driver, Config config, String selector, By locator) {
		this(driver, config, selector, locator, null);
		this.element = getElement(locator);
	}

	public Container(WebDriver driver, Config config, String selector, By locator, WebElement element) {
		super(driver, config, selector, locator, element);
	}

	public Container waitAndFindContainer(String selector) {
		WebElement element = waitAndFindElement(selector);
		return new Container(driver, config, selector, locator, element);
	}

	public Container find(String selector) {
		return new Container(driver, config, this.selector + ' ' + selector, getBy(selector));
	}
	
	public Container find(WebElement element) {
		return new Container(driver, config, element);
	}
	
	public Container find(WebElement element, int index) {
		String selector = getSelector(element);
		return new Container(driver, config, selector + ":nth-child(" + (index + 1) + ")");
	}

	public Container getIFrame(String selector) {
		WebElement element = getElement(selector);
		WebDriver frameDriver = driver.switchTo().frame(element);
		Container iframe = new Container(frameDriver, config, "body", By.cssSelector("body"));
		executeScript("arguments[0].focus()", iframe.getElement());
		iframe.iframeElement = element;
		return iframe;
	}

	public WebElement getIFrameElement() {
		return iframeElement;
	}
	
	public WebDriver switchToDefault() {
		return driver.switchTo().defaultContent();
	}

	@Override
	public By getBy(String selector, By parent) {
		By locator;
		if (iframeElement != null && "body".equals(selector)) {
			log.debug("Find selector: " + this.selector + ' ' + selector);
			locator = parent;
		} else {
			locator = super.getBy(selector, parent);
		}
		return locator;
	}
}