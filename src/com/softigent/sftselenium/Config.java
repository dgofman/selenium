package com.softigent.sftselenium;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaDriverService;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariDriverService;
import org.openqa.selenium.safari.SafariOptions;

public class Config extends Properties {

	private static final long serialVersionUID = 238683884179262164L;
	
	protected Connector connector;
	protected DriverService driverService;
	protected MutableCapabilities capabilities;

	protected float actionDelay;
	protected File snapshotDir;
	protected int pageLoadTimeout;

	protected Point windowOffset;
	protected boolean useRobotClick;

	protected Logger log = CacheLogger.getLogger(Config.class.getName());

	protected boolean assignUserProfile = false; //true - Firefox driver initialization (clean addons WARN)
	protected boolean debugDriver = false;
	protected static boolean ignoreCaseSensitivity = false;
	protected static boolean replaceLeftToRightMark = false;
	
	//Replace [No-Break space] -> "194 160" to [Space] -> "32" 
	protected static boolean replaceNoBreakSpace = false;
	
	public static final String FIREFOX_DRIVER = "Firefox";
	public static final String CHROME_DRIVER = "Chrome";
	public static final String SAFARI_DRIVER = "Safari";
	public static final String INTERNET_EXPLORER_DRIVER = "IE";
	public static final String EDGE_DRIVER = "Edge";
	public static final String OPERA_DRIVER = "Opera";
	public static final String PHANTOMJS_DRIVER = "Phantom";

	public Config(String propertyFile) {
		super();
		this.windowOffset = new Point(0, 0);
		try {
			String absPath = getAbsolutePath(propertyFile);
			log.info("File properties path " + absPath);
			this.load(new FileInputStream(absPath));
			if ("true".equals(getProperty("create_snapshots"))) {
				snapshotDir = new File(getAbsolutePath("screenshots/snapshots/"));
				if (!snapshotDir.exists()) {
					snapshotDir.mkdirs();
				}
				FileUtils.cleanDirectory(snapshotDir);
			}
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
		this.initProperties("action_delay", "load_timeout", "use_robot_click", "ignore_case_sensitivity", "replaceNoBreakSpace", "replaceLeftToRightMark");
	}

	public void initProperties(String delayKey, String timeoutKey, String useRobotClick, String ignoreCaseSensitivity, String replaceNoBreakSpace, String replaceLeftToRightMark) {
		this.actionDelay = Float.parseFloat(this.getProperty(delayKey) != null ? this.getProperty(delayKey) : "0.5");
		this.pageLoadTimeout = Integer
				.parseInt(this.getProperty(timeoutKey) != null ? this.getProperty(timeoutKey) : "30");
		this.useRobotClick = "true".equals(this.getProperty(useRobotClick));
		Config.ignoreCaseSensitivity = "true".equals(this.getProperty(ignoreCaseSensitivity));
		Config.replaceNoBreakSpace = "true".equals(this.getProperty(replaceNoBreakSpace));
		Config.replaceLeftToRightMark  = "true".equals(this.getProperty(replaceLeftToRightMark)) || "Edge".equals(this.getProperty("driver"));
	}
	
	public String getProperty(String key) {
		String value = System.getProperty(key);
		if (value != null) {
			return value;
		}
		return super.getProperty(key);
	}

	public String getDriverName() {
		return this.getProperty("driver");
	}
	
	public void createConnector(String driverName, boolean headless) {
		String driverPath = getDriverPath(driverName,  driverName.toLowerCase() + "_driver_path");
		String driveFile = null;
		if (driverPath != null) {
			driveFile = Config.getAbsolutePath(driverPath);
			if (!new File(driveFile).exists()) {
				String[] arr = driverPath.split("/");
				if (arr.length > 3) {
					if (arr[0].equals("drivers") && arr[1].equals(Make.DRIVERS.get(driverName)[1])) {
						Properties config = new Properties();
						config.setProperty("projectDir", new File("").getAbsolutePath());
						config.setProperty("driver", driverName);
						config.setProperty("driverVersion", arr[2]);
						if (arr.length > 4) {
							config.setProperty("driverOS", arr[3]);
						}
						log.info("Config: " + config);
						Scanner in = new Scanner(System.in);
						try {
							Make.loadDrivers(in, config);
						} catch (Exception e) {
							e.printStackTrace();
						}
						in.close();
					}
				} else {
					Element.assertFail("Cannot load driver path: " + driveFile);
				}
			}
		}
		switch (driverName) {
		case FIREFOX_DRIVER:
			if (driverPath != null) {
				System.setProperty("webdriver.gecko.driver", driveFile);
			} else {
				CacheLogger.getLogger(BaseTest.class.getName())
						.info("Install FireFox version 47.0.1 or older. http://filehippo.com/download_firefox/68836");
			}
			break;
		case CHROME_DRIVER:
			System.setProperty("webdriver.chrome.driver", driveFile);
			break;
		case INTERNET_EXPLORER_DRIVER:
			System.setProperty("webdriver.ie.driver", driveFile);
			break;
		case EDGE_DRIVER:
			System.setProperty("webdriver.edge.driver", driveFile);
			break;
		case PHANTOMJS_DRIVER:
			System.setProperty("phantomjs.binary.path", driveFile);
			break;
		}
		if (connector == null) {
			connector = new Connector();
		}
		connector.init(driverName, headless, this);
	}
	
	public void createDriver() {
		if (connector.getDriver() == null) {
			WebDriver driver = getDriver(connector);
			connector.setDriver(driver);
			boolean isFullScreen = "true".equals(this.getProperty("open_fullscreen"));
			fullScreenControl(isFullScreen, connector);
		}
	}
	
	public void closeDriver() {
		connector.closeDriver();
	}

	public WebDriver getDriver(Connector connector) {
		String driverName = connector.getDriverName();
		boolean headless = connector.isHeadless();
		WebDriver driver = null;
		boolean isPrivate = "true".equals(this.getProperty("open_as_private"));
		switch (driverName) {
		case FIREFOX_DRIVER:
			FirefoxOptions fopt = new FirefoxOptions().addPreference("layout.css.devPixelsPerPx", "1.0");
			fopt.setHeadless(headless);
			createProfile(driverName, fopt);
			if (isPrivate) {
				fopt.addPreference("browser.privatebrowsing.autostart", true);
			}
			fopt.addPreference("browser.tabs.remote.autostart", false);
			fopt.addPreference("browser.tabs.remote.autostart.2", false);
			fopt.setAcceptInsecureCerts(true);
			driver = createDriver(driverName, new GeckoDriverService.Builder().usingFirefoxBinary(fopt.getBinary()).build(), fopt);
			break;
		case CHROME_DRIVER:
			ChromeOptions copt = new ChromeOptions();
			copt.setHeadless(headless);
			copt.addArguments("--no-sandbox"); // Bypass OS security model, MUST BE THE VERY FIRST OPTION
			copt.addArguments("--no-default-browser-check");
			copt.addArguments("disable-infobars");
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
			driver = createDriver(driverName, ChromeDriverService.createDefaultService(), copt);
			break;
		case SAFARI_DRIVER:
			// driver = createDriver(driverName, DesiredCapabilities.safari());
			SafariOptions sopts = new SafariOptions();
			driver = createDriver(driverName, SafariDriverService.createDefaultService(sopts), sopts);
			break;
		case EDGE_DRIVER:
			DesiredCapabilities edge = new DesiredCapabilities();
			edge.setBrowserName("MicrosoftEdge");
			edge.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			edge.setJavascriptEnabled(true);
			driver = createDriver(driverName, EdgeDriverService.createDefaultService(), new EdgeOptions().merge(edge));
			break;
		case OPERA_DRIVER:
			driver = createDriver(driverName, OperaDriverService.createDefaultService(), new OperaOptions());
			break;
		case INTERNET_EXPLORER_DRIVER:
			DesiredCapabilities iedc = DesiredCapabilities.internetExplorer();
			iedc.setCapability("initialBrowserUrl", "about:blank");
			iedc.setCapability("disable-popup-blocking", true);
			iedc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			iedc.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			iedc.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);  
			iedc.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS);
			iedc.setCapability(InternetExplorerDriver.NATIVE_EVENTS, true);
			iedc.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, true);
			iedc.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
			iedc.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
			iedc.setJavascriptEnabled(true); 
			InternetExplorerOptions iop = new InternetExplorerOptions(iedc);
			driver = createDriver(driverName, new InternetExplorerDriverService.Builder().build(), iop);
			break;
		case PHANTOMJS_DRIVER:
			DesiredCapabilities pdc = new DesiredCapabilities();
			pdc.setJavascriptEnabled(true);
			pdc.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new  String[] {
				"--webdriver-loglevel=NONE"
			});
			pdc.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
			pdc.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, System.getProperty("phantomjs.binary.path"));
			driver = createDriver(driverName, PhantomJSDriverService.createDefaultService(pdc), pdc);
			((PhantomJSDriver) driver).executePhantomJS("var data = {}; \n" +
			"this.onResourceRequested = function (req) {\n" +
			"    data[req.id] = req; \n" +
			"};\n" +
			"this.onResourceReceived = function (res) {\n" +
			"    if (res.contentType == 'application/json; charset=utf-8') {\n" +
			"        console.log('\u001b[1;35mREST ' + data[res.id].method + '::' + res.url + ' - ' + res.status);\n" +
			"        if (res.status < 200 || res.status > 206) {\n" +
			"            console.log('\u001b[1;34mRequest:\u001b[0;30m' + JSON.stringify(data[res.id], undefined, 2));\n" +
			"            console.log('\u001b[1;31mResponse:\u001b[0;30m' + JSON.stringify(res, undefined, 2));\n" +
			"        }\n" +
			"    }\n" +
			"    delete data[res.id];\n" +
			"};", new Object[] {});
			break;
		}
		log.info("getDriver: " + driverName);
		return driver;
	}
	
	public void createProfile(String driverName, MutableCapabilities capabilities) {
		if (assignUserProfile) {
			switch (driverName) {
			case FIREFOX_DRIVER:
				FirefoxOptions fopt = (FirefoxOptions) capabilities;
				//To create a new test Profile: (Windows->R, type: "firefox.exe -p" and press ENTER)
				FirefoxProfile profile = new ProfilesIni().getProfile("SeleniumProfile");
				if (profile == null) {
					log.info("Create user: SeleniumProfile");
					try {
						String os = System.getProperty("os.name").toLowerCase();
						Process process;
						if (os.startsWith("win")) {
							process = Runtime.getRuntime().exec(new String[] {"cmd", "/c", "\"%ProgramFiles%/Mozilla Firefox/firefox.exe\" -CreateProfile SeleniumProfile"});
						} else {
							process = Runtime.getRuntime().exec(new String[] {"firefox -CreateProfile SeleniumProfile"});
						}
						Thread.sleep(1000);
						process.destroy();
						if (os.startsWith("win")) {
							process = Runtime.getRuntime().exec(new String[] {"cmd", "/c", "\"%ProgramFiles%/Mozilla Firefox/firefox.exe\" -headless -foreground -P SeleniumProfile"});
						} else {
							process = Runtime.getRuntime().exec(new String[] {"firefox -headless -foreground -P SeleniumProfile"});
						}
						Thread.sleep(3000);
						process.destroy();
						profile = new ProfilesIni().getProfile("SeleniumProfile");
						fopt.addPreference("firefox_profile", "SeleniumProfile");
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					} finally {
						if (profile == null) {
							profile = new FirefoxProfile();
						}
					}
				}
				fopt.setProfile(profile);
				if (debugDriver) {
					profile.setPreference("security.sandbox.content.level", 1);
					fopt.setLogLevel(FirefoxDriverLogLevel.TRACE);
				}
			}
		}
	}

	public WebDriver createDriver(String driverName, DriverService driverService, MutableCapabilities capabilities) {
		log.info("createDriver: " + driverName);
		this.driverService = driverService;
		this.capabilities = capabilities;
		switch (driverName) {
		case FIREFOX_DRIVER:
			return new FirefoxDriver((GeckoDriverService) driverService, (FirefoxOptions) capabilities);
		case CHROME_DRIVER:
			return new ChromeDriver((ChromeDriverService) driverService, (ChromeOptions) capabilities);
		case SAFARI_DRIVER:
			return new SafariDriver((SafariDriverService) driverService, (SafariOptions) capabilities);
		case INTERNET_EXPLORER_DRIVER:
			return new InternetExplorerDriver((InternetExplorerDriverService) driverService, (InternetExplorerOptions) capabilities);
		case EDGE_DRIVER:
			return new EdgeDriver((EdgeDriverService) driverService, (EdgeOptions) capabilities);
		case OPERA_DRIVER:
			return new OperaDriver((OperaDriverService) driverService, (OperaOptions) capabilities);
		case PHANTOMJS_DRIVER:
			return  new PhantomJSDriver((PhantomJSDriverService) driverService, capabilities);
		}
		return null;
	}

	public WebDriver createDriver(String driverName, MutableCapabilities capabilities) {
		this.driverService = null;
		this.capabilities = capabilities;
		switch (driverName) {
		case FIREFOX_DRIVER:
			return new FirefoxDriver((FirefoxOptions) capabilities);
		case CHROME_DRIVER:
			return new ChromeDriver((ChromeOptions) capabilities);
		case SAFARI_DRIVER:
			return new SafariDriver((SafariOptions) capabilities);
		case INTERNET_EXPLORER_DRIVER:
			return new InternetExplorerDriver((InternetExplorerOptions) capabilities);
		case EDGE_DRIVER:
			return new EdgeDriver((EdgeOptions) capabilities);
		case OPERA_DRIVER:
			return new OperaDriver((OperaOptions) capabilities);
		case PHANTOMJS_DRIVER:
			return new PhantomJSDriver(capabilities);
		}
		return null;
	}

	public void fullScreenControl(boolean isFullScreen, Connector connector) {
		String driverName = connector.getDriverName();
		WebDriver driver = connector.getDriver();
		if (isFullScreen) {
			if (!driverName.equals(CHROME_DRIVER)) {
				driver.manage().window().maximize();
			}
		} else if (this.getProperty("window_dimension") != null) {
			if (driverName.equals(INTERNET_EXPLORER_DRIVER)) {
				this.getWindowOffset().move(10, 8); // IE browser border
			} else if (driverName.equals(CHROME_DRIVER)) {
				this.getWindowOffset().move(8, 8); // Chrome browser border	
			} else if (!driverName.equals(PHANTOMJS_DRIVER)) {
				this.getWindowOffset().move(10, 5); // FireFox browser border
			}
			String[] wh = this.getProperty("window_dimension").split("x");
			if (wh.length == 2) {
				Dimension d = new Dimension(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
				driver.manage().window().setSize(d);
			}
		}
	}

	public String getDriverPath(String driverName, String driverPathKey) {
		String path = getProperty(driverPathKey);
		log.info("Driver Name: " + driverName + ", Path: " + path);
		return path;
	}

	public void actionDelay() {
		createSnapshot();
		Element.mlsWait((int)(actionDelay * 1000), true);
	}

	public void createSnapshot() {
		if (snapshotDir != null) {
			File file = new File(snapshotDir, snapshotDir.list().length + ".png");
			log.info(file.getAbsolutePath());
			SeleniumUtils.screenshot(connector.getDriver(), file);
		}
	}

	public DriverService getDriverService() {
		return driverService;
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
	
	public static boolean ignoreCaseSensitivity() {
		return Config.ignoreCaseSensitivity;
	} 

	public static boolean replaceNoBreakSpace() {
		return Config.replaceNoBreakSpace;
	}
	
	public static boolean replaceLeftToRightMark() {
		return Config.replaceLeftToRightMark;
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

	public Connector getConnector() {
		return connector;
	}
	
	public MutableCapabilities getCapabilities() {
		return capabilities;
	}

	public static String os() {
		String os = System.getProperty("os.name").toLowerCase().substring(0, 3);
		if ("lin".equals(os) || "nix".equals(os) || "nux".equals(os) || "aix".equals(os) || "sunos".equals(os)) {
			os = "lin";
		}
		return os;
	}
}