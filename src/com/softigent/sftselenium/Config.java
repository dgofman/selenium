package com.softigent.sftselenium;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config extends Properties {
	
	private float actionDelay;
	private int pageLoadTimeout;
	private int clickButtonDelay;
	
	private String env;
	private String user;
	
	private static final long serialVersionUID = 238683884179262164L;
	static Logger log = Logger.getLogger(Config.class.getName());
	
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
		
		env = System.getProperty("env");
		if (env == null) {
			env = this.getProperty("env", false);
		}
		if (env != null) {
			env = env + "_";
		} else {
			env = "";
		}
		
		user = System.getProperty("user");
		if (user == null) {
			user = this.getProperty("user", false);
		}
		if (user != null) {
			user = user + "_";
		} else {
			user = "";
		}
		
		this.actionDelay = Float.parseFloat(this.getProperty(delayKey) != null ? this.getProperty(delayKey) : "0.5");
		this.pageLoadTimeout = Integer.parseInt(this.getProperty(timeoutKey) != null ? this.getProperty(timeoutKey) : "30");
		this.clickButtonDelay = Integer.parseInt(this.getProperty(clickDelay) != null ? this.getProperty(clickDelay) : "3");
	}
	
	@Override
	public String getProperty(String key) {
		return this.getProperty(key, true);
	}
	
	public String getProperty(String key, boolean concat) {
		if (concat == true) {
			String value = super.getProperty(env + user + key);
			if (value != null) {
				return value;
			}
			value = super.getProperty(env + key);
			if (value != null) {
				return value;
			}
		}
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

	public String getEnv() {
		return env;
	}

	public String getUser() {
		return user;
	}
}
