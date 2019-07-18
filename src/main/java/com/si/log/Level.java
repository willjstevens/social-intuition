/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.log;

/**
 * Defines various log levels assocated for the logger.
 *
 * The standard for using different log levels is as follows:
 * <ul>
 *  <li>error: Indicates something severe has occurred and application task cannot continue normally.</li>
 *  <li>warning: Indicates something unanticpated has occurred, but application can recover.</li>
 *  <li>info: Basic information such as beginning a task and successful/error completion of a task.</li>
 *  <li>fine: Detail statements indicating basic troubleshooting information and info dumps on operations.</li>
 *  <li>finer: Essentially tracing statements, mostly in/throughout complex operations.</li>
 * 	<li>finest: Detail of displaying entire data contents, no matter how big. This might come in the form of a
 * 		file's/object's entire contents.</li>
 * </ul>
 *
 * @author wstevens
 */
public enum Level
{
	OFF,
	ERROR,
	WARN,
	INFO,
	FINE,
	FINER,
	FINEST;

	
	boolean isLevelEnabled(Level targetLevel) {
		boolean isLevelEnabled = false;
		
		final int thisLevelOrdinal = ordinal();
		final int targetLevelOrdinal = targetLevel.ordinal();
		if (thisLevelOrdinal >= targetLevelOrdinal) {
			isLevelEnabled = true;
		}
		
		return isLevelEnabled;
	}
	
	public static Level toLogLevel(final String logLevelStr)
	{
		Level retval = FINEST; // if unparseable, report as ALL for troubleshooting
		
		if (logLevelStr.equals(OFF.toString())) {
			retval = OFF;
		} else if (logLevelStr.equals(ERROR.toString())) {
			retval = ERROR;
		} else if (logLevelStr.equals(WARN.toString())) {
			retval = WARN;
		} else if (logLevelStr.equals(INFO.toString())) {
			retval = INFO;
		} else if (logLevelStr.equals(FINE.toString())) {
			retval = FINE;
		} else if (logLevelStr.equals(FINER.toString())) {
			retval = FINER;
		} else if (logLevelStr.equals(FINEST.toString())) {
			retval = FINEST;
		} 
		
		return retval;
	}
}
