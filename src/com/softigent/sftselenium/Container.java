package com.softigent.sftselenium;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class Container {

	private WebDriver driver;
	private Config config;
	private String selector;
	private By locator;
	private WebElement element;

	static Logger log = Logger.getLogger(Container.class.getName());

	public String getSelector() {
		return selector;
	}

	public Container(WebDriver driver, Config config, String selector) {
		this(driver, config, selector, By.cssSelector(selector));
	}
	
	public Container(WebDriver driver, Config config, String selector, By locator) {
		this.driver = driver;
		this.config = config;
		this.selector = selector;
		this.locator = locator;
		this.element = getElement(locator);
	}
	
	public By findBy(String selector) {
		return getBy(selector);
	}
	
	public WebElement findElement(String selector) {
		return getElement(getBy(selector));
	}
	
	public List<WebElement> findElements(String selector) {
		return getElements(getBy(selector));
	}
	
	public WebElement findElement(String selector, WebElement element) {
		return element.findElement(getBy(selector));
	}
	
	public Container find(String selector) {
		return new Container(driver, config, this.selector + ' ' + selector, getBy(selector));
	}
	
	public WebElement getElement() {
		return element;
	}
	
	public WebElement getParent() {
		return getParent("..");
	}
	
	public WebElement getParent(String path) {
		return getParent(element, path);
	}
	
	public WebElement getParent(WebElement node) {
		return getParent(node, "..");
	}
	
	public WebElement getParent(WebElement node, String path) {
		return node.findElement(By.xpath(path));
	}
	
	public Container getIFrame() {
		driver.switchTo().frame(element);
		return new Container(driver.switchTo().defaultContent(), config, "body");
	}
	
	public By getBy(String selector) {
		return getBy(selector, this.locator);
	}
	
	@SuppressWarnings("static-access")
	public By getBy(String selector, By parent) {
		By locator;
		if (selector.startsWith("xpath:")) {
			selector = selector.substring(6);
			log.debug("Find selector: " + selector);
			locator = parent.xpath(selector);
		} else {
			log.debug("Find selector: " + this.selector + ' ' + selector);
			locator = parent.cssSelector(this.selector + ' ' + selector);
		}
		return locator;
	}

	public List<WebElement> getElements(String selector) {
		return getElements(getBy(selector));
	}

	public List<WebElement> getElements(By locator) {
		List<WebElement> elements = driver.findElements(locator);
		if (elements == null || elements.size() == 0) {
			log.error("Cannot find an element for locator: " + locator + " [" + driver.getCurrentUrl() + "]");
			fail();
		}
		if (elements.size() > 1) {
			log.trace("Found elements=" + elements.size() + " for locator: " + locator);
		} else {
			log.debug("Elements (" + elements.size() + ") - " + locator);
		}
		return elements;
	}

	public WebElement getElement(String selector) {
		return getElement(getBy(selector));
	}

	public WebElement getElement(By locator) {
		List<WebElement> elements = getElements(locator);
		if (elements == null || elements.size() == 0) {
			log.warn("Cannot find an element for locator: " + locator);
			fail();
		}
		if (elements != null && elements.size() > 0) {
			if (elements.size() > 1) {
				log.warn("Found elements=" + elements.size() + " for selector: " + locator);
			}
			return elements.get(0);
		}
		return null;
	}
		
	public Object executeScript(String selector, String command, String attrName, Object value) {
		WebElement element = getElement(selector);
		if (element != null) {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			return js.executeScript(command, element, attrName, value);
		} else {
			return null;
		}
	}
	
	public void clearAllText(String selector) {
		clearText(selector, -1);
	}
	
	public void clearText(String selector) {
		clearText(selector, 0);
	}
	
	public void clearText(String selector, int index) {
		List<WebElement> elements = getElements(selector);
		if (elements != null) {
			for (int i = 0; i < elements.size(); i++) {
				if (index == -1 || index == i) {
					WebElement element = elements.get(i);
					element.clear();
				}
			}
		}
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
	
	public void waitText(String selector, String value) {
		this.waitWhenTrue(selector, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				return element.getText().equals(value);
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

	public String getText(String selector) {
		log.debug("Get Text for selector: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.getText();
		}
		return null;
	}
	
	public String getValue(String selector) {
		log.debug("Get Value for selector: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.getAttribute("value");
		}
		return null;
	}

	public Boolean validateText(String selector, String value) {
		log.debug("Validate Text value=" + value + ", for selector: " + selector);
		return validateString(getText(selector), value);
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
		return getAttributeValue(selector, "innerHTML");
	}
	
	public Boolean validateHTML(String selector, String value) {
		log.debug("Validate HTML value=" + value + ", for selector: " + selector);
		return validateString(getHTML(selector), value);
	}
	
	public Boolean assertHTML(String selector, String value) {
		log.debug("Assert HTML value=" + value + ", for selector: " + selector);
		return assertString(getHTML(selector), value);
	}

	public String getAttributeValue(String selector, String name) {
		log.debug("Get attribute=" + name + ", for selector: " + selector);
		WebElement element = getElement(selector);
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
		executeScript(selector, "arguments[0].style[arguments[1]] = arguments[2]", name, value);
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
	
	public WebElement getOptionByText(String selector, String text) {
		log.debug("OptionByText text=" + text + ", for selector: " + selector);
		WebElement select = getElement(selector);
		List<WebElement> options = select.findElements(By.tagName("option"));
		for(WebElement option : options){
	        if(option.getText().equals(text)) {
	            return option;
	        }
	    }
		return null;
	}
	
	public void selectOptionByText(String selector, String text) {
		WebElement option = getOptionByText(selector, text);
		if (option != null) {
			option.click();
		}
	}

	public void click(String selector) {
		log.debug("Click on: " + selector);
		WebElement element = waitAndFindElement(selector);
		if (element != null) {
			try {
				element.click();	
			} catch(Exception e) {
				log.error("Cannot execute click event for selector: " + selector + " [" + driver.getCurrentUrl() + "]");
				throw e;
			}
			SeleniumUtils.sleep(config.getActionDelay());
		}
	}

	public void mouseClickByLocator(String selector) {
		log.debug("Mouse Click on: " + selector);
		WebElement element = waitAndFindElement(selector);
		if (element != null) {
			try {
				Actions builder = new Actions(driver);
				builder.moveToElement(element).click(element);
				builder.perform();
			} catch(Exception e) {
				log.error("Cannot execute click event for selector: " + selector + " [" + driver.getCurrentUrl() + "]");
				throw e;
			}
			SeleniumUtils.sleep(config.getActionDelay());
		}
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
	
	public boolean isDisplayed(String selector) {
		log.debug("isEnabled: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.isDisplayed();
		}
		return false;
	}
	
	public void setBrowseFile(String path) {
		log.debug("Browse File: " + path);
		SeleniumUtils.fileBrowseDialog(driver, path);
	}

	public void waitPageLoad() {
		SeleniumUtils.wait(driver, config.getPageLoadTimeout(), null);
	}

	public void waitPageLoad(String urlPath) {
		SeleniumUtils.wait(driver, config.getPageLoadTimeout(), Pattern.compile(urlPath));
	}
	
	public void waitWhenTrue(String selector, IWaitCallback callback) {
		WebElement element = SeleniumUtils.waitAndFindElement(driver, findBy(selector), config.getPageLoadTimeout());
		for (int i = 0; i < config.getPageLoadTimeout(); i++) {
			print('.', false);
			if (callback.isTrue(element)) {
				print('.');
				return;
			}
			wait(1);
		}
		log.error("TIMEOUT: [" + driver.getCurrentUrl() + "]");
	}

	public Boolean assertString(String str1, String str2) {
		return validateString(str1, str2, true);
	}

	public Boolean validateString(String str1, String str2) {
		return validateString(str1, str2, false);
	}

	public Boolean validateString(String str1, String str2, boolean isAssert) {
		log.debug("validateString: '" + str1 + "' = '" + str2 + "'");
		boolean isTrue;
		
		if (str1 == null) {
			isTrue = str2 == null;
		} else {
			isTrue = str1.equals(str2);
			if (!isTrue) {
				isTrue = Pattern.compile(str2).matcher(str1).matches();
			}
		}
		
		if (!isTrue) {
			log.error("\n'" + str1 + "' != \n'" + str2 + "'");
		}
		
		if (isAssert) {
			assertTrue(isTrue);
		}
		
		return isTrue;
	}
	
	public WebElement waitAndFindElement() {
		return SeleniumUtils.waitAndFindElement(driver, locator, config.getPageLoadTimeout());
	}
	
	public WebElement waitAndFindElement(String selector) {
		return SeleniumUtils.waitAndFindElement(driver, findBy(selector), config.getPageLoadTimeout());
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
		if (isNewLine) {
			System.out.println(message);
		} else {
			System.out.print(message);
		}
	}
}