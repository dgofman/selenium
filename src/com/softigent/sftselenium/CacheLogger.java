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

	private static final CacheLoggerFactory loggerFactory = new CacheLoggerFactory();
	
	private static final Deque<String> lastMessages = new ArrayDeque<String>();
	
	public CacheLogger(String name) {
		super(name);
	}

	public static Logger getLogger(String name) {
		return Logger.getLogger(name, loggerFactory); 
	}

	@Override
	public void callAppenders(LoggingEvent event) {
		super.callAppenders(event);
		if (lastMessages.size() == MAX_STACK_SIZE) {
			lastMessages.removeFirst();
		}
		for(Category c = this; c != null; c=c.getParent()) {
			AppenderSkeleton app = (AppenderSkeleton)c.getAppender("file");
			if (app != null) {
				if(app.isAsSevereAsThreshold(event.getLevel())) {
					lastMessages.offer(app.getLayout().format(event));
					return;
				}
			}
		}
		lastMessages.offer(event.getLevel() + ": " + event.getMessage());
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