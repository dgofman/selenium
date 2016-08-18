package com.softigent.sftselenium;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.ArrayList;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class Element {

	protected WebDriver driver;
	protected Config config;
	protected String selector;
	protected By locator;
	protected WebElement element;

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
		return s[1].replace("css selector: ", "").replace("]", "");
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

	public WebElement findElement(String selector) {
		return findElement(getBy(selector));
	}

	public WebElement findElement(By locator) {
		return getElement(locator);
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
						array[i] = "[id='" + array[i].substring(1) + "']";
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

	public WebElement getElement(String selector) {
		return getElement(getBy(selector));
	}

	public WebElement getElement(By locator) {
		List<WebElement> elements = getElements(locator, 1);
		if (elements == null || elements.size() == 0) {
			log.warn("Cannot find an element for locator: " + locator);
			fail();
		}
		if (elements != null && elements.size() > 0) {
			return elements.get(0);
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
	public Point getElementLocation(WebElement element) {
		@SuppressWarnings("unchecked")
		ArrayList<Number> o = (ArrayList<Number>) executeScript(
				"return (function(o) { var l = [screenX || screenLeft, (screenY || screenTop) + ((screen.height - innerHeight) - (screen.height - outerHeight))]; "
						+ "while(o) { l[0] += + o.offsetLeft; l[1] += + o.offsetTop; o = o.offsetParent; } return l;})(arguments[0])",
				element, null, null);
		return new Point(o.get(0).intValue(), o.get(1).intValue());
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
		return this.executeScript(command, element, null, null);
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
		element.sendKeys(value);
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public void waitText(String selector, String value) {
		log.debug("waitText: " + value + " in selector=" + selector);
		this.waitWhenTrue(selector, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				return Element.regExpString(element.getText(), value);
			}
		});
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
		log.debug("Get Value for selector: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.getAttribute("value");
		}
		return null;
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
		return assertString(getText(selector), value);
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
		log.debug("findIndexByText text=" + value + ", for selector: " + selector);
		waitIsDisplayed(selector);
		WebElement parent = getElement(selector);
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
		enter(getElement(selector));
	}

	public void enter(WebElement element) {
		log.debug("Enter on: " + getElementName(element));
		element.sendKeys(Keys.ENTER);
	}

	public void click(String selector) {
		this.click(selector, 0, 0);
	}

	public void click(String selector, int x, int y) {
		click(waitAndFindElement(selector), x, y);
	}

	public void click() {
		this.click(element);
	}

	public void click(WebElement element) {
		this.click(element, 0, 0);
	}

	public void click(WebElement parentElement, String path) {
		this.click(findElement(parentElement, path), 0, 0);
	}

	public void click(WebElement parentElement, String path, int x, int y) {
		this.click(parentElement.findElement(findBy(path)), x, y);
	}

	public void click(WebElement element, int x, int y) {
		if (element != null) {
			if (x != 0 || y != 0) {
				try {
					log.trace("Robot click (" + x + 'x' + y + ") on: " + getElementName(element));
					robotMouseMove(element.getLocation().x + x, element.getLocation().y + y);
					robotMouseClick();
					SeleniumUtils.sleep(config.getActionDelay());
				} catch (AWTException e) {
					e.printStackTrace();
				}
			} else {
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
	}
	
	public void jsClick(String selector) {
		jsClick(waitAndFindElement(selector));
	}
	
	public void jsClick(WebElement element) {
		log.debug("jsClick on: " + getElementName(element));
		executeScript("arguments[0].click();", element);
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public void mouseClick(String selector) {
		mouseClick(selector, 0, 0);
	}

	public void mouseClick(WebElement element) {
		this.mouseClick(element, 0, 0);
	}

	public void mouseClick(String selector, int x, int y) {
		mouseClick(getElement(selector), x, y);
	}

	public void mouseClick(WebElement element, int x, int y) {
		log.debug("Mouse Click (" + x + 'x' + y + ") on: " + getElementName(element));
		Actions action = new Actions(driver);
		action.moveToElement(element, x, y).click();
		action.build().perform();
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public void mouseMove(String selector) {
		mouseMove(getElement(selector));
	}

	public void mouseMove(WebElement element) {
		this.mouseMove(element, 0, 0);
	}

	public void mouseMove(WebElement element, int x, int y) {
		log.debug("Mouse Move (" + x + 'x' + y + ") on: " + getElementName(element));
		Actions action = new Actions(driver);
		action.moveToElement(element, x, y);
		action.click().build().perform();
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public void robotMouseMove(int x, int y) throws AWTException {
		Robot robot = new Robot();
		robot.mouseMove(x, y);
	}

	public void robotMouseClick() throws AWTException {
		Robot robot = new Robot();
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}

	public void robotMouseDragAndDrop(WebElement source, WebElement target) throws AWTException {
		robotMouseDragAndDrop(source, target, 0, 0);
	}

	public void robotMouseDragAndDrop(WebElement source, WebElement target, int offsetX, int offsetY) throws AWTException {
		Robot robot = new Robot();
		Point sourcePoint = getElementLocation(source);
		Point targetPoint = getElementLocation(target);
		// drag
		robot.mouseMove(sourcePoint.x + offsetX, sourcePoint.y + offsetY);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		wait(1);
		// drop
		robot.mouseMove(targetPoint.x + offsetX, targetPoint.y + offsetY);
		wait(1);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
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
		
		/*action.clickAndHold(element).build().perform();
		action.release(element).build().perform();*/
	}

	public boolean isSelected(String selector) {
		log.debug("isSelected: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.isSelected();
		}
		return false;
	}

	public boolean isEnabled(String selector) {
		log.debug("isEnabled: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.isEnabled();
		}
		return false;
	}

	public Alert alertWindow(int state) {
		Alert alert = driver.switchTo().alert();
		switch (state) {
		case 1:
			alert.accept(); // for two buttons, choose the affirmative one
			break;
		case 2:
			alert.dismiss();
			break;
		}
		return alert;
	}

	public void waitIsEnabled(String selector) {
		log.debug("waitIsEnabled: " + selector);
		this.waitWhenTrue(selector, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				return element.isEnabled();
			}
		});
	}

	public boolean isDisplayed(String selector) {
		log.debug("isDisplayed: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			print(element.isDisplayed() + " > " + element.getAttribute("style"));
			return element.isDisplayed();
		}
		return false;
	}

	public boolean isExists() {
		return element != null;
	}

	public boolean isExists(String selector) {
		List<WebElement> elements = driver.findElements(getBy(selector));
		log.debug("isExists (" + elements.size() + "): " + selector);
		return elements.size() != 0;
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

	public void waitIsDisplayed(String selector) {
		this.waitWhenTrue(selector, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				print('.', false);
				return element.isDisplayed();
			}
		});
	}
	
	public void waitIsDisplayed(String selector, int count) {
		List<WebElement> els = this.getElements(selector);
		this.waitWhenTrue(new IWaitCallback() {
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
		SeleniumUtils.fileBrowseDialog(driver, path);
	}

	public void waitWhenTrue(String selector, IWaitCallback callback) {
		this.waitWhenTrue(SeleniumUtils.waitAndFindElement(driver, getBy(selector), config.getPageLoadTimeout()), callback);
	}

	public void waitWhenTrue(WebElement element, IWaitCallback callback) {
		this.waitWhenTrue(element, callback, true);
	}
	
	public void waitWhenTrue(WebElement element, IWaitCallback callback, boolean isPrint) {
		for (int i = 0; i < config.getPageLoadTimeout(); i++) {
			if (isPrint) {
				print('.', false);
			}
			if (callback.isTrue(element)) {
				if (isPrint) {
					print('.');
				}
				return;
			}
			wait(1);
		}
		fail("TIMEOUT: [" + driver.getCurrentUrl() + "]");
	}
	
	public void waitWhenTrue(IWaitCallback callback) {
		this.waitWhenTrue(element, callback, true);
	}
	
	public static void isTrue(Boolean bool, String message) {
		if (!bool) {
			fail(message);
		}
	}

	public static void assertFail(String message) {
		fail(message);
	}

	public static Boolean assertString(String str1, String str2) {
		return validateString(str1, str2, true);
	}
	
	public static Boolean assertObject(Object obj1, Object obj2) {
		boolean isTrue = validateObject(obj1, obj2);
		assertTrue(isTrue);
		return isTrue;
	}
	
	public static Boolean validateObject(Object obj1, Object obj2) {
		log.debug("compareString: '" + obj1 + "' = '" + obj2 + "'");
		boolean isTrue = obj1 == obj2;
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
			assertTrue(isTrue);
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
		log.trace("Wait element(s) = " + locator);
		return SeleniumUtils.waitAndFindElement(driver, locator, config.getPageLoadTimeout());
	}

	public void wait(float sec) {
		mlsWait((int) sec * 1000);
	}

	public void wait(int sec) {
		mlsWait(sec * 1000);
	}

	public static void mlsWait(int mlSec) {
		try {
			Thread.sleep(mlSec);
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
		print(executeScript("return arguments[0].outerHTML", element, null, null));
	}

	public void print() {
		this.print(element);
	}
}