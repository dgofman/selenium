package com.softigent.sftselenium;

import static org.junit.Assume.assumeTrue;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@FixMethodOrder(MethodSorters.JVM)
public abstract class BaseTest {
	
	protected Connector connector;

	protected Logger log;
	protected String className;
	
	public static final double GIT_VERSION = 1.4;

	@Rule
	public TestWatcher testWatchThis = new TestWatcher() {
		@Override
		protected void failed(Throwable e, Description description) {
			doFailed(e, description);
		}
	};
	
	@Rule
	public TestName testName = new TestName();
	
	public static void verifySeleniumVersion(double version) {
		if (version < GIT_VERSION) {
			String error = "Unsupported major.minor version " + GIT_VERSION + ".\nGet latest changes from https://github.com/dgofman/selenium";
			System.err.println(error);
			throw new UnsupportedClassVersionError(error);
		}
	}
	
	public void validateIgnoreTests(String[] ignoreTests) throws Exception {
		for (String test : ignoreTests) {
			if (test.equals(testName.getMethodName())) {
				assumeTrue(false);
				break;
			}
		}
	}

	public final Container body;

	public BaseTest(Connector connector) {
		if (connector == null) {
			throw new NullPointerException("Please initialize a default connector.");
		}
		preInitialize(connector);
		this.connector = connector;
		this.className = getClass().getName();
		this.body = createContainer("body");
		
		String logger_stack = connector.getConfig().getProperty("logger_stack_size");
		if (logger_stack != null && !Double.isNaN(Double.valueOf(logger_stack))) {
			CacheLogger.MAX_STACK_SIZE = Integer.parseInt(logger_stack);
		}
		initialize();
	}

	protected String doFailed(Throwable e, Description description) {
		log.error("BaseTest::doFailed", e);
		return String.join("\n", CacheLogger.getLastMessages());
	}
	
	protected void preInitialize(Connector connector) {
		WebDriver driver = connector.getDriver();
		try {
			driver.getCurrentUrl();
		} catch (Exception e1) {
			try {
				driver.switchTo().alert().accept();
			} catch (Exception e2) {}
		}
	}

	protected void initialize() {
		log = CacheLogger.getLogger(className);
	}
	
	public static Connector createConnector(Config config, String driverName) {
		String driverPath = config.getProperty("firefox_driver_path");
		if (driverPath != null) {
			System.setProperty("webdriver.gecko.driver", Config.getAbsolutePath(driverPath));
		} else {
			CacheLogger.getLogger(BaseTest.class.getName()).info("Install FireFox version 47.0.1 or older. http://filehippo.com/download_firefox/68836");
		}

		config.getProperty("chrome_driver_path");
		if (driverPath != null) {
			System.setProperty("webdriver.chrome.driver", Config.getAbsolutePath(driverPath));
		}
		
		driverPath = config.getProperty("ie_driver_path");
		if (driverPath != null) {
			System.setProperty("webdriver.ie.driver", Config.getAbsolutePath(driverPath));
		}

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
	
	public Container createWindowContainer(String windowTitle) {
		return new Container(switchWindow(windowTitle), connector.getConfig(), "body");
	}
	
	public void waitPageLoad() {
		waitPageLoad(null);
	}

	public void waitPageLoad(String urlPath) {
		waitPageLoad(urlPath, connector.getConfig().getPageLoadTimeout());
	}
	
	public void waitPageLoad(String urlPath, int timeoutSec) {
		SeleniumUtils.wait(connector.getDriver(), timeoutSec, urlPath != null ? Pattern.compile(urlPath) : null);
	}

	/*
	 * script - FakeDate.prototype.__proto__.getTimezoneOffset = function() { return 0; }
	 */
	public Object execFakeDate(String script) {
		return executeScript("function FakeDate() {" +
			"var args = Array.prototype.slice.call(arguments); " +
			"args.unshift(null); return new (Function.prototype.bind.apply(FakeDate.originalDate, args));} " +
			"FakeDate.prototype.__proto__ = Date.prototype; FakeDate.originalDate = Date; Date = FakeDate; " + 
			script);
	}

	public Object executeScript(String command) {
		return this.executeScript(command, null);
	}

	public Object executeScript(String command, Object element) {
		JavascriptExecutor js = (JavascriptExecutor) body.getDriver();
		if (element == null) {
			return js.executeScript(command);
		} else {
			return js.executeScript(command, element);
		}
	}
	
	public String getCurrentURL() {
		return connector.getDriver().getCurrentUrl();
	}
	
	public String getURL(String url) {
		return url;
	}

	public void openPage(String url) {
		log.info("Opening " + url);
		connector.getDriver().get(getURL(url));
	}

	public void gotoURL(String path) {
		String url = getCurrentURL();
		url = getURL(url.substring(0,  url.indexOf('/', url.indexOf("//") + 2)) + path);
		log.info("To URL " + url);
		connector.getDriver().navigate().to(url);
	}
	
	public void gotoPage(String pageName) {
		String url = getCurrentURL();
		url = getURL(url.substring(0,  url.lastIndexOf('/')) + pageName);
		log.info("To Page " + url);
		connector.getDriver().navigate().to(url);
	}
	
	public void waitForLoad() {
		new WebDriverWait(connector.getDriver(), 30).until((ExpectedCondition<Boolean>) wd ->
			((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
	}

	public void addCookies(String name, String value) {
		connector.getDriver().manage().addCookie(new Cookie(name, value));
	}
	
	public void deleteCookie(String name) {
		connector.getDriver().manage().deleteCookieNamed(name);
	}
	
	public void deleteAllCookies() {
		connector.getDriver().manage().deleteAllCookies();
	}
	
	public void closeBrowser() {
		connector.getDriver().quit();
	}
	
	public void closeOtherTabs() {
		String currentTab = connector.getDriver().getWindowHandle();
		for(String winHandle : connector.getDriver().getWindowHandles()) {
			if (!winHandle.equals(currentTab)) {
				connector.getDriver().switchTo().window(winHandle).close();
			}
		}
		connector.getDriver().switchTo().window(currentTab);
	}
	
	public Set<String> getWindowHandles() {
		return connector.getDriver().getWindowHandles();
	}
	
	public String getWindowHandle() {
		return connector.getDriver().getWindowHandle();
	}
	
	public String getWindowHandleByURL(String regex) {
		WebDriver driver = connector.getDriver();
		Set<String> windows = driver.getWindowHandles();
	
		for (String window : windows) {
			try {
				driver.switchTo().window(window);
				Boolean isTrue = Container.compareString(driver.getCurrentUrl(), regex);
				if (isTrue) {
					return window;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public WebDriver switchWindow(String winHandle) {
		return connector.getDriver().switchTo().window(winHandle);
	}
	
	public void closeWindow(String winHandle) {
		connector.getDriver().switchTo().window(winHandle).close();
	}
	
	/**
		startThread(new Runnable() {
			public void run() {
				//TO DO
			}
		});
	 */
	public void startThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setName("com.softigent.sftselenium::BaseTest");
		thread.start();
	}

	public static void print(Object message) {
		System.out.println(message);
	}
	
	@Rule
	public TestWatcher testWatcher = new TestWatcher() {
		@Override
		protected void starting(final Description description) {
			log.info(">>>>> Start " + description.getClassName() + "::" + description.getMethodName());
		}

		@Override
		protected void finished(final Description description) {
			log.info(">>>>> End " + description.getClassName() + "::" + description.getMethodName());
		}
	};
}