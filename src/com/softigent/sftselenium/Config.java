package com.softigent.sftselenium;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config extends Properties {
	
	private float actionDelay;
	private int pageLoadTimeout;
	private int clickButtonDelay;
		
	private static final long serialVersionUID = 238683884179262164L;
	static Logger log = Logger.getLogger(Config.class.getName());
	
	public Config(File propertyFile) {
		this(propertyFile, "action_delay", "click_delay", "load_timeout");
	}

	public Config(File propertyFile, String delayKey, String clickDelay,  String timeoutKey) {
		super();
		log.info("File properties path " + propertyFile.getAbsolutePath());
		try {
			this.load(new FileInputStream(propertyFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.actionDelay = Float.parseFloat(this.getProperty(delayKey) != null ? this.getProperty(delayKey) : "0.5");
		this.pageLoadTimeout = Integer.parseInt(this.getProperty(timeoutKey) != null ? this.getProperty(timeoutKey) : "30");
		this.clickButtonDelay = Integer.parseInt(this.getProperty(clickDelay) != null ? this.getProperty(clickDelay) : "3");
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
}
