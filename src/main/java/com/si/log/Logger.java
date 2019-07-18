/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.log;


/**
*
*
* 
* @author wstevens
*/
public interface Logger 
{
	public void error(String message, Throwable throwable);
	public void error(String message, Throwable throwable, Object... args);
	public void warn(String message);
	public void warn(String message, Object... args);
	public void info(String message);
	public void info(String message, Object... args);
	public void fine(String message);
	public void fine(String message, Object... args);
	public void finer(String message);
	public void finer(String message, Object... args);
	public void finest(String message);
	public void finest(String message, Object... args);
	
	public boolean isInfo();
	public boolean isFine();
	public boolean isFiner();
	public boolean isFinest();
	
	void setLevel(Level level);
	void setHandler(Handler handler);
}
