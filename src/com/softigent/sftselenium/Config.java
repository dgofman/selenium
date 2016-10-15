package com.softigent.sftselenium;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openqa.selenium.Point;

public class Config extends Properties {
	
	private float actionDelay;
	private int pageLoadTimeout;

	private IConfig iConfig;
		
	private Point windowOffset;
	private boolean useRobotClick;
	
	private static final long serialVersionUID = 238683884179262164L;
	static Logger log = CacheLogger.getLogger(Config.class.getName());
	
	public Config(String propertyFile) {
		this(propertyFile, "action_delay", "load_timeout", "use_robot_click");
	}

	public Config(String propertyFile, String delayKey, String timeoutKey, String useRobotClick) {
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
		
		this.windowOffset = new Point(0, 0);
		this.actionDelay = Float.parseFloat(this.getProperty(delayKey) != null ? this.getProperty(delayKey) : "0.5");
		this.pageLoadTimeout = Integer.parseInt(this.getProperty(timeoutKey) != null ? this.getProperty(timeoutKey) : "30");
		this.useRobotClick = "true".equals(this.getProperty(useRobotClick));
	}

	@Override
	public String getProperty(String key) {
		if (iConfig != null) {
			return iConfig.getProperty(key);
		}
		return super.getProperty(key);
	}

	public String getSuperProperty(String key) {
		return super.getProperty(key);
	}

	public float getActionDelay() {
		return actionDelay;
	}
	
	public int getPageLoadTimeout() {
		return pageLoadTimeout;
	}

	public IConfig getiConfig() {
		return iConfig;
	}

	public void setiConfig(IConfig iConfig) {
		this.iConfig = iConfig;
	}
	
	public Point getWindowOffset() {
		return windowOffset;
	}
	
	public boolean isRobotClick() {
		return useRobotClick;
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