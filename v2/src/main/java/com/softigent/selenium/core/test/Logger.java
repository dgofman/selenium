package com.softigent.selenium.core.test;

public class Logger {

	private ch.qos.logback.classic.Logger slf4jLogger;
	private org.apache.logging.log4j.Logger log4jLogging;
	private org.apache.log4j.Logger log4jLogger;
	
	public static Logger initRoot() {
		Logger l = new Logger();
		try {
			Class.forName("ch.qos.logback.classic.Logger"); //throw an exception if class not exists
			l.slf4jLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("ROOT");
			l.slf4jLogger.setLevel(ch.qos.logback.classic.Level.INFO);
		} catch (Throwable ex) {
			try {
				org.apache.logging.log4j.core.config.Configurator.setRootLevel(org.apache.logging.log4j.Level.INFO);
				l.log4jLogging = org.apache.logging.log4j.LogManager.getRootLogger();
			} catch (Throwable ex2) {
				l.log4jLogger = org.apache.log4j.LogManager.getRootLogger();
				l.log4jLogger.setLevel(org.apache.log4j.Level.INFO);
			}
		}
		return l;
	}
	
	public void info(String msg) {
		if (slf4jLogger != null) {
			slf4jLogger.info(msg);
		} else if (log4jLogging != null) {
			log4jLogging.info(msg);
		} else {
			log4jLogger.info(msg);
		}
	}
	
	public void debug(String msg) {
		if (slf4jLogger != null) {
			slf4jLogger.debug(msg);
		} else if (log4jLogging != null) {
			log4jLogging.debug(msg);
		} else {
			log4jLogger.debug(msg);
		}
	}
	
	public void warn(String msg) {
		if (slf4jLogger != null) {
			slf4jLogger.warn(msg);
		} else if (log4jLogging != null) {
			log4jLogging.warn(msg);
		} else {
			log4jLogger.warn(msg);
		}
	}
	
	public void error(String msg) {
		if (slf4jLogger != null) {
			slf4jLogger.error(msg);
		} else if (log4jLogging != null) {
			log4jLogging.error(msg);
		} else {
			log4jLogger.error(msg);
		}
	}
}
