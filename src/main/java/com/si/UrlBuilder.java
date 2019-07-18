/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si;

import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.ConfigurationService;

import java.util.ArrayList;
import java.util.List;

import static com.si.UrlConstants.*;

/**
 *
 *
 * 
 * @author wstevens
 */
public final class UrlBuilder 
{
	private static final Logger logger = LogManager.manager().newLogger(UrlBuilder.class, Category.UTILITY);
	private ConfigurationService configurationService;

	private String domainPrefix;
	private List<String> actionParts = new ArrayList<>();
	private List<RequestParamPair> requestParams = new ArrayList<>();
    private boolean makeAbsolute;
    private boolean useSpaPrefix;
	
	public UrlBuilder(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	private class RequestParamPair {
		String key;
		String value;
		public RequestParamPair(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}

	public UrlBuilder setDomainPrefixPlain() {
		domainPrefix = configurationService.getSiteDomainHttpString();
		return this;
	}

	public UrlBuilder setDomainPrefixSecure() {
		domainPrefix = configurationService.getSecureSiteDomainHttpsString();
		return this;
	}

    public UrlBuilder setSpaPrefix() {
        useSpaPrefix = true;
        return this;
    }

    public void makeAbsolute() {
        makeAbsolute = true;
    }

	public UrlBuilder addRequestParameterPair(String key, String value) {
		RequestParamPair pair = new RequestParamPair(key, value);
		requestParams.add(pair);
		return this;
	}
	
	public UrlBuilder appendActionPathPart(String actionPart) {
		actionParts.add(actionPart);
		return this;
	}
	
	public UrlBuilder appendUserHomePage(String username) {
		StringBuilder userPagePathBuilder = new StringBuilder();
		userPagePathBuilder.append(URL_PATH_SEPARATOR_CHAR);
		userPagePathBuilder.append(username);
		String path = userPagePathBuilder.toString();
		if (logger.isFiner()) {
			logger.finer("Assembled user home page as: %s", path);
		}
		appendActionPathPart(path);
		return this;
	}

	public String buildActionParts() {
		StringBuilder builder = new StringBuilder();
		for (String actionPart : actionParts) {
			builder.append(actionPart);
		}
		return builder.toString();
	}
	
	public void clear() {
		domainPrefix = null;
		actionParts.clear();
		requestParams.clear();
	}
	
	public String buildUrl() {
		StringBuilder builder = new StringBuilder();

        // if no domain prefix is set but make absolute is, then default to NON-secure
        if (makeAbsolute && domainPrefix == null) {
            setDomainPrefixPlain();
        }
        // now if domain prefix is set then attach
		if (domainPrefix != null) {
			builder.append(domainPrefix);			
		}
        // if an SPA (single page app), then add the prefix
        if (useSpaPrefix) {
            builder.append(URL_PATH_SEPARATOR_CHAR);
            builder.append(SPA_PREFIX);
        }

		for (String actionPart : actionParts) {
            if (!actionPart.startsWith(URL_PATH_SEPARATOR)) {
                builder.append(URL_PATH_SEPARATOR_CHAR);
            }
			builder.append(actionPart);
		}
		if (!requestParams.isEmpty()) {
			builder.append(QUERY_STRING_SEPARATOR);
			for (int i = 0, size = requestParams.size(); i < size; i++) {
				RequestParamPair pair = requestParams.get(i);
				builder.append(pair.key).append(QUERY_STRING_ASSIGNMENT).append(pair.value);
				if (i != size-1) {
					builder.append(QUERY_STRING_AND);
				}
			}
		}

        // if nothing set at this point, then it is a root request
        if (builder.length() == 0) {
            builder.append(URL_PATH_SEPARATOR_CHAR);
        }

		String absoluteUrl = builder.toString();
		if (logger.isFine()) {
			logger.fine("Assembled URL is: %s", absoluteUrl);
		}

		return absoluteUrl;
	}
	
	 
}
