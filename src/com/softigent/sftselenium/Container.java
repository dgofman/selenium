package com.softigent.sftselenium;

import java.util.List;

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
		this(driver, config, getSelector(element), null, element);
		this.locator = findBy(selector);
	}

	public Container(WebDriver driver, Config config, String selector, By locator) {
		this(driver, config, selector, locator, null);
		List<WebElement> elements = getElements(locator, -1);
		this.element = elements.size() == 0 ? null : elements.get(0);
	}

	public Container(WebDriver driver, Config config, String selector, By locator, WebElement element) {
		super(driver, config, selector, locator, element);
	}

	public Container getIFrame(String selector) {
		WebElement element = getElement(selector);
		WebDriver frameDriver = driver.switchTo().frame(element);
		Container iframe = new Container(frameDriver, config, "(IFRAME) document.querySelector('" + selector + "').contentDocument:", By.cssSelector("body"));
		iframe.selectorPath = "body";
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

	public void close(BaseTest test) {
		driver.close();
		test.switchWindow(test.testHandleId);	
	}
}