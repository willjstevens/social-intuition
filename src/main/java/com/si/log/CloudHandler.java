package com.si.log;

import com.si.service.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.PrintStream;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static com.si.log.Level.*;

/**
 * In most cloud PAAS environments, there are not physical logs but rather commands to 
 * execute to see the log tail.  The cloud providers funnel standard logs (slf4, log4j)
 * and standard out/err to the same "log".  Therefore this handler writes to standard 
 * out and standard err.
 *
 * @author wstevens
 */
final class CloudHandler implements Handler
{
	private static final DateTimeFormatter DISPLAY_TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	@Autowired private UtilityService utilityService;
	private PrintStream out = System.out;
	private PrintStream err = System.err;
	
	CloudHandler() {}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#error(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void error(Class<?> clazz, String message, Throwable throwable) {
		err(clazz, ERROR, message, throwable);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#error(java.lang.String, java.lang.Throwable, java.lang.Object[])
	 */
	@Override
	public void error(Class<?> clazz, String message, Throwable throwable, Object... args) {
		err(clazz, ERROR, message, throwable, args);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#warn(java.lang.String)
	 */
	@Override
	public void warn(Class<?> clazz, String message) {
		out(clazz, WARN, message);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#warn(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void warn(Class<?> clazz, String message, Object... args) {
		out(clazz, WARN, message, args);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#info(java.lang.String)
	 */
	@Override
	public void info(Class<?> clazz, String message) {
		out(clazz, INFO, message);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#info(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void info(Class<?> clazz, String message, Object... args) {
		out(clazz, INFO, message, args);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#fine(java.lang.String)
	 */
	@Override
	public void fine(Class<?> clazz, String message) {
		out(clazz, FINE, message);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#fine(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void fine(Class<?> clazz, String message, Object... args) {
		out(clazz, FINE, message, args);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#finer(java.lang.String)
	 */
	@Override
	public void finer(Class<?> clazz, String message) {
		out(clazz, FINER, message);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#finer(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void finer(Class<?> clazz, String message, Object... args) {
		out(clazz, FINER, message, args);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#finest(java.lang.String)
	 */
	@Override
	public void finest(Class<?> clazz, String message) {
		out(clazz, FINEST, message);
	}

	/* (non-Javadoc)
	 * @see com.valloc.log.Handler#finest(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void finest(Class<?> clazz, String message, Object... args) {
		out(clazz, FINEST, message, args);
	}

	private String getDisplayTimestampString() {
		OffsetDateTime now = OffsetDateTime.now(Clock.systemUTC());
		return DISPLAY_TIMESTAMP_FORMATTER.format(now);
	}

	private void out(Class<?> clazz, Level level, String msg, Object... args) {
		out(clazz, level, String.format(msg, args));
	}
	
	private void out(Class<?> clazz, Level level, String msg) {
		out.println(String.format("%s %s %s: %s", getDisplayTimestampString(), clazz.getSimpleName(), level, msg));
	}
	
	private void err(Class<?> clazz, Level level, String msg, Throwable t) {
		err.println(String.format("%s %s %s: %s", getDisplayTimestampString(), clazz.getSimpleName(), level, msg));
		t.printStackTrace(err);
	}
	
	private void err(Class<?> clazz, Level level, String msg, Throwable t, Object... args) {
		err(clazz, level, String.format(msg, args), t);
	}
}
