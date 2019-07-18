/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.log;

import com.si.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * 
 * @author wstevens
 */
public final class LogManager 
{
	private static final LogManager logManager = new LogManager();
	private static final Handler handler = new CloudHandler();
	private static final Map<Class<?>, Logger> loggers = new HashMap<Class<?>, Logger>();
	private static final Map<String, List<Logger>> categoryLoggers = new HashMap<String, List<Logger>>();
	private Level currentLevel = Level.INFO;
	
	public static LogManager manager() {
		return logManager;
	}
	
	public Logger newLogger(Class<?> clazz, Category... categories) {
		final Logger logger = newLoggerImplementation(clazz);
		logger.setHandler(handler);
		logger.setLevel(currentLevel);
		
		loggers.put(clazz, logger);
		
		for (Category category : categories) {
			final String categoryId = category.id();
			List<Logger> catLoggers = categoryLoggers.get(categoryId);
			if (catLoggers == null) {
				catLoggers = new ArrayList<Logger>();
				categoryLoggers.put(categoryId, catLoggers);
			}
			catLoggers.add(logger);
		}
		
		return logger;
	}
	
	public void setLevel(Level level) {
		this.currentLevel = level;
		
		for (Logger logger : loggers.values()) {
			logger.setLevel(currentLevel);
		}
	}
	
	public void setLevel(Level level, Category... categories) {
		for (Category category : categories) {
			List<Logger> loggers = categoryLoggers.get(category.id());
			for (Logger logger : loggers) {
				logger.setLevel(level);
			}
		}
	}
	
	public void resetAllToCurrentLevel() {
		for (Logger logger : loggers.values()) {
			logger.setLevel(currentLevel);
		}
	}

	private Logger newLoggerImplementation(Class<?> clazz) {
        Logger logger = new CategoryLogger(clazz);
		return logger;
	}
}
