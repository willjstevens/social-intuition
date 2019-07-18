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
interface Handler 
{
	void error(Class<?> clazz, String message, Throwable throwable);
	void error(Class<?> clazz, String message, Throwable throwable, Object... args);
	void warn(Class<?> clazz, String message);
	void warn(Class<?> clazz, String message, Object... args);
	void info(Class<?> clazz, String message);
	void info(Class<?> clazz, String message, Object... args);
	void fine(Class<?> clazz, String message);
	void fine(Class<?> clazz, String message, Object... args);
	void finer(Class<?> clazz, String message);
	void finer(Class<?> clazz, String message, Object... args);
	void finest(Class<?> clazz, String message);
	void finest(Class<?> clazz, String message, Object... args);
}
