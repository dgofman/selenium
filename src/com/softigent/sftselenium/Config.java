package com.softigent.sftselenium;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config extends Properties {
	
	private int actionDelay;
	private int pageLoadTimeout;
		
	private static final long serialVersionUID = 238683884179262164L;
	static Logger log = Logger.getLogger(Config.class.getName());
	
	public Config(String path) {
		super();
		File file = new File("portal.properties");
		log.info("File properties path " + file.getAbsolutePath());
		try {
			this.load(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.actionDelay = Integer.parseInt(this.getProperty("action_delay"));
		this.pageLoadTimeout = Integer.parseInt(this.getProperty("load_timeout"));
	}

	public int getActionDelay() {
		return actionDelay;
	}

	public int getPageLoadTimeout() {
		return pageLoadTimeout;
	}
}
