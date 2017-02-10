package com.softigent.sftselenium;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

public class Config extends Properties {
	
	private static final long serialVersionUID = 238683884179262164L;
	
	protected float actionDelay;
	protected int pageLoadTimeout;

	protected Point windowOffset;
	protected boolean useRobotClick;
	
	protected Logger log = CacheLogger.getLogger(Config.class.getName());
	
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
	
	public Connector createConnector(String driverName) {
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
		return SeleniumUtils.getDriver(driverName, this);
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