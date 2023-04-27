package com.softigent.selenium.ui.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Connector {
	private WebDriver driver;
	private Config config;
	private String driverName;
	
	public static final String FIREFOX_DRIVER = "Firefox";
	public static final String CHROME_DRIVER = "Chrome";
	public static final String SAFARI_DRIVER = "Safari";
	public static final String INTERNET_EXPLORER_DRIVER = "IE";
	public static final String EDGE_DRIVER = "Edge";

	protected Logger log = Logger.getLogger(Connector.class.getName());

	public void init(String driverName, Config config) {
		this.driverName = driverName;
		this.config = config;
	}

	public void closeDriver() {
		if (driver != null) {
			log.debug("Close Driver");
			driver.close();
			log.debug("Quit Driver");
			try {
				driver.quit();
			} catch (Exception e) {
			}
			driver = null;
		}
	}

	public Config getConfig() {
		return config;
	}

	public String getDriverName() {
		return driverName;
	}
	
	public WebDriver getDriver() {
		return driver;
	}
	
	public void resetDriver() {
		driver = null;
	}

	public boolean isHeadless() {
		return "true".equals(config.getProperty("headless"));
	}
	
	public void createDriver() {
		if (driver == null) {
			driver = initWebDriver();
			boolean isFullScreen = "true".equals(config.getProperty("open_fullscreen"));
			fullScreenControl(isFullScreen);
		}
	}
	
	public WebDriver initWebDriver() {
		boolean headless = isHeadless();
		WebDriver driver = null;
		switch (driverName) {
		case FIREFOX_DRIVER:
			FirefoxOptions fopt = new FirefoxOptions().addPreference("layout.css.devPixelsPerPx", "1.0");
			fopt.setHeadless(headless);
			fopt.addPreference("browser.tabs.remote.autostart", false);
			fopt.addPreference("browser.tabs.remote.autostart.2", false);
			fopt.setAcceptInsecureCerts(true);
			WebDriverManager.firefoxdriver().setup();
	        driver = new FirefoxDriver(fopt);
			break;
		case CHROME_DRIVER:
			ChromeOptions copt = new ChromeOptions();
			copt.setHeadless(headless);
			copt.addArguments("--no-sandbox"); // Bypass OS security model, MUST BE THE VERY FIRST OPTION
			copt.addArguments("--no-default-browser-check");
			copt.addArguments("disable-infobars");
			copt.addArguments("--allowed-ips");
			copt.addArguments("--disable-extensions");
			copt.addArguments("--disable-notifications");
			copt.addArguments("--start-maximized");
			copt.addArguments("--disable-web-security");
			copt.addArguments("--no-proxy-server");
			copt.addArguments("--enable-automation");
			copt.addArguments("--disable-save-password-bubble");
			//copt.setExperimentalOption("useAutomationExtension", false);
			//copt.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems

			Map<String, Object> prefs = new HashMap<String, Object>();
			prefs.put("credentials_enable_service", false);
			prefs.put("profile.password_manager_enabled", false);
			copt.setExperimentalOption("prefs", prefs);

			/*
			 * LoggingPreferences logPrefs = new LoggingPreferences();
			 * logPrefs.enable(LogType.BROWSER, Level.ALL); DesiredCapabilities capabilities
			 * = DesiredCapabilities.chrome(); if (isFullScreen) {
			 * options.addArguments("--start-maximized"); } if (isPrivate) {
			 * capabilities.setCapability("chrome.switches", Arrays.asList("--incognito"));
			 * } capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
			 * capabilities.setCapability(ChromeOptions.CAPABILITY, options);
			 */
			WebDriverManager.chromedriver().setup();
			driver = new ChromeDriver(copt);
			break;
		case SAFARI_DRIVER:
			WebDriverManager.safaridriver().setup();
			driver = new SafariDriver();
			break;
		case EDGE_DRIVER:
			DesiredCapabilities edge = new DesiredCapabilities();
			edge.setBrowserName("MicrosoftEdge");
			//edge.setCapability(org.openqa.selenium.remote.CapabilityType.ACCEPT_SSL_CERTS, true);
			//edge.setJavascriptEnabled(true);
			WebDriverManager.edgedriver().setup();
			driver = new EdgeDriver(new EdgeOptions().merge(edge));
			break;
		case INTERNET_EXPLORER_DRIVER:
			WebDriverManager.iedriver().setup();
			driver = new InternetExplorerDriver();
			break;
		}
		log.info("getDriver: " + driverName);
		return driver;
	}
	
	public void fullScreenControl(boolean isFullScreen) {
		if (isFullScreen) {
			if (!driverName.equals(CHROME_DRIVER)) {
				driver.manage().window().maximize();
			}
		} else if (config.getProperty("window_dimension") != null) {
			if (driverName.equals(INTERNET_EXPLORER_DRIVER)) {
				config.getWindowOffset().moveBy(10, 8); // IE browser border
			} else if (driverName.equals(CHROME_DRIVER)) {
				config.getWindowOffset().moveBy(8, 8); // Chrome browser border	
			}
			String[] wh = config.getProperty("window_dimension").split("x");
			if (wh.length == 2) {
				Dimension d = new Dimension(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
				driver.manage().window().setPosition(new Point(0, 0));
				driver.manage().window().setSize(d);
			}
		}
	}

}