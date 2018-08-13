package com.softigent.sftselenium;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.sun.glass.events.KeyEvent;

public class Element {

	protected WebDriver driver;
	protected Config config;
	protected String selector;
	protected By locator;
	protected WebElement element;
	
	public static List<Throwable> assertErrorCollector;
	
	final String JS_BUILD_CSS_SELECTOR =
		    "var n = []; function t(e) { var i = 1; p = e.parentNode; " +
		    "while (e = e.previousElementSibling) { i++;}; " +
		    "n.unshift(p.tagName.toLowerCase() + ':nth-child(' + i + ')'); if (p.nodeName !== 'BODY') " +
		    "t(p);};t(arguments[0]); n.unshift('body'); return n.join('>');";
	
	//document.evaluate(PATH, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
	final String JS_BUILD_XPATH_SELECTOR =
		    "var n = []; function t(e) { while (e) { if (e.tagName) { " +
		    "var p = e; var i = 1; while (p = p.previousElementSibling) { " +
		    "if(e.tagName == p.tagName) i++; } n.unshift(e.tagName.toLowerCase() + " + 
		    "(i > 1 ? '[' + i + ']' : '')); } e = e.parentNode; }}; t(arguments[0]); return n.join('/');";
	
	static Logger log = CacheLogger.getLogger(Element.class.getName());

	public String getSelector() {
		return selector;
	}

	public WebDriver getDriver() {
		return driver;
	}

	public WebElement getElement() {
		return element;
	}

	public static String getSelector(WebElement element) {
 		String[] s = element.toString().split(" -> ");
		return s[1].replace("css selector: ", "").replaceFirst("]$","");
	}

	public Element(WebDriver driver, Config config, String selector) {
		this(driver, config, selector, findBy(selector));
	}

	public Element(WebDriver driver, Config config, WebElement element) {
		this(driver, config, null, null, element);
		this.selector = getSelector(element);
		this.locator = findBy(selector);
	}

	public Element(WebDriver driver, Config config, String selector, By locator) {
		this(driver, config, selector, locator, null);
		this.element = getElement(locator);
	}

	public Element(WebDriver driver, Config config, String selector, By locator, WebElement element) {
		this.driver = driver;
		this.config = config;
		this.selector = fixSelector(selector);
		this.locator = locator;
		this.element = element;
	}
	
	public Actions getActions() {
		return new Actions(driver);
	}

	public WebElement findElement(String selector) {
		return findElement(getBy(selector));
	}

	public WebElement findElement(By locator) {
		return getElement(locator);
	}

	public static List<WebElement> findElements(WebElement parent, String path) {
		return parent.findElements(findBy(path));
	}
	
	public List<WebElement> findElements(String selector) {
		return getElements(getBy(selector), -1);
	}

	public WebElement findElement(String selector, WebElement element) {
		return element.findElement(getBy(selector));
	}

	public static WebElement findElement(WebElement parentElement, String path) {
		return parentElement.findElement(findBy(path));
	}
	
	public Element createElement(WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String selector = (String)js.executeScript(JS_BUILD_CSS_SELECTOR, element);
		return new Element(driver, config, selector, findBy(selector), element);
	}

	public Element find(String selector) {
		return new Element(driver, config, this.selector + ' ' + selector, getBy(selector));
	}
	
	public Element find(WebElement element) {
		return new Element(driver, config, element);
	}

	public static WebElement getParent(WebElement element, String path) {
		return element.findElement(By.xpath(path));
	}

	public WebElement getParent(String selector, String path) {
		return getParent(findElement(selector), path);
	}

	public WebElement getParent() {
		return getParent("..");
	}

	public WebElement getParent(String path) {
		return getParent(element, path);
	}

	public static WebElement getParent(WebElement node) {
		return getParent(node, "..");
	}

	public By getBy(String selector) {
		return getBy(selector, this.locator);
	}

	@SuppressWarnings("static-access")
	public By getBy(String selector, By parent) {
		By locator;
		String fixSelector = fixSelector(selector);
		log.debug("Find selector: " + this.selector + ' ' + fixSelector);
		if (selector.startsWith("xpath:")) {
			locator = parent.xpath(fixSelector);
		} else {
			locator = parent.cssSelector(this.selector + ' ' + fixSelector);
		}
		return locator;
	}

	public static By findBy(String selector) {
		By locator;
		String fixSelector = fixSelector(selector);
		log.debug("Find selector: " + fixSelector);
		if (selector.startsWith("xpath:")) {
			locator = By.xpath(fixSelector);
		} else {
			locator = By.cssSelector(fixSelector);
		}
		return locator;
	}
	
	public static String fixSelector(String selector) {
		if (selector.startsWith("xpath:")) {
			selector = selector.substring(6);
		} else {
			if (selector.indexOf("#") != -1) {
				String[] array = selector.split(" ");
				for (int i = 0; i < array.length; i++) {
					if (array[i].charAt(0) == '#') {
						String[] childred = array[i].split(">"); //Validate on child selector: #a>b
						array[i] = "[id='" + childred[0].substring(1) + "']";
						for (int j = 1; j < childred.length; j++) {
							array[i] += ">" + childred[j];
						}
					}
				}
				selector = String.join(" ", array);
			}
		}
		return selector;
	}

	public String getElementName(WebElement element) {
		String s[] = element.toString().split(" -> ");
		if (s.length > 2) {
			return s[1].replaceAll("]]$", "") + " -> " + s[2].replaceAll("]$", "");
		} else {
			return s[1].replaceAll("]$", "");
		}
	}

	public List<WebElement> getElements(String selector) {
		return getElements(selector, 1);
	}

	public List<WebElement> getElements(String selector, int expectSize) {
		return getElements(getBy(selector), expectSize);
	}

	public List<WebElement> getElements(By locator, int expectSize) {
		log.trace("Find element(s) " + locator);
		List<WebElement> elements = null;
		try {
			elements = driver.findElements(locator);
		} catch (Exception e) {
			log.error(e.getMessage());
			fail();
		}
		if (expectSize != -1 && (elements == null || elements.size() == 0)) {
			log.error("Cannot find an element for locator: " + locator + " [" + driver.getCurrentUrl() + "]");
			fail();
		}
		if (expectSize == 1 && elements.size() > 1) {
			log.warn("Found elements=" + elements.size() + " for locator: " + locator);
		} else {
			log.debug("Elements (" + elements.size() + ") - " + locator);
		}
		return elements;
	}

	public WebElement getElementByText(String path, String text) {
		log.debug("getElementByText: " + getElementName(this.element));
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String currentPath = (String)js.executeScript(JS_BUILD_XPATH_SELECTOR, this.element);
		By by = getBy("xpath://" + path + "[text()='" + text + "']");
		List<WebElement> elements = driver.findElements(by);
		for (WebElement element : elements) {
			String selector = (String)js.executeScript(JS_BUILD_XPATH_SELECTOR, element);
			if (selector.startsWith(currentPath)) {
				return element;
			}
		}
		return null;
	}

	public WebElement getElement(String selector) {
		return getElement(selector, 1);
	}
	
	public WebElement getElement(String selector, int expectSize) {
		return getElement(getBy(selector), expectSize);
	}
	
	public WebElement getElement(By locator) {
		return getElement(locator, 1);
	}

	public WebElement getElement(By locator, int expectSize) {
		List<WebElement> elements = getElements(locator, expectSize);
		if (elements != null && elements.size() > 0) {
			return elements.get(0);
		}
		if (expectSize != -1) {
			log.warn("Cannot find an element for locator: " + locator);
			fail();
		}
		return null;
	}

	public Map<String, Number> getElementClientRect(WebElement element) {
		@SuppressWarnings("unchecked")
		Map<String, Number> rect = (Map<String, Number>) executeScript("return arguments[0].getBoundingClientRect()",
				element, null, null);
		return rect;
	}

	//Be sure the Developer Tool is closed or dock to right, otherwise vertical calculation will be failed.
	//Chrome download bar must be closed. See: SeleniumUtils.resetWindow();
	public Point getElementLocation(WebElement element) {
		@SuppressWarnings("unchecked")
		ArrayList<Number> o = (ArrayList<Number>) executeScript(
				"return (function(o) { var l = [screenX || screenLeft, (screenY || screenTop) + ((screen.height - innerHeight) - (screen.height - outerHeight))]; "
						+ "while(o) { l[0] += + o.offsetLeft; l[1] += + o.offsetTop; o = o.offsetParent; } return l;})(arguments[0])",
				element, null, null);
		Point offset = config.getWindowOffset();
		return new Point(o.get(0).intValue() + offset.x, o.get(1).intValue() - offset.y);
	}

	public Object executeScript(String selector, String command, String attrName, Object value) {
		return this.executeScript(command, getElement(selector), attrName, value);
	}

	public Object executeScript(String command, WebElement element, String attrName, Object value) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		return js.executeScript(command, element, attrName, value);
	}

	public Object executeScript(String command, WebElement element) {
		return this.executeScript(command, element, null, null);
	}

	public Object executeScript(String command) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		return js.executeScript(command);
	}

	public void setFocus(String selector) {
		setFocus(waitAndFindElement(selector));
	}
	
	public void setFocus() {
		setFocus(element);
	}
	
	public void setFocus(WebElement element) {
		log.debug("setFocus on: " + getElementName(element));
		executeScript("arguments[0].focus();", element);
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public void clearAllText(String selector) {
		clearText(selector, -1);
	}

	public void clearText(String selector) {
		clearText(selector, 1);
	}

	public void clearText(String selector, int index) {
		List<WebElement> elements = getElements(selector, index);
		if (elements != null) {
			for (int i = 0; i < elements.size(); i++) {
				if (index == -1 || i == index - 1) {
					WebElement element = elements.get(i);
					element.clear();
				}
			}
		}
	}

	public void clearText() {
		element.clear();
	}
	
	public void keyDown(String str) {
		this.keyDown(element, str, Keys.COMMAND);
	}
	
	public void keyDown(String selector, String str) {
		this.keyDown(getElement(selector), str, Keys.COMMAND);
	}
	
	public void keyDown(WebElement element, String str, Keys key) {
		log.debug("keyDown on: " + getElementName(element));
		if (str != null) {
			new Actions(driver).sendKeys(element, str).perform();
		}
		new Actions(driver).keyDown(element, key).perform();
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public void keyUp(String str) {
		this.keyUp(element, str, Keys.COMMAND);
	}
	
	public void keyUp(String selector, String str) {
		this.keyUp(getElement(selector), str, Keys.COMMAND);
	}
	
	public void keyUp(WebElement element, String str, Keys key) {
		log.debug("keyUp on: " + getElementName(element));
		if (str != null) {
			new Actions(driver).sendKeys(element, str).perform();
		}
		new Actions(driver).keyUp(element, key).perform();
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public void setText(String selector, String value) {
		this.setText(selector, value, true);
	}

	public void setText(String selector, String value, boolean doClear) {
		this.setText(selector, value, 0, doClear);
	}

	public void setText(String selector, String value, int index, boolean doClear) {
		log.debug("Set Text index=" + index + ", value=" + value);
		List<WebElement> elements = getElements(selector);
		if (elements != null && elements.size() > index) {
			if (doClear) {
				elements.get(index).clear();
			}
			elements.get(index).sendKeys(value);
			SeleniumUtils.sleep(config.getActionDelay());
		}
	}
	
	public void setText(String value) {
		setText(element, value);
	}

	public void setText(WebElement element, String value) {
		element.sendKeys(value);
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public WebElement setText(String selector, Object value, String lastChar) {
		return setText(getElement(selector), value, lastChar);
	}
	
	public WebElement setText(WebElement elemenet, Object value, String lastChar) {
		executeScript("arguments[0].value=arguments[2];", elemenet, null, value);
		elemenet.sendKeys(lastChar);
		SeleniumUtils.sleep(config.getActionDelay());
		return elemenet;
	}
	
	public WebElement waitValue(String selector, String value) {
		log.debug("waitValue: " + value + " in selector=" + selector);
		return this.waitWhenTrue(new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				try {
					element = getElement(selector);
					return Element.regExpString(getValue(element), value);
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	public WebElement waitText(String selector, String value) {
		log.debug("waitText: " + value + " in selector=" + selector + ", value=" + value);
		return this.waitWhenTrue(new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				try {
					element = getElement(selector, -1);
					String text = getText(element);
					return Element.regExpString(text, value);
				} catch (Exception e) {
					return false;
				}
			}
		});
	}
	
	public WebElement waitHtmlText(String selector, String value) {
		log.debug("waitHtmlText: " + value + " in selector=" + selector);
		return this.waitWhenTrue(new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				try {
					element = getElement(selector, -1);
					return Element.regExpString(getHTML(element).trim(), value);
				} catch (Exception e) {
					return false;
				}
			}
		});
	}
	
	public void sendKeys(String value) {
		log.debug("value=" + value);
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public void sendKeys(String selector, String value) {
		this.sendKeys(selector, value, 0);
	}

	public void sendKeys(String selector, String value, int index) {
		log.debug("sendKeys index=" + index + ", value=" + value);
		List<WebElement> elements = getElements(selector);
		if (elements != null && elements.size() > index) {
			elements.get(index).sendKeys(value);
			SeleniumUtils.sleep(config.getActionDelay());
		}
	}
	
	public String getText() {
		return getText(element);
	}

	public String getText(WebElement element) {
		log.debug("Get Text for selector: " + getElementName(element));
		if (element != null) {
			if (element.getTagName().toUpperCase().equals("INPUT") ||
				element.getTagName().toUpperCase().equals("TEXTAREA")) {
				return element.getAttribute("value");
			}
			return element.getText();
		}
		return null;
	}

	public String getText(String selector) {
		return getText(getElement(selector));
	}

	public String getValue(String selector) {
		return getValue(getElement(selector));
	}
	
	public String getValue(WebElement element) {
		log.debug("Get Value for element: " + getElementName(element));
		if (element != null) {
			return element.getAttribute("value");
		}
		return null;
	}
	
	public Boolean compareValue(String value) {
		log.debug("Compare Value =" + value + ", for selector: " + selector);
		return compareString(getValue(element), value);
	}

	public Boolean compareValue(String selector, String value) {
		log.debug("Compare Value =" + value + ", for selector: " + selector);
		return compareString(getValue(selector), value);
	}

	public int validateValue(String value) {
		log.debug("Validate Value =" + value + ", for selector: " + selector);
		return validateString(getValue(element), value)  ? 0 : 1;
	}

	public int validateValue(String selector, String value) {
		log.debug("Validate Value =" + value + ", for selector: " + selector);
		return validateString(getValue(selector), value) ? 0 : 1;
	}

	public Boolean assertValue(String value) {
		log.debug("Assert Value =" + value + ", for selector: " + selector);
		return assertString(getValue(selector), value);
	}

	public Boolean assertValue(String selector, String value) {
		log.debug("Assert Value =" + value + ", for selector: " + selector);
		return assertString(getValue(selector), value);
	}

	public Boolean compareText(String value) {
		log.debug("Compare Text value=" + value + ", for selector: " + selector);
		return compareString(getText(element), value);
	}

	public Boolean compareText(String selector, String value) {
		log.debug("Compare Text value=" + value + ", for selector: " + selector);
		return compareString(getText(selector), value);
	}

	public int validateText(String value) {
		log.debug("Validate Text value=" + value + ", for selector: " + selector);
		return validateString(getText(element), value)  ? 0 : 1;
	}

	public int validateText(String selector, String value) {
		log.debug("Validate Text value=" + value + ", for selector: " + selector);
		return validateString(getText(selector), value) ? 0 : 1;
	}

	public Boolean assertText(String value) {
		log.debug("Assert Text value=" + value + ", for selector: " + selector);
		return assertString(getText(), value);
	}

	public Boolean assertText(String selector, String value) {
		log.debug("Assert Text value=" + value + ", for selector: " + selector);
		return assertString(getText(selector), value);
	}

	public void setHTML(String selector, String value) {
		log.debug("Set HTML value=" + value);
		executeScript(selector, "arguments[0].innerHTML=arguments[2];", null, value);
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public String getHTML() {
		return getHTML(element);
	}

	public String getHTML(String selector) {
		return getHTML(getElement(selector));
	}

	public String getHTML(WebElement element) {
		return getAttributeValue(element, "innerHTML");
	}

	public Boolean validateHTML(String selector, String value) {
		log.debug("Validate HTML value=" + value + ", for selector: " + selector);
		return validateString(getHTML(selector), value);
	}

	public Boolean assertHTML(String selector, String value) {
		log.debug("Assert HTML value=" + value + ", for selector: " + selector);
		return assertString(getHTML(selector), value);
	}

	public String getAttributeValue(String name) {
		log.debug("Get attribute=" + name + ", for selector: " + selector);
		return element.getAttribute(name);
	}

	public String getAttributeValue(String selector, String name) {
		return getAttributeValue(getElement(selector), name);
	}

	public String getAttributeValue(WebElement element, String name) {
		log.debug("Get attribute=" + name + ", for element: " + getElementName(element));
		if (element != null) {
			return element.getAttribute(name);
		}
		return null;
	}

	public void setAttributeValue(String selector, String name, Object value) {
		log.debug("Set setAttributeValue name=" + name + ", value=" + value + ", for selector: " + selector);
		executeScript(selector, "arguments[0].setAttribute(arguments[1], arguments[2]);", name, value);
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public boolean hasClass(WebElement element, String className) {
		log.debug("hasClass, for className: " + className + ", element: " + getElementName(element));
		if (element != null) {
			String classes = element.getAttribute("class");
			for (String c : classes.split(" ")) {
				if (c.equals(className)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasClass(String selector, String className) {
		return hasClass(getElement(selector), className);
	}

	public boolean hasClass(String className) {
		return hasClass(element, className);
	}

	public String getCssValue(String name) {
		log.debug("Get CSS=" + name + ", for selector: " + selector);
		if (element != null) {
			return element.getCssValue(name);
		}
		return null;
	}

	public String getCssValue(String selector, String name) {
		log.debug("Get CSS=" + name + ", for selector: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.getCssValue(name);
		}
		return null;
	}

	public void setCssValue(String selector, String name, Object value) {
		log.debug("Set setCssValue name=" + name + ", value=" + value + ", for selector: " + selector);
		executeScript(selector, "arguments[0].style[arguments[1]] = arguments[2];", name, value);
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public Boolean validateAttribute(String selector, String name, String value) {
		log.debug("Validate attribute=" + name + ", value=" + value + ", for selector: " + selector);
		return validateString(getAttributeValue(selector, name), value);
	}

	public Boolean assertAttribute(String selector, String name, String value) {
		log.debug("Assert attribute=" + name + ", value=" + value + ", for selector: " + selector);
		return assertString(getAttributeValue(selector, name), value);
	}

	public Boolean validateCssValue(String selector, String name, String value) {
		log.debug("Validate CSS=" + name + ", value=" + value + ", for selector: " + selector);
		return validateString(getCssValue(selector, name), value);
	}

	public Boolean assertCssValue(String selector, String name, String value) {
		log.debug("Assert CSS=" + name + ", value=" + value + ", for selector: " + selector);
		return assertString(getCssValue(selector, name), value);
	}

	public WebElement getOptionByText(String selector, String value) {
		return findElementByText(selector, value, "option");
	}

	public void selectOptionByText(String selector, String text) {
		selectOptionByText(selector, text, "option");
	}

	public void selectOptionByText(String selector, String text, String tagName) {
		WebElement element = findElementByText(selector, text, tagName);
		if (element != null) {
			element.click();
		} else {
			fail("Cannot find " + tagName + ": '" + text + "' in: " + selector);
		}
	}
	
	public WebElement findElementByText(String selector, String value) {
		return findElementByText(selector, value, "li");
	}
	
	public WebElement findElementByText(String selector, String value, String tagName) {
		int index = findIndexByText(selector, value, tagName);
		if (index != -1) {
			WebElement parent = getElement(selector);
			return parent.findElements(By.tagName(tagName)).get(index);
		} else {
			return null;
		}
	}
	
	public int findIndexByText(String selector, String value) {
		return findIndexByText(selector, value, "li");
	}
	
	public int findIndexByText(String selector, String value, String tagName) {
		waitIsDisplayed(selector);
		return findIndexByText(getElement(selector), value, tagName);
	}

	public int findIndexByText(WebElement parent, String value, String tagName) {
		log.debug("findIndexByText text=" + value + ", for element: " + getElementName(parent));
		List<WebElement> elements = parent.findElements(By.tagName(tagName));
		for (int i = 0; i < elements.size(); i++) {
			WebElement element = elements.get(i);
			String text = element.getText();
			if (text == null || text.equals("")) {
				text = element.getAttribute("innerHTML");
			}
			if (text != null && text.equals(value)) {
				return i;
			}
		}
		return -1;
	}

	public void enter(String selector) {
		enter(waitAndFindElement(selector));
	}

	public void enter(WebElement element) {
		log.debug("Enter on: " + getElementName(element));
		element.sendKeys(Keys.ENTER);
	}
	
	public void enter() {
		element.sendKeys(Keys.ENTER);
	}
	
	public static void enterKey() {
		getRobot().keyPress(KeyEvent.VK_ENTER);
	}
	
	public static void tab() {
		getRobot().keyPress(KeyEvent.VK_TAB);
	}
	
	// Click Alt-F4 to close outlook, printer windows
	public static void closeWindow() {
		Element.keyPress(new int[] { KeyEvent.VK_ALT, KeyEvent.VK_F4 });
	}
	
	public void click(String selector) {
		WebElement element = waitAndFindElement(selector);
		waitIsEnabled(selector);
		click(element);
	}
	
	public void forceClick(String selector) {
		WebElement element = SeleniumUtils.waitAndFindElement(driver, locator, config.getPageLoadTimeout(), false);
		click(element);
	}

	public void click() {
		this.click(element);
	}

	public void click(WebElement parentElement, String path) {
		this.click(parentElement.findElement(findBy(path)));
	}

	public void click(WebElement element) {
		if (element != null) {
			log.debug("Click on: " + getElementName(element));
			this.waitWhenTrue(element, new IWaitCallback() {
				public boolean isTrue(WebElement element) {
					return element.isDisplayed();
				}
			});
			try {
				element.click();
			} catch (Exception e) {
				mouseClick(element);
			}
			SeleniumUtils.sleep(config.getActionDelay());
		}
	}
	
	public void doubleClick(String selector) {
		doubleClick(waitAndFindElement(selector));
	}
	
	public void doubleClick(WebElement element) {
		log.debug("DoubleClick on: " + getElementName(element));
		Actions action = new Actions(driver);
		action.moveToElement(element).doubleClick().build().perform();
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public void jsClick() {
		jsClick(selector);
	}
	
	public void jsClick(String selector) {
		jsClick(waitAndFindElement(selector));
	}
	
	public void jsClick(WebElement element) {
		log.debug("jsClick on: " + getElementName(element));
		executeScript("arguments[0].click();", element);
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public void jsEventClick(WebElement element) {
		createEvent(element, "click");
	}
	
	public void createEvent(String eventName) {
		createEvent(element, eventName);
	}
	
	public void createEvent(WebElement element, String eventName) {
		log.debug("createEvent: " + eventName + ", " + getElementName(element));
		executeScript("var event = document.createEvent('Event'); event.initEvent('" + eventName + "', true, true); arguments[0].dispatchEvent(event);", element);
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public Actions contextClick(String selector) {
		return this.contextClick(waitAndFindElement(selector));
	}
	
	public Actions contextClick(WebElement element) {
		Actions action = new Actions(driver).contextClick(element);
		action.build().perform();
		return action;
	}
	
	public Actions mouseClick(String selector) {
		return mouseClick(selector, 0, 0);
	}

	public Actions mouseClick(WebElement element) {
		return this.mouseClick(element, 0, 0);
	}

	public Actions mouseClick(String selector, int x, int y) {
		return mouseClick(waitAndFindElement(selector), x, y);
	}

	public Actions mouseClick(WebElement element, int x, int y) {
		log.debug("Mouse Click (" + x + 'x' + y + ") on: " + getElementName(element));
		executeScript("return arguments[0].scrollIntoView(true);", element);
		Actions action = new Actions(driver);
		action.moveToElement(element, x, y).click();
		action.build().perform();
		SeleniumUtils.sleep(config.getActionDelay());
		return action;
	}

	public Actions mouseMove(String selector) {
		return mouseMove(selector, 0, 0);
	}

	public Actions mouseMove(String selector, int x, int y) {
		return this.mouseMove(waitAndFindElement(selector), x, y);
	}
	
	public Actions mouseMove(WebElement element) {
		return this.mouseMove(element, 0, 0);
	}
	
	public Actions mouseMove() {
		return this.mouseMove(element, 0, 0);
	}

	public Actions mouseMove(WebElement element, int x, int y) {
		log.debug("Mouse Move (" + x + 'x' + y + ") on: " + getElementName(element));
		Actions action = new Actions(driver);
		action.moveToElement(element, x, y);
		action.build().perform();
		SeleniumUtils.sleep(config.getActionDelay());
		return action;
	}
	
	public void robotMouseMove(String selector) {
		robotMouseMove(selector, 0, 0);
	}
	
	public void robotMouseMove(String selector, int offsetX, int offsetY) {
		robotMouseMove(waitAndFindElement(selector), offsetX, offsetY);
	}
	
	public void robotMouseMove(WebElement element) {
		robotMouseMove(element, 0, 0);
	}

	public void robotMouseMove(WebElement element, int offsetX, int offsetY) {
		executeScript("return arguments[0].scrollIntoView(true);", element);
		WebElement body = driver.findElement(By.tagName("body"));
		@SuppressWarnings("unchecked")
		ArrayList<Number> a = (ArrayList<Number>) executeScript(
				"return (function(o) { var l = [o.scrollLeft || o.parentElement.scrollLeft, o.scrollTop || o.parentElement.scrollTop]; return l;})(arguments[0])", body);
		wait(.2f);
		Point point = getElementLocation(element);
		log.info("robotMouseMove: " + (point.x + offsetX) + 'x' + (point.y + offsetY) + " - " + a.get(0).intValue() + 'x' + a.get(1).intValue());
		robotMouseMove(point.x + offsetX - a.get(0).intValue(), point.y + offsetY - a.get(1).intValue());
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public void robotMouseMove(int x, int y) {
		getRobot().mouseMove(x, y);
	}
	
	public void robotMouseClick(String selector) {
		robotMouseClick(selector, 0, 0);
	}
	
	public void robotMouseClick(String selector, int offsetX, int offsetY) {
		robotMouseClick(waitAndFindElement(selector), offsetX, offsetY);
	}

	public void robotMouseClick(WebElement element) {
		robotMouseClick(element, 0, 0);
	}

	public void robotMouseClick(WebElement element, int offsetX, int offsetY) {
		robotMouseMove(element, offsetX, offsetY);
		robotMouseClick();
	}

	public void robotMouseClick() {
		robotMouseClick(true);
	}

	public void robotMouseClick(boolean moveMouseOut) {
		Robot robot = getRobot(100);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		if (moveMouseOut) {
			//robotMouseMove(0, 0);
		}
	}

	public void robotMouseDragAndDrop(WebElement source, WebElement target) throws AWTException {
		robotMouseDragAndDrop(source, target, 0, 0);
	}
	
	public void robotMouseDragAndDrop(WebElement source, WebElement target, int offsetX, int offsetY) {
		robotMouseDragAndDrop(source, target, offsetX, offsetY, offsetX, offsetY);
	}

	public void robotMouseDragAndDrop(WebElement source, WebElement target, int offsetX1, int offsetY1, int offsetX2, int offsetY2) {
		WebElement element;
		if (source.getLocation().y > target.getLocation().y) {
			element = target;
		} else {
			element = source;
		}
		executeScript("return arguments[0].scrollIntoView(true);", element);
		WebElement body = driver.findElement(By.tagName("body"));
		@SuppressWarnings("unchecked")
		ArrayList<Number> a = (ArrayList<Number>) executeScript(
				"return (function(o) { var l = [o.scrollLeft || o.parentElement.scrollLeft, o.scrollTop || o.parentElement.scrollTop]; return l;})(arguments[0])", body);
		wait(.2f);
		log.info("robotMouseDragAndDrop: " + a.get(0).intValue() + 'x' + a.get(1).intValue());

		Robot robot = getRobot(500);
		Point sourcePoint = getElementLocation(source);
		Point targetPoint = getElementLocation(target);
		// drag
		log.info("Source - x=" + (sourcePoint.x + offsetX1 - a.get(0).intValue()) + ", y=" + (sourcePoint.y + offsetY1 - a.get(1).intValue()));
		robot.mouseMove(sourcePoint.x + offsetX1 - a.get(0).intValue(), sourcePoint.y + offsetY1 - a.get(1).intValue());
		wait(.2f);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseMove(sourcePoint.x - a.get(0).intValue(), sourcePoint.y - a.get(1).intValue() - 5); //register drag event
		wait(.2f);
		// drop
		log.info("Target - x=" + (targetPoint.x + offsetX2 - a.get(0).intValue()) + ", y=" + (targetPoint.y + offsetY2 - a.get(1).intValue()));
		robot.mouseMove(targetPoint.x + offsetX2 - a.get(0).intValue(), targetPoint.y + offsetY2 - a.get(1).intValue());
		wait(.2f);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		wait(.2f);
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public void mouseDragAndDrop(String source, String target) {
		mouseDragAndDrop(getElement(source), getElement(target));
	}

	public void mouseDragAndDrop(WebElement source, WebElement target) {
		log.debug("Mouse DragAndDrop: source=" + getElementName(source) + ", target=" + getElementName(target));
		Actions action = new Actions(driver);
		action.dragAndDrop(source, target);
		action.build().perform();
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public void clickAndHoldDrag(WebElement source, WebElement target) {
		clickAndHoldDrag(source, target);
	}

	public void clickAndHoldDrag(WebElement source, WebElement target, int offsetX, int offsetY) {
		log.debug("Mouse clickAndHold: source=" + getElementName(source) + ", target=" + getElementName(target));
		Actions action = new Actions(driver);
		action.moveToElement(source, offsetX, offsetY).clickAndHold(source).moveToElement(target).release(source);
		action.build().perform();
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	//key - java.awt.event.KeyEvent;
	public static void keyPress(int key) {
		keyPress(key, getRobot());
	}

	public static void keyPress(int key, Robot robot) {
		int[] keys = new int[1];
		keys[0] = key;
		keyPress(keys, robot);
	}

	public static void keyPress(int [] keys) {
		keyPress(keys, getRobot());
	}
		
	public static void keyPress(int [] keys, Robot robot) {
		log.info("keyPress: " + Arrays.toString(keys));
		for (int i = 0; i < keys.length; i++) {
			robot.keyPress(keys[i]);
		}
		for (int i = keys.length - 1; i >= 0; i--) {
			robot.keyRelease(keys[i]);
		}
	}
	
	public static Robot getRobot() {
		return getRobot(0);
	}
		
	public static Robot getRobot(int delay) {
		Robot robot = null;
		try {
			robot = new Robot();
			if (delay > 0) {
				robot.setAutoDelay(100);
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}
		return robot;
	}

	public boolean isSelected(String selector) {
		log.debug("isSelected: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			try {
				return element.isSelected();
			} catch (StaleElementReferenceException elementHasDisappeared) {
				return false;
			}
		}
		return false;
	}

	public boolean isEnabled(String selector) {
		log.debug("isEnabled: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			try {
				return element.isEnabled();
			} catch (StaleElementReferenceException elementHasDisappeared) {
				return false;
			}
		}
		return false;
	}
	
	public WebElement activeElement() {
		return driver.switchTo().activeElement();
	}

	public Alert alertWindow(int state) {
		Alert alert = null;
		try {
			alert = driver.switchTo().alert();
			if (alert !=  null) {
				switch (state) {
					case 1:
						alert.accept(); // for two buttons, choose the affirmative one
						break;
					case 2:
						alert.dismiss();
						break;
				}
			}
		} catch (Exception e) {}
		return alert;
	}

	public WebElement waitIsEnabled(String selector) {
		log.debug("waitIsEnabled: " + selector);
		return this.waitWhenTrue(selector, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				try {
					return element.isEnabled();
				} catch (StaleElementReferenceException elementHasDisappeared) {
					return false;
				}
			}
		});
	}
	
	public WebElement waitIsEnabled() {
		log.debug("waitIsEnabled: " + getElementName(this.element));
		return this.waitWhenTrue(this.element, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				try {
					return element.isEnabled();
				} catch (StaleElementReferenceException elementHasDisappeared) {
					return false;
				}
			}
		});
	}
	
	public boolean isNotDisplayed(String selector) {
		return !isDisplayed(selector);
	}

	public boolean isDisplayed(String selector) {
		return isDisplayed(getElement(selector, -1));
	}
	
	public boolean isDisplayed(WebElement element) {
		if (element != null) {
			log.debug("isDisplayed: " + getElementName(element));
			try {
				return element.isDisplayed();
			} catch (StaleElementReferenceException elementHasDisappeared) {
				return false;
			}
		}
		return false;
	}

	public boolean isExists() {
		return element != null;
	}
	
	public boolean isNotExists() {
		return element == null;
	}

	public boolean isExists(String selector) {
		List<WebElement> elements = driver.findElements(getBy(selector));
		log.debug("isExists (" + elements.size() + "): " + selector);
		return elements.size() != 0;
	}
	
	public boolean isNotExists(String selector) {
		return !isExists(selector);
	}

	public boolean isVisible() {
		return this.isVisible(element);
	}

	public boolean isVisible(String selector) {
		return this.isVisible(getElement(selector));
	}

	public boolean isVisible(WebElement element) {
		log.debug("isVisible: " + getElementName(element));
		if (element != null) {
			try {
				String style = element.getCssValue("display");
				return !"none".equals(style);
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	
	public WebElement waitSelector(String selector, boolean isExists) {
		return this.waitWhenTrue(element, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				print('.', false);
				WebElement el = null;
				try {
					el = Element.this.getElement(selector, -1);
				} catch (StaleElementReferenceException elementHasDisappeared) {
				}
				return (isExists ? el != null : el == null);
			}
		});
	}
	
	public WebElement waitIsHidden(String selector) {
		return this.waitIsHidden(this.getElement(selector, -1));
	}

	public WebElement waitIsHidden(WebElement element) {
		if (element != null) {
			return this.waitWhenTrue(element, new IWaitCallback() {
				public boolean isTrue(WebElement element) {
					print('.', false);
					try {
						return element == null || !element.isDisplayed();
					} catch (StaleElementReferenceException elementHasDisappeared) {
						return true;
					}
				}
			});
		}
		return element;
	}
	
	public WebElement waitIsDisplayed(String selector) {
		return this.waitWhenTrue(selector, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				print('.', false);
				try {
					return element.isDisplayed();
				} catch (StaleElementReferenceException elementHasDisappeared) {
					return false;
				}
			}
		});
	}
	
	public WebElement waitIsDisplayed(String selector, int count) {
		List<WebElement> els = this.getElements(selector);
		return this.waitWhenTrue(new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				print('.', false);
				int displayed = 0;
				for (WebElement el : els) {
					if (el.isDisplayed()) {
						displayed++;
					}
				}
				return displayed == count;
			}
		});
	}

	public void setBrowseFile(String path) {
		log.debug("Browse File: " + path);
		SeleniumUtils.fileBrowseDialog(this, driver, path);
	}

	public WebElement waitWhenTrue(String selector, IWaitCallback callback) {
		return this.waitWhenTrue(waitAndFindElement(selector), callback);
	}

	public WebElement waitWhenTrue(WebElement element, IWaitCallback callback) {
		return this.waitWhenTrue(element, callback, true);
	}
	
	public WebElement waitWhenTrue(WebElement element, IWaitCallback callback, boolean isPrint) {
		return waitWhenTrue(config.getPageLoadTimeout(), element, callback, isPrint);
	}
	
	public WebElement waitWhenTrue(int timeoutInSec, WebElement element, IWaitCallback callback, boolean isPrint) {
		for (int i = 0; i < timeoutInSec; i++) {
			if (isPrint) {
				print('.', false);
			}
			if (callback.isTrue(element)) {
				if (isPrint) {
					print('.');
				}
				return element;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		fail("TIMEOUT: [" + driver.getCurrentUrl() + "]");
		return null;
	}
	
	public WebElement waitWhenTrue(IWaitCallback callback) {
		return this.waitWhenTrue(element, callback, true);
	}
	
	public WebElement waitWhenTrue(int timeout, IWaitCallback callback) {
		return this.waitWhenTrue(timeout, element, callback, true);
	}
	
	public static void isFalse(Boolean bool, String message) {
		Element.isTrue(bool == false, message);
	}
	
	public static void isTrue(Boolean bool, String message) {
		if (!bool) {
			assertFail("<<<<<<<<<<[" + message + "]>>>>>>>>>>");
		}
	}

	public static void assertFail(String message) {
		if (assertErrorCollector == null) {
			fail(message);
		} else {
			assertErrorCollector.add(new AssertionError(message));
		}
	}

	public static Boolean assertString(String str1, String str2) {
		return validateString(str1, str2, true);
	}
	
	public static Boolean assertObject(Object obj1, Object obj2) {
		return assertObject(obj1, obj2, true);
	}
	
	public static Boolean assertObject(Object obj1, Object obj2, boolean isExpectedTrue) {
		boolean isTrue = validateObject(obj1, obj2, isExpectedTrue);
		assertTrue(isTrue);
		return isTrue;
	}
	
	public static Boolean validateObject(Object obj1, Object obj2, boolean isExcepted) {
		log.debug("ASSERT: '" + obj1 + (isExcepted ? "' = '" : "' != '") + obj2 + "'");
		boolean isTrue = (String.valueOf(obj1).equals(String.valueOf(obj2))) == isExcepted;
		if (!isTrue) {
			log.error("\n'" + obj1 + "' != \n'" + obj2 + "'");
		}
		return isTrue;
	}

	public static Boolean validateString(String str1, String str2) {
		return validateString(str1, str2, false);
	}

	public static Boolean validateString(String str1, String str2, boolean isAssert) {
		boolean isTrue = compareString(str1, str2);
		if (isAssert) {
			isTrue(isTrue, "\n'" + str1 + "' !=\n'" + str2 + "'\n");
		}
		return isTrue;
	}

	public static Boolean compareString(String str1, String str2) {
		log.debug("compareString: '" + str1 + "' = '" + str2 + "'");
		return regExpString(str1, str2);
	}
	
	public static Boolean regExpString(String str, String regExp) {
		boolean isTrue;

		if (str == null) {
			isTrue = regExp == null;
		} else {
			isTrue = str.equals(regExp);
			if (!isTrue) {
				regExp = regExp.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
				isTrue = Pattern.compile(regExp, Pattern.DOTALL).matcher(str).matches();
			}
		}
		return isTrue;
	}
	
	public static List<String> find(String regex, String str) {
		List<String> matches = new ArrayList<String>();
		Matcher m = Pattern.compile(regex).matcher(str);
		while (m.find()) {
			matches.add(m.group());
		}
		return matches;
	}

	public WebElement waitAndFindElement() {
		return this.waitAndFindElement(this.locator);
	}

	public WebElement waitAndFindElement(String selector) {
		return this.waitAndFindElement(getBy(selector));
	}

	public WebElement waitAndFindElement(By locator) {
		return waitAndFindElement(locator, config.getPageLoadTimeout());
	}
	
	public WebElement waitAndFindElement(By locator, int timeout) {
		log.trace("Wait element(s) = " + locator);
		return SeleniumUtils.waitAndFindElement(driver, locator, timeout);
	}

	public void wait(float sec) {
		mlsWait(sec * 1000);
	}

	public void wait(int sec) {
		mlsWait(sec * 1000);
	}

	public static void mlsWait(float mlSec) {
		try {
			Thread.sleep((int)mlSec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void print(Object message) {
		print(message, true);
	}

	public static void print(Object message, boolean isNewLine) {
		if (log.isDebugEnabled()) {
			if (isNewLine) {
				System.out.println(message);
			} else {
				System.out.print(message);
			}
		}
	}

	public void print(WebElement element) {
		print(executeScript("return arguments[0].outerHTML", element));
	}

	public void print() {
		this.print(element);
	}
}