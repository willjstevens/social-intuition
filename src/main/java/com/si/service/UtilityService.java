/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.si.Category;
import com.si.entity.Intuition;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

import static com.si.Constants.*;

/**
 * 
 *
 * @author wstevens
 */
@Service
public class UtilityService
{
	private static final Logger logger = LogManager.manager().newLogger(UtilityService.class, Category.UTILITY);
    private static final String DISPLAY_TIMESTAMP_PATTERN = "MMMM d, y 'at' h:mm a";
    private static final DateTimeFormatter DISPLAY_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_TIMESTAMP_PATTERN);
    private static final DateTimeFormatter PERSISTENT_TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final PrettyTime displayPrettyTimeFormatter = new PrettyTime();
	private static final String DEFAULT_URL_ENCODING = "UTF-8";
    @Autowired private ResourceLoader resourceLoader;
	@Autowired
    @Lazy
    private ConfigurationService configurationService;
	private Properties emailProps = new Properties();
	private Session emailSession;
	private String senderEmail;
    private boolean isInitialized;

	public void init() {
        initEmailSending();
        isInitialized = true;
	}

	public void initEmailSending() {
		String emailConnectionString = configurationService.getEmailConnectionString();
		if (logger.isInfo()) {
			logger.info("Initializing email sending with connection string \"%s\".", emailConnectionString);
		}
		emailProps.clear();
		// split string into key/value pairs (i.e. format: mail.smtp.host=smtp.gmail.com,mail.smtp.port=587)
		String[] keyValuePairs = null;
		try {
			keyValuePairs = emailConnectionString.split(",");
		} catch (PatternSyntaxException e) {
			logger.error("Could not parse the email connection string into key/value pairs by commas; string is: \"%s\".", e, emailConnectionString);
			throw e;
		}
        String senderUsername = null;
		String senderPassword = null;
		for (String keyValuePair : keyValuePairs) {
			String[] keyValuePairArray = null;
			try {
				keyValuePairArray = keyValuePair.split("=");
			} catch (PatternSyntaxException e) {
				logger.error("Could not parse the email key/value pair by equals sign; string is: \"%s\".", e, keyValuePair);
				throw e;
			}
			String key = keyValuePairArray[0];
			String value = keyValuePairArray[1];
			if (logger.isFine()) {
				logger.fine("Attempting to load email key/value pair configuration: key=%s, value=%s", key, value);
			}
			//mail.smtp.host=smtp.gmail.com,mail.smtp.port=587,mail.smtp.starttls.enable=true,mail.smtp.auth=true,mail.debug=true,sender.email=vallocllc@gmail.com,sender.password=valloc123#
			if ("sender.email".equals(key)) {
				senderEmail = value;
			} else if ("sender.username".equals(key)) {
                senderUsername = value;
            } else if ("sender.password".equals(key)) {
				senderPassword = value;
			} else { // misc configuration for properties file
				emailProps.put(key, value);
			}
		}
		// next set authenticator info
        final String finalizedUsername = senderUsername != null ? senderUsername : senderEmail;
		final String finalizedSenderEmail = senderEmail, finalizedSenderPassword = senderPassword;
	    emailSession = Session.getInstance(emailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication () {
                return new PasswordAuthentication(finalizedUsername, finalizedSenderPassword);
            }
        });
	}

	@Async
	public void sendHtmlEmail(String recipient, String subject, String body) {
		try {
			if (logger.isFiner()) {
				logger.finer("About to send an email to recipient=%s and subject=\"%s\".", recipient, subject);
			}
			if (logger.isFinest()) {
				logger.finest("The body of the HTML email for recipient=%s and subject=\"%s\" will contain the following: ", recipient, subject);
				logger.finest(body);
			}
	        MimeMessage msg = new MimeMessage(emailSession);
            msg.setFrom(new InternetAddress(senderEmail)) ;
	        msg.setRecipients(Message.RecipientType.TO, recipient);
	        msg.setSubject(subject);
	        msg.setContent(body, "text/html; charset=utf-8");
	        Transport.send(msg);
	        if (logger.isFiner()) {
				logger.finer("Successfully sent HTML email to recipient %s.", recipient);
			}
	     } catch (Exception e) {
	        logger.error("Failed to send HTML email message to recipient %s due to %s.", e, recipient, e);
	     }
	}

    public boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    public Cookie newSessionCookie(String cookieValue) {
        Cookie cookie = new Cookie(COOKIE_SESSION_KEY, cookieValue);
        cookie.setSecure(true);
        cookie.setMaxAge(configurationService.getCookieExpirySeconds());
        cookie.setPath(FORWARD_SLASH_STR);
        return cookie;
    }

    public void deleteSessionCookie(Cookie sessionCookie) {
        sessionCookie.setMaxAge(0);
        sessionCookie.setSecure(true);
        sessionCookie.setPath(FORWARD_SLASH_STR);
    }

    public Cookie findSessionCookie(Cookie[] cookies) {
        Cookie cookie = null;
        if (cookies != null) { // could be null if user cleared all cookies
            for (Cookie candidateCookie : cookies) {
                if (candidateCookie.getName().equals(COOKIE_SESSION_KEY)) {
                    cookie = candidateCookie;
                    break;
                }
            }
        }
        return cookie;
    }

    public boolean isOverSsl(HttpServletRequest request) {
        // first check right off request for local dev environments
        boolean isSecure = request.getScheme().toLowerCase().startsWith(SECURE_SCHEME);
        // if still not secure, check for provider environments like Heroku which forward indicator in request header
        if (!isSecure) {
            if (logger.isFiner()) {
                logger.finer("Request isSecure found to be %b with scheme of %s. Now attempting provider's settings.", request.isSecure(), request.getScheme());
            }
            // Give final shot at pulling it from Heroku headers
            String providerForwardHeader = request.getHeader(configurationService.getProviderSecureRequestHeaderName());
            if (providerForwardHeader != null && providerForwardHeader.equalsIgnoreCase(SECURE_SCHEME)) {
                isSecure = true;
            }
        }
//        if (logger.isFinest()) {
//            logger.finest("Returning isSecure value of %b for URI %s.", isSecure, request.getRequestURI());
//        }
        return isSecure;
    }

    public void redirect(HttpServletResponse response, String redirectUrl, int statusCode) {
        String encodedRedirectUrl = response.encodeRedirectURL(redirectUrl);
        if (logger.isFiner()) {
            logger.finer("Encoded original redirect URL (%s) into: %s.", redirectUrl, encodedRedirectUrl);
        }
        response.setStatus(statusCode); // found since URL is here but being temporarily redirected
        try {
            response.sendRedirect(encodedRedirectUrl);
            if (logger.isFine()) {
                logger.fine("Successfully redirected with %d to: %s.", statusCode, encodedRedirectUrl);
            }
        } catch (Exception e) {
            logger.error("Error occured when redirecting to URL %s.", e, encodedRedirectUrl);
        }
    }

    public String urlEncode(String url) {
        String retval = null;

        try {
            retval = URLEncoder.encode(url, DEFAULT_URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsupported encoding for %s.", e, DEFAULT_URL_ENCODING);
        }

        return retval;
    }

    public String newUuidString() {
        String retval = null;
        retval = UUID.randomUUID().toString();
        if (logger.isFiner()) {
            logger.finer("Returning generated UUID string: " + retval);
        }
        return retval;
    }

    public String loadFileStringContentsFromClasspath(String location) {
        String retval = null;
        StringBuilder builder = new StringBuilder();
        try {
            Resource resource = resourceLoader.getResource(location);
            Path path = Paths.get(resource.getURI());
            List<String> allLines = Files.readAllLines(path, Charset.defaultCharset());
            for (String line : allLines) {
                builder.append(line);
            }
            retval = builder.toString();
        } catch (Exception e) {
            logger.error("Error when retrieving all string lines from the file at location %s.", e, location);
        }
        return retval;
    }

    public OffsetDateTime now() {
        return OffsetDateTime.now();
    }

    public OffsetDateTime nowUtc() {
        return OffsetDateTime.now(Clock.systemUTC());
    }

    public String nowUtcPersistentString() {
        return toPersistentString(nowUtc());
    }

    public String toPersistentString(OffsetDateTime offsetDateTime) {
        return offsetDateTime.format(PERSISTENT_TIMESTAMP_FORMATTER);
    }

    public OffsetDateTime toUtcOffsetDateTime(String persistentString) {
        return OffsetDateTime.parse(persistentString, PERSISTENT_TIMESTAMP_FORMATTER);
    }

    public OffsetDateTime toEstOffsetDateTime(String persistentString) {
        OffsetDateTime utcOffsetDateTime = toUtcOffsetDateTime(persistentString);
        return utcToEst(utcOffsetDateTime);
    }

    public OffsetDateTime utcToEst(OffsetDateTime utcTimestamp) {
        return OffsetDateTime.ofInstant(utcTimestamp.toInstant(), ZoneId.of("-05:00"));
    }

    public Date toDate(OffsetDateTime offsetDateTime) {
        return Date.from(offsetDateTime.toInstant());
    }

    public String toDisplayTimestamp(OffsetDateTime offsetDateTime) {
        return offsetDateTime.format(DISPLAY_TIMESTAMP_FORMATTER);
    }

    public String toPrettyTime(OffsetDateTime offsetDateTime) {
        Date timestamp = toDate(offsetDateTime);
        return displayPrettyTimeFormatter.format(timestamp);
    }

    public String jsonStringify(final Object object) {
        String json = null;

        final StringWriter stringWriter = new StringWriter();
        final BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(stringWriter);
            if (logger.isFiner()) {
                logger.finer("About to convert the following object to JSON: %s.", object);
            }
            mapper.writeValue(jsonGenerator, object);

            json = stringWriter.toString();
            if (logger.isFiner()) {
                logger.finer("Successfully converted the object to JSON: \"%s\".", json);
            }
        } catch (IOException e) {
            String msg = String.format("Exception when attempting to stringify JSON with the following object: %s.", object);
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                logger.warn("Exception when attempting to close JSON stringify buffered writer: %s.", e);
            }
            try {
                stringWriter.close();
            } catch (IOException e) {
                logger.warn("Exception when attempting to close JSON stringify string writer: %s.", e);
            }
        }

        return json;
    }

    public <T> T jsonParse(final String json, final Class<T> type) {
        T object;

        final ObjectMapper mapper = new ObjectMapper();
        try {
            object = mapper.readValue(json, type);
        } catch (Exception e) {
            String msg = String.format("Exception occurred when attempting to parse JSON into an object: %s.", e);
            logger.error(msg, e);
            logger.warn("The JSON from the Exception (%s) was:\n%s\n.", e, json);
            throw new RuntimeException(msg, e);
        }

        return object;
    }

    public boolean isIntuitionVisible(Intuition intuition, String... visibilities) {
        boolean isVisible = false;
        String intuitionVisibility = intuition.getVisibility();
        for (String visibility : visibilities) {
            if (intuitionVisibility.equals(visibility)) {
                isVisible = true;
                break;
            }
        }
        return isVisible;
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}
