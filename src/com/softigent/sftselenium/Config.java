package com.softigent.sftselenium;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config extends Properties {
	
	private float actionDelay;
	private int pageLoadTimeout;
	private int clickButtonDelay;
	private boolean isJqueryClick;

	private IConfig iConfig;
		
	private static final long serialVersionUID = 238683884179262164L;
	static Logger log = CacheLogger.getLogger(Config.class.getName());
	
	public Config(String propertyFile) {
		this(propertyFile, "action_delay", "click_delay", "load_timeout");
	}

	public Config(String propertyFile, String delayKey, String clickDelay,  String timeoutKey) {
		super();
		try {
			log.info("File properties path " + new File(propertyFile).getAbsolutePath());
			this.load(new FileInputStream(propertyFile));
		} catch (Exception e1) {
			try {
				log.info("File properties path " + Config.class.getResource('/' + propertyFile).getPath());
				this.load(Config.class.getResourceAsStream('/' + propertyFile));
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		
		this.actionDelay = Float.parseFloat(this.getProperty(delayKey) != null ? this.getProperty(delayKey) : "0.5");
		this.pageLoadTimeout = Integer.parseInt(this.getProperty(timeoutKey) != null ? this.getProperty(timeoutKey) : "30");
		this.clickButtonDelay = Integer.parseInt(this.getProperty(clickDelay) != null ? this.getProperty(clickDelay) : "3");
		this.isJqueryClick = "true".equals(this.getProperty("use_jquery_click"));
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
	
	public int getClickDelay() {
		return clickButtonDelay;
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

	public boolean isJqueryClick() {
		return isJqueryClick;
	}
}
