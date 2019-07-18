/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.log;


/**
 * 
 *
 * @author wstevens
 */
final class CategoryLogger implements Logger
{
	private Handler handler;
	private Class<?> clazz;

	// optimizations
	private boolean isInfo;
	private boolean isFine;
	private boolean isFiner;
	private boolean isFinest;
	
	CategoryLogger(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void error(String message, Throwable throwable) {
		handler.error(clazz, message, throwable);
	}

	@Override
	public void error(String message, Throwable throwable, Object... args) {
		handler.error(clazz, message, throwable, args);
	}

	@Override
	public void warn(String message) {
		handler.warn(clazz, message);
	}

	@Override
	public void warn(String message, Object... args) {
		handler.warn(clazz, message, args);
	}

	@Override
	public void info(String message) {
		if (isInfo()) {
			handler.info(clazz, message);
		}
	}

	@Override
	public void info(String message, Object... args) {
		if (isInfo) {
			handler.info(clazz, message, args);
		}
	}

	@Override
	public void fine(String message) {
		if (isFine) {
			handler.fine(clazz, message);
		}
	}

	@Override
	public void fine(String message, Object... args) {
		if (isFine) {
			handler.fine(clazz, message, args);
		}
	}

	@Override
	public void finer(String message) {
		if (isFiner) {
			handler.finer(clazz, message);
		}
	}

	@Override
	public void finer(String message, Object... args) {
		if (isFiner) {
			handler.finer(clazz, message, args);
		}
	}

	@Override
	public void finest(String message) {
		if (isFinest) {
			handler.finest(clazz, message);
		}
	}

	@Override
	public void finest(String message, Object... args) {
		if (isFinest) {
			handler.finest(clazz, message, args);
		}
	}

	@Override
	public boolean isInfo() {
		return isInfo;
	}

	@Override
	public boolean isFine() {
		return isFine;
	}

	@Override
	public boolean isFiner() {
		return isFiner;
	}

	@Override
	public boolean isFinest() {
		return isFinest;
	}

	@Override
	public void setLevel(Level level) {
		isInfo 		= level.isLevelEnabled(Level.INFO);
		isFine 		= level.isLevelEnabled(Level.FINE);
		isFiner 	= level.isLevelEnabled(Level.FINER);
		isFinest 	= level.isLevelEnabled(Level.FINEST);
	}

	@Override
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
}
