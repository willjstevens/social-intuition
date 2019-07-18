package com.si.service;

import com.si.Category;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Standard implementation for account operations.
 *
 * @author wstevens
 */
@Service
public class ValidationService
{	
	private static final Logger logger = LogManager.manager().newLogger(ValidationService.class, Category.SERVICE_VALIDATION);
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);		

	public boolean isNull(String value) {
		boolean isNull = value == null;
		if (logger.isFiner()) {
			logger.finer("isNull result for string \"%s\" is: %b.", value, isNull);
		}
		return isNull;
	}

	public boolean isNotNull(String value) {
		boolean isNotNull = value != null;
		if (logger.isFiner()) {
			logger.finer("isNotNull result for string \"%s\" is: %b.", value, isNotNull);
		}
		return isNotNull;
	}

	public boolean isEmpty(String value) {
		boolean isEmpty = false;
		if (value == null) {
			throw new IllegalArgumentException("Value cannot be null.");
		}
		isEmpty = value.isEmpty();
		if (logger.isFiner()) {
			logger.finer("isEmpty result for string \"%s\" is: %b.", value, isEmpty);
		}
		return isEmpty;
	}

	public boolean isNotEmpty(String value) {
		boolean isNotEmpty = false;
		if (value == null) {
			throw new IllegalArgumentException("Value cannot be null.");
		}
		isNotEmpty = !value.isEmpty();
		if (logger.isFiner()) {
			logger.finer("isNotEmpty result for string \"%s\" is: %b.", value, isNotEmpty);
		}
		return isNotEmpty;
	}

	public boolean isNullOrEmpty(String value) {
		boolean isNullOrEmpty = false;
		if (value == null || value.isEmpty()) {
			isNullOrEmpty = true;
		}
		if (logger.isFiner()) {
			logger.finer("isNullOrEmpty result for string \"%s\" is: %b.", value, isNullOrEmpty);
		}
		return isNullOrEmpty;
	}

	public boolean isNotNullAndNotEmpty(String value) {
		boolean isNotNullAndNotEmpty = false;
		if (value != null && !value.isEmpty()) {
			isNotNullAndNotEmpty = true;
		}
		if (logger.isFiner()) {
			logger.finer("isNotNullAndNotEmpty result for string \"%s\" is: %b.", value, isNotNullAndNotEmpty);
		}
		return isNotNullAndNotEmpty;
	}

	public boolean isValidSize(String value, int minSize, int maxSize) {
		boolean isValidSize = false;
		final int valLength = value.length();
		if (value != null && valLength >= minSize && valLength <= maxSize) {
			isValidSize = true;
		}
		if (logger.isFiner()) {
			logger.finer("isValidSize result for string \"%s\" (length %d) with minSize %d and maxSize %d is: %b.", value, valLength, minSize, maxSize, isValidSize);
		}
		return isValidSize;
	}

	public boolean isUnderMinSize(String value, int minSize) {
		boolean isUnderMinSize = false;
		if (value != null && value.length() < minSize) {
			isUnderMinSize = true;
		}
		if (logger.isFiner()) {
			logger.finer("isUnderMinSize result for string \"%s\" and limit %d is: %b.", value, minSize, isUnderMinSize);
		}
		return isUnderMinSize;
	}

	public boolean isOverMaxSize(String value, int maxSize) {
		boolean isOverMaxSize = false;
		if (value != null && value.length() > maxSize) {
			isOverMaxSize = true;
		}
		if (logger.isFiner()) {
			logger.finer("isOverMaxSize result for string \"%s\" and limit %d is: %b.", value, maxSize, isOverMaxSize);
		}
		return isOverMaxSize;
	}

	public boolean containsSpaces(String value) {
		boolean containsSpaces = false;
		assertNotNull(value);
		containsSpaces = value.contains(" ");
		if (logger.isFiner()) {
			logger.finer("containsSpaces result for string \"%s\" is: %b.", value, containsSpaces);
		}
		return containsSpaces;
	}

	public boolean isValidEmailFormat(String email) {
		boolean isValid = false;
		Matcher matcher = emailPattern.matcher(email);
		isValid = matcher.matches();
		if (logger.isFiner()) {
			logger.finer("Email address validation result for string \"%s\" is: %b.", email, isValid);
		}
		return isValid;
	}

	public boolean isValidDateFormat(String dateString, String format) {
		boolean isValidDateFormat = false;
		final DateFormat formatter = new SimpleDateFormat(format);
		try {
			formatter.parse(dateString);
			isValidDateFormat = true;
		} catch (ParseException swallow) {}
		if (logger.isFiner()) {
			logger.finer("isValidDateFormat result for date string \"%s\" and format string \"%s\" is: %b.", dateString, format, isValidDateFormat);
		}
		return isValidDateFormat;
	}

	public void assertNotNull(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
	}

	public void assertNotNullAndNotEmpty(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("Value cannot be null or empty");
		}
	}

	public boolean isValidFormat(String value, String regex) {
		boolean isValidFormat = false;
		if (value != null && value.matches(regex)) {
			isValidFormat = true;
		}
		if (logger.isFiner()) {
			logger.finer("isValidFormat result for string \"%s\" and regex string \"%s\" is: %b.", value, regex, isValidFormat);
		}
		return isValidFormat;
	}
}
