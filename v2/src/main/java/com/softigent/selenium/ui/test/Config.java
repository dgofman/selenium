package com.softigent.selenium.ui.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Point;

public class Config extends Properties {

	private static final long serialVersionUID = -1;
	
	protected Connector connector;
	
	protected String env;
	protected float actionDelay;
	protected File snapshotDir;
	protected int pageLoadTimeout;

	protected Point windowOffset;
	protected boolean useRobotClick;

	protected Logger log = Logger.getLogger(Config.class.getName());

	protected boolean assignUserProfile = false; //true - Firefox driver initialization (clean addons WARN)
	protected boolean debugDriver = false;
	protected static boolean ignoreCaseSensitivity = false;
	protected static boolean replaceLeftToRightMark = false;
	
	//Replace [No-Break space] -> "194 160" to [Space] -> "32" 
	protected static boolean replaceNoBreakSpace = false;
		
	public Config() {
		this.windowOffset = new Point(0, 0);
	}

	public Config(String propertyFile) {
		super();
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
		
		env = getProperty("env");
		if (env != null) {
			env = env + "_";
		} else {
			env = "";
		}
	}
	
	public Config setProperty(String key, String value) {
		super.setProperty(key, value);
		return this;
	}
	
	public Config initDefaultProperties() {
		return initDefaultProperties(Connector.CHROME_DRIVER);
	}

	public Config initDefaultProperties(String driverName) {
		this.setProperty("driver", driverName);
		this.actionDelay = Float.parseFloat(getProperty("action_delay", "0.5"));
		this.pageLoadTimeout = Integer.parseInt(this.getProperty("load_timeout", "30"));
		this.useRobotClick = "true".equals(this.getProperty("use_robot_click", "false"));
		Config.ignoreCaseSensitivity = "true".equals(this.getProperty("ignore_case_sensitivity", "false"));
		Config.replaceNoBreakSpace = "true".equals(this.getProperty("replaceNoBreakSpace", "false"));
		Config.replaceLeftToRightMark = "true".equals(this.getProperty("replaceLeftToRightMark", "false")) || "Edge".equals(this.getProperty("driver"));
		return this;
	}
	
	public String getProperty(String key, String defaultValue) {
		String value = System.getProperty(env + key);
		if (value != null) {
			return value;
		}
		value = super.getProperty(env + key);
		if (value != null) {
			return value;
		}
		value = System.getProperty(key);
		if (value != null) {
			return value;
		}
		return super.getProperty(key, defaultValue);
	}

	public String getDriverName() {
		return this.getProperty("driver");
	}
	
	public void createConnector(String driverName) {
		if (connector == null) {
			connector = new Connector();
		}
		connector.init(driverName, this);
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
		return path != null ? getFile(path).getAbsolutePath() : null;
	}

	public Connector getConnector() {
		return connector;
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
}