package com.softigent.sftselenium;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggingEvent;

public class CacheLogger extends Logger {
	
	public static int MAX_STACK_SIZE = 5;
	public static boolean SKIP_LOGS = false;

	private static final CacheLoggerFactory loggerFactory = new CacheLoggerFactory();
	
	private static final Deque<String> lastMessages = new ArrayDeque<String>();
	
	private AppenderSkeleton fileAppender;
	private boolean searchAppender = true;
	
	public CacheLogger(String name) {
		super(name);
		SKIP_LOGS = false;
	}

	public static Logger getLogger(String name) {
		return Logger.getLogger(name, loggerFactory); 
	}

	@Override
	public void callAppenders(LoggingEvent event) {
		if (!SKIP_LOGS) {
			super.callAppenders(event);
			if (lastMessages.size() == MAX_STACK_SIZE) {
				lastMessages.removeFirst();
			}
			if (searchAppender && fileAppender == null) {
				searchAppender = false;
				for(Category c = this; c != null; c=c.getParent()) {
					synchronized(c) {
						fileAppender = (AppenderSkeleton)c.getAppender("file");
						if (fileAppender != null) {
							break;
						}
					}
				}
			}
			if (fileAppender != null) {
				if(fileAppender.isAsSevereAsThreshold(event.getLevel())) {
					lastMessages.offer(fileAppender.getLayout().format(event));
				}
			} else {
				lastMessages.offer(event.getLevel() + ": " + event.getMessage());
			}
		}
	}
	
	public static Deque<String> getLastMessages() {
		return lastMessages;
	}
}

class CacheLoggerFactory implements LoggerFactory {

	public CacheLoggerFactory() {
	}

	public Logger makeNewLoggerInstance(String name) {
		return new CacheLogger(name);
	}
}