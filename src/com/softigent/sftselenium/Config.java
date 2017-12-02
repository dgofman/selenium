package com.softigent.sftselenium;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

public class Config extends Properties {
	
	private static final long serialVersionUID = 238683884179262164L;
	
	protected float actionDelay;
	protected int pageLoadTimeout;

	protected Point windowOffset;
	protected boolean useRobotClick;
	
	protected Logger log = CacheLogger.getLogger(Config.class.getName());
	
	private static String driverName;
	
	public Config(String propertyFile) {
		super();
		try {
			String absPath = getAbsolutePath(propertyFile);
			log.info("File properties path " + absPath);
			this.load(new FileInputStream(absPath));
		} catch (Exception e1) {
			try {
				log.info("File properties path " + Config.class.getResource('/' + propertyFile).getPath());
				this.load(Config.class.getResourceAsStream('/' + propertyFile));
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	public void setDefaultProperties() {
		this.initProperties("action_delay", "load_timeout", "use_robot_click");
	}
	
	public void initProperties(String delayKey, String timeoutKey, String useRobotClick) {
		this.windowOffset = new Point(0, 0);
		this.actionDelay = Float.parseFloat(this.getProperty(delayKey) != null ? this.getProperty(delayKey) : "0.5");
		this.pageLoadTimeout = Integer.parseInt(this.getProperty(timeoutKey) != null ? this.getProperty(timeoutKey) : "30");
		this.useRobotClick = "true".equals(this.getProperty(useRobotClick));
	}
	
	public void setDriverName(String driverName) {
		Config.driverName = driverName;
	}
	
	public static final String getDriverName() {
		return Config.driverName;
	}
	
	public Connector createConnector(String driverName) {
		setDriverName(driverName);
		String driverPath;
		if (driverName.equals("Firefox")) {
			driverPath = getDriverPath(driverName, "firefox_driver_path");
			if (driverPath != null) {
				System.setProperty("webdriver.gecko.driver", Config.getAbsolutePath(driverPath));
			} else {
				CacheLogger.getLogger(BaseTest.class.getName()).info("Install FireFox version 47.0.1 or older. http://filehippo.com/download_firefox/68836");
			}
		} else if (driverName.equals("Chrome")) {
			driverPath = getDriverPath(driverName, "chrome_driver_path");
			if (driverPath != null) {
				System.setProperty("webdriver.chrome.driver", Config.getAbsolutePath(driverPath));
			}
		} else if (driverName.equals("IE")) {
			driverPath = getDriverPath(driverName, "ie_driver_path");
			if (driverPath != null) {
				System.setProperty("webdriver.ie.driver", Config.getAbsolutePath(driverPath));
			}
		}

		return new Connector(getDriver(driverName), this);
	}
	
	public WebDriver getDriver(String driverName) {
		WebDriver driver = null;
		boolean isPrivate = "true".equals(this.getProperty("open_as_private"));
		boolean isFullScreen = "true".equals(this.getProperty("open_fullscreen"));
		if (driverName.equals("Firefox")) {
			FirefoxOptions options = new FirefoxOptions()
				.addPreference("layout.css.devPixelsPerPx", "1.0");
			if (isPrivate) {
				options.addPreference("browser.privatebrowsing.autostart", true);
			}
			driver = createDriver(driverName, options);
		} else if (driverName.equals("Chrome")) {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("disable-infobars");
			options.addArguments("--disable-extensions");
			options.addArguments("--disable-notifications");
			options.addArguments("--start-maximized");
			options.addArguments("--disable-web-security");
			options.addArguments("--no-proxy-server");
			options.addArguments("--enable-automation");
			options.addArguments("--disable-save-password-bubble");
			
			Map<String, Object> prefs = new HashMap<String, Object>();
			prefs.put("credentials_enable_service", false);
			prefs.put("profile.password_manager_enabled", false);
			options.setExperimentalOption("prefs", prefs);

			/*LoggingPreferences logPrefs = new LoggingPreferences();
			logPrefs.enable(LogType.BROWSER, Level.ALL);
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
			if (isFullScreen) {
				options.addArguments("--start-maximized");
			}
			if (isPrivate) {
				capabilities.setCapability("chrome.switches", Arrays.asList("--incognito"));
			}
			capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);*/
			driver = createDriver(driverName, options);
		} else if (driverName.equals("Safari")) {
			//driver = createDriver(driverName, DesiredCapabilities.safari());
			driver = createDriver(driverName, new SafariOptions());
		} else if (driverName.equals("IE")) {
			DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
			dc.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			dc.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
			dc.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
			
			if (isPrivate) {
				dc.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true); 
				dc.setCapability(InternetExplorerDriver.IE_SWITCHES, "-private");
			}
			driver = createDriver(driverName, new InternetExplorerOptions(dc));
		}

		fullScreenControl(isFullScreen, driver, driverName);
		
		return driver;
	}
	
	public WebDriver createDriver(String driverName, MutableCapabilities capabilities) {
		switch (driverName) {
			case "Firefox":
				return new FirefoxDriver((FirefoxOptions)capabilities);
			case "Chrome":
				return new ChromeDriver((ChromeOptions)capabilities);
			case "Safari":
				return new SafariDriver((SafariOptions)capabilities);
			case "IE":
				return new InternetExplorerDriver((InternetExplorerOptions)capabilities);
		}
		return null;
	}
	
	public void fullScreenControl(boolean isFullScreen, WebDriver driver, String driverName) {
		if (isFullScreen) {
			if (!driverName.equals("Chrome")) {
				driver.manage().window().maximize();
			}
		} else if (this.getProperty("window_dimension") != null) {
			if (driverName.equals("IE")) {
				this.getWindowOffset().move(10, 8); //IE browser border
			} else if (driverName.equals("Chrome")) {
				this.getWindowOffset().move(8, 8); //Chrome browser border
			} else {
				this.getWindowOffset().move(10, 5); //FireFox browser border
			}
			String[] wh = this.getProperty("window_dimension").split("x");
			if (wh.length == 2) {
				Dimension d = new Dimension(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
				driver.manage().window().setSize(d);
			}
		}
	}
	
	public String getDriverPath(String driverName, String driverPathKey) { 
		return getProperty(driverPathKey);
	}

	public float getActionDelay() {
		return actionDelay;
	}
	
	public int getPageLoadTimeout() {
		return pageLoadTimeout;
	}
	
	public Point getWindowOffset() {
		return windowOffset;
	}
	
	public boolean isRobotClick() {
		return useRobotClick;
	}
	
	public void setRobotClick(boolean useRobotClick) {
		this.useRobotClick = useRobotClick;
	}
	
	public static File getFile(String path) {
		String parentDirectory = System.getProperty("parentDir");
		if (parentDirectory != null) {
			try {
				return new File(new File(parentDirectory).getCanonicalPath(), path);
			} catch (IOException e) {
				return new File(parentDirectory, path);
			}
		} else {
			return new File(path);
		}
	}
	
	public static String getAbsolutePath(String path) {
		return getFile(path).getAbsolutePath();
	}
}