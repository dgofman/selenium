package com.softigent.sftselenium;

import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@RunWith(ExtendedRunner.class)
public abstract class BaseTest {

	protected final Config config;
	protected final Connector connector;
	protected final Logger log;
	protected final String className;
	
	protected Container body;
	protected String testHandleId;
	
	@Rule
	public ErrorCollector assertErrorCollector = new ErrorCollector();

	public enum CloseDriver { 
	    NEVER, ALWAYS, PASSED; 
	} 
	
	protected CloseDriver doCloseDriver;
	
	public static final double GIT_VERSION = 8.2;
	public static final int GIT_PATCH = 0;

	@Rule
	public TestName testName = new TestName();
	
	public static void verifySeleniumVersion(double version) {
		if (version > GIT_VERSION) {
			String error = "Unsupported major.minor version " + GIT_VERSION + ".\nGet latest changes from https://github.com/dgofman/selenium";
			System.err.println(error);
			throw new UnsupportedClassVersionError(error);
		}
	}
	
	public void validateIgnoreTests(String[] ignoreTests) {
		for (String test : ignoreTests) {
			if (test.equals(testName.getMethodName())) {
				assumeTrue(false);
				break;
			}
		}
	}

	public BaseTest(Config config) {
		this.config = config;
		this.connector = createConnector(config.getDriverName(), "true".equals(System.getProperty("headless")));
		preInitialize(connector);
		className = getClass().getName();

		String logger_stack = connector.getConfig().getProperty("logger_stack_size");
		if (logger_stack != null && !Double.isNaN(Double.valueOf(logger_stack))) {
			CacheLogger.MAX_STACK_SIZE = Integer.parseInt(logger_stack);
		}
		this.log = initialize();
	}
	
	@Before
	protected void setUp() throws Exception {
		if (connector.getDriver() == null) {
			createDriver();
		}
		createBody();
		testHandleId = getWindowHandle();
		doCloseDriver = CloseDriver.ALWAYS;
	}
	
	@After
	public void tearDown() throws Exception {
		if (Element.assertErrorCollector != null) {
			for (Throwable error : Element.assertErrorCollector) {
				assertErrorCollector.addError(error);
			}
			Element.assertErrorCollector = null;
		}
	}
	
	protected void createDriver() {
		config.createDriver();
	}
	
	protected void closeDriver() {
		config.closeDriver();
	}
	
	public Connector createConnector(String driverName, boolean headless) {
		if (config.getConnector() == null) {
			config.createConnector(driverName, headless);
		} else {
			Connector connector = config.getConnector();
			connector.closeDriver();
			config.createConnector(driverName, headless);
		}
		return config.getConnector();
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

	protected Logger initialize() {
		return CacheLogger.getLogger(className);
	}

	public Container createContainer(String selector) {
		return new Container(connector.getDriver(), connector.getConfig(), selector);
	}
	
	public Container createContainer(WebElement element) {
		return new Container(connector.getDriver(), connector.getConfig(), element);
	}
	
	public Container createContainer(WebElement element, String xpath) {
		return new Container(connector.getDriver(), connector.getConfig(), Container.getParent(element, xpath));
	}
	
	public Container createContainer(List<WebElement> elements, int index) {
		Container container = createContainer(elements.get(index));
		container.selector += ":nth-child(" + (index + 1) + ")";
		return container;
	}

	public Container popupWindowByURL(String urlRegex) {
		return popupWindow(getWindowHandleByURL(urlRegex));
	}

	public Container popupWindowByTitle(String titleRegExp) {
		return popupWindow(getWindowHandleByTitle(titleRegExp));
	}

	public Container popupWindow(List<WebDriverInfo> popups) {
		if (popups.size() > 0) {
			return createWindowContainer(switchWindow(popups.get(0)));
		} else {
			return null;
		}
	}

	public Container createWindowContainer(String winHandle) {
		return createWindowContainer(switchWindow(winHandle));
	}
	
	public Container createWindowContainer(WebDriver driver) {
		return new Container(driver, connector.getConfig(), "body");
	}
	
	public void waitPageLoadByTitle(String title) {
		this.waitPageLoadByTitle(title, connector.getConfig().getPageLoadTimeout());
	}
	
	public void waitPageLoadByTitle(String title, int timeoutSec) {
		WebDriverWait wait = new WebDriverWait(connector.getDriver(), timeoutSec);
		wait.until(ExpectedConditions.titleContains(title));
	}
	
	public void waitPageLoad() {
		waitPageLoad(null);
	}

	public void waitPageLoad(String urlPath) {
		waitPageLoad(urlPath, true);
	}
	
	public void waitPageLoad(String urlPath, boolean isRegExp) {
		waitPageLoad(urlPath, connector.getConfig().getPageLoadTimeout(), isRegExp);
	}
	
	public void waitPageLoad(String urlPath, int timeoutSec, boolean isRegExp) {
		log.info("Wait page load: " + urlPath);
		SeleniumUtils.wait(connector.getDriver(), timeoutSec, urlPath, isRegExp);
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
	
	public void  createBody() {
		body = createContainer("body");
	}
	
	public Container getBody() {
		return body;
	}

	public void openPage(String url, boolean trafficListener) {
		log.info("Opening " + url);
		connector.getDriver().get(getURL(url));
		config.createSnapshot();
		if (trafficListener && 
			!(connector.getDriver() instanceof PhantomJSDriver) &&
			!(connector.getDriver() instanceof InternetExplorerDriver)) {
			JavascriptExecutor js = (JavascriptExecutor) connector.getDriver();
			SeleniumUtils.createXMLHTTPTrafficListener(js, trafficListener(js));
		}
	}

	protected Runnable trafficListener(final JavascriptExecutor js) {
		String originalURL = connector.getDriver().getCurrentUrl();
		return new Runnable() {
			@Override
			public void run() {
				try {
					while(originalURL != null && connector != null && connector.getDriver() != null &&
							originalURL.equals(connector.getDriver().getCurrentUrl())) {
						try {
							Thread.sleep(1000);
							@SuppressWarnings("unchecked")
							List<List<?>> responses = (List<List<?>>)js.executeScript("return window.SeleniumXMLHttpRequest.get()");
							responses.forEach(res -> {
								logTrafficResponse(res);
							});
						} catch (Exception e) {
						}
					}
				} catch (WebDriverException e) {}
			}
		};
	}
	
	protected void logTrafficResponse(List<?> res) {
		Long status = (Long) res.get(0);
		if (status < 200 || status > 206) {
			System.err.println("\u001b[1;35mREST " + res.get(1) + "::" + res.get(2) + " - " + status + "\n" +
			"\u001b[1;34mRequest:\u001b[0;30m " + SeleniumUtils.prettyJson(res.get(3)) + "\n" + 
			"\u001b[1;31mResponse:\u001b[0;30m " + SeleniumUtils.prettyJson(res.get(4)));
		} else {
			System.out.println("\u001b[1;35mREST " + res.get(1) + "::" + res.get(2) + " - " + status);
		}
	}

	public void gotoURL(String path) {
		String url = getCurrentURL();
		url = getURL(url.substring(0,  url.indexOf('/', url.indexOf("//") + 2)) + path);
		log.info("To URL " + url);
		connector.getDriver().navigate().to(url);
		config.createSnapshot();
	}
	
	public void gotoPage(String pageName) {
		String url = getCurrentURL();
		url = getURL(url.substring(0,  url.lastIndexOf('/')) + pageName);
		log.info("To Page " + url);
		connector.getDriver().navigate().to(url);
		config.createSnapshot();
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
	
	public List<WebDriverInfo> getWindowHandleByURL(String urlRegex) {
		return getWindowHandle(urlRegex, null);
	}
	
	public List<WebDriverInfo> getWindowHandleByTitle(String titleRegExp) {
		return getWindowHandle(null, titleRegExp);
	}
	
	public List<WebDriverInfo> getWindowHandle(String urlRegex, String titleRegExp) {
		return getWindowHandle(urlRegex, titleRegExp, 0);
	}
	
	public List<WebDriverInfo> getWindowHandle(String urlRegex, String titleRegExp, int index) {
		final WebDriver driver = connector.getDriver();
		final String winHandler = getWindowHandle();
		driver.getWindowHandles();
		Set<String> handles = driver.getWindowHandles();
		if (handles.size() == 1 && config.getCapabilities() instanceof InternetExplorerOptions) {
			log.warn("Uncheck: Internet Options -> Security -> Enable Protected Mode");
		}
		List<WebDriverInfo> list = new ArrayList<WebDriverInfo>();
		for (String handle : handles) {
			WebDriver win = driver.switchTo().window(handle);
			if ((urlRegex != null && Container.compareString(win.getCurrentUrl(), urlRegex)) ||
				(titleRegExp != null && Container.compareString(win.getTitle(), titleRegExp))) {
				list.add(new WebDriverInfo(win));
			}
		}
		driver.switchTo().window(winHandler);
		if (list.size() == 0 && index < 20) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			return getWindowHandle(urlRegex, titleRegExp, ++index);
		}
		return list;
	}
	
	public String waitWindowHandleByURL(String regex) {
		WaitCallback waitCallBack = new WaitCallback() {
			public boolean isTrue() {
				List<WebDriverInfo> list = getWindowHandleByURL(regex);
				if (list.size() > 0) {
					value = list.get(0).handler;
					return true;
				}
				return false;
			}
		};
		body.waitWhenTrue(waitCallBack);
		return (String)waitCallBack.getValue();
	}
	
	public WebDriver switchWindow(WebDriverInfo info) {
		return connector.getDriver().switchTo().window(info.handler);
	}

	public WebDriver switchWindow(String winHandle) {
		return connector.getDriver().switchTo().window(winHandle);
	}
	
	public void closeWindow(String winHandle) {
		connector.getDriver().switchTo().window(winHandle).close();
	}
	
	public void setWindowSize(int width, int height) { 
		setWindowSize(new Dimension(width, height));
	}
	
	public void setWindowSize(Dimension dimension) { 
		connector.getDriver().manage().window().setSize(dimension);
	}
	
	public void fullWindow() {
		connector.getDriver().manage().window().maximize();
	}
	
	public void restoreWindow() {
		String[] wh = connector.getConfig().getProperty("window_dimension").split("x");
		if (wh.length == 2) {
			setWindowSize(new Dimension(Integer.parseInt(wh[0]), Integer.parseInt(wh[1])));
		}
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

	public void tab() {
		connector.getDriver().switchTo().activeElement();
	}
	
	public Config getConfig() {
		return config;
	}
	
	public Connector getConnector() {
		return connector;
	}

	@Rule
	public TestWatcher testWatcher = new TestWatcher() {
		
		@Override
		protected void starting(final Description description) {
			log.info(">>>>> Start " + description.getClassName() + "::" + description.getMethodName());
		}
		
		@Override
		protected void succeeded(Description description) {
			log.info(">>>>> succeeded doCloseDriver::" + doCloseDriver);
			if (doCloseDriver != CloseDriver.NEVER) {
				closeDriver();
			}
		}


		@Override
		protected void failed(Throwable e, Description description) {
			doFailed(e, description);
			log.info(">>>>> failed doCloseDriver::" + doCloseDriver);
			if (e instanceof org.openqa.selenium.NoSuchWindowException ||
				e instanceof org.openqa.selenium.WebDriverException) {
				connector.setDriver(null);
			} else if (doCloseDriver == CloseDriver.ALWAYS) {
				closeDriver();
			}
		}

		@Override
		protected void finished(final Description description) {
			log.info(">>>>> End " + description.getClassName() + "::" + description.getMethodName());
		}
	};
	
	public static class WebDriverInfo {
		public final String title;
		public final String url;
		public final String source;
		public final String handler;
		public final Set<String> handlers;
		
		public WebDriverInfo(WebDriver driver) {
			url = driver.getCurrentUrl();
			source = driver.getPageSource();
			title = driver.getTitle();
			handler = driver.getWindowHandle();
			handlers = driver.getWindowHandles();
		}
	}
}