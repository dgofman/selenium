package com.softigent.sftselenium;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config extends Properties {
	
	private float actionDelay;
	private int pageLoadTimeout;
		
	private static final long serialVersionUID = 238683884179262164L;
	static Logger log = Logger.getLogger(Config.class.getName());
	
	public Config(File propertyFile) {
		this(propertyFile, "action_delay", "load_timeout");
	}

	public Config(File propertyFile, String delayKey, String timeoutKey) {
		super();
		log.info("File properties path " + propertyFile.getAbsolutePath());
		try {
			this.load(new FileInputStream(propertyFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.actionDelay = Float.parseFloat(this.getProperty(delayKey));
		this.pageLoadTimeout = Integer.parseInt(this.getProperty(timeoutKey));
	}

	public float getActionDelay() {
		return actionDelay;
	}

	public int getPageLoadTimeout() {
		return pageLoadTimeout;
	}
}
