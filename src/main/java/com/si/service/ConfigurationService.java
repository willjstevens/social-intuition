/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.service;

import com.si.Category;
import com.si.Util;
import com.si.dao.ApplicationDao;
import com.si.dto.ClientConfigDto;
import com.si.entity.Config;
import com.si.log.Level;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.si.Category.*;

/**
 * 
 *
 * @author wstevens
 */
@Service
public class ConfigurationService
{
	private static final Logger logger = LogManager.manager().newLogger(ConfigurationService.class, Category.CONFIGURATION);
	private static final String ENVIRONMENT_STR_DEV 	= "dev";
	private static final String ENVIRONMENT_STR_PROD	= "prod";
    private static final DateFormat appDataPublishFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); // 2013-09-26 11:47:15.634-05

	@Autowired private ApplicationDao applicationDao;
	@Autowired private UtilityService utilityService;
	private boolean isInitialized;
	private int systemServiceScheduledExecutorDelay;
	private Level logLevel;
	private Date appDataPublishDate;
	private String environmentString;
	private String emailConnectionString;
	private String siteDomainHttpString;
	private String secureSiteDomainHttpsString;
	private String siteDomainOverride;
	private int verificationPeriodAllowanceDays;
    private int intuitionExpiration;
    private int cookieExpirySeconds;
    private String providerSecureRequestHeaderName;
	private ClientConfigDto clientConfig;
	private String userGuestImageUrl;
	private String userDefaultImageUrl;

	public void init() {
		load();
		isInitialized = true;
	}

	public void load() {
		final List<Config> allConfigs = applicationDao.getAllConfigs();
		for (Config config : allConfigs) {
			final Category category = config.getCategory();
			final String key = config.getKey();
			final String value = config.getValue();
			logger.info("Loading config with key %s for category %s with value \"%s\".", key, category, value);
			if (value == null) {
				throw new NullPointerException(String.format("Config value for key %s is null which is not allowed.", key));
			}
			if (category == CONTROLLER) {
				if (key.equals("cookieExpirySeconds")) {
					cookieExpirySeconds = new BigDecimal(value).intValue();
				}
			} else if (category == SERVICE_SYSTEM) {
				if (key.equals("scheduledJobDelay")) {
					systemServiceScheduledExecutorDelay = Integer.parseInt(value);
				}
			} else if (category == SERVICE_ACCOUNT) {
				if (key.equals("verificationPeriodAllowanceDays")) {
					verificationPeriodAllowanceDays = Integer.parseInt(value);
				} else if (key.equals("userGuestImageUrl")) {
					userGuestImageUrl = value;
				}   else if (key.equals("userDefaultImageUrl")) {
					userDefaultImageUrl = value;
				}
			} else if (category == SERVICE_APPLICATION) {
                if (key.equals("intuitionExpiration")) {
                    intuitionExpiration = Integer.parseInt(value);
                }
            } else if (category == LOG) {
				if (key.equals("level")) {
					logLevel = Level.toLogLevel(value);
                    if (logLevel != null) {
                        LogManager.manager().setLevel(logLevel);
                        logger.info("Set official log level to: %s", logLevel);
                    }
				}
			} else if (category == CLIENT) {
				if (key.equals("appDataPublishDate")) {
                    try {
                        appDataPublishDate = appDataPublishFormatter.parse(value);
                    } catch (ParseException e) {
                        appDataPublishDate = Util.now();
                        logger.error("Problem parsing app data publish string \"%s\". It will be explicitly set to a timestamp of now: %s.", e, value, appDataPublishDate.toString());
                    }
                } else if (key.equals("clientConfig")) {
					clientConfig = utilityService.jsonParse(value, ClientConfigDto.class);
				}
			} else if (category == ENVIRONMENT) {
				if (key.equals("environment")) {
					environmentString = value.toLowerCase();
					if (!ENVIRONMENT_STR_DEV.equals(environmentString) && !ENVIRONMENT_STR_PROD.equals(environmentString)) {
						throw new IllegalArgumentException(String.format("Environment value %s is not valid.", environmentString));
					}
				} else if (key.equals("siteDomainHttp")) {
					siteDomainHttpString = value;
				} else if (key.equals("siteDomainHttps")) {
					secureSiteDomainHttpsString = value;
				} else if (key.equals("siteDomainOverride")) {
					siteDomainOverride = value;
				} else if (key.equals("providerSecureRequestHeaderName")) {
                	providerSecureRequestHeaderName = value;
                }
			} else if (category == Category.UTILITY) {
				if (key.equals("emailConnectionString")) {
					emailConnectionString = value;
				}	
			} else {
                logger.warn("Found unrecognized config entry \"%s\" under category %s.", key, category);
            }
		}

		// do post read-in work
		clientConfig.setUserDefaultImageUrl(userDefaultImageUrl);
		clientConfig.setUserGuestImageUrl(userGuestImageUrl);
		clientConfig.setSiteDomainHttps(secureSiteDomainHttpsString);
		clientConfig.setSiteDomainOverride(siteDomainOverride);
	}


	public int getSystemServiceScheduledExecutorDelay() {
		return systemServiceScheduledExecutorDelay;
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public Date getAppDataPublishDate() {
		return appDataPublishDate;
	}

	public String getEnvironmentString() {
		return environmentString;
	}

	public boolean isDevEnvironment() {
        return environmentString.equals(ENVIRONMENT_STR_DEV);
    }

	public boolean isProdEnvironment() {
		return environmentString.equals(ENVIRONMENT_STR_PROD);
	}

	public String getEmailConnectionString() {
		return emailConnectionString;
	}

	public String getSiteDomainHttpString() {
		return siteDomainHttpString;
	}

	public String getSecureSiteDomainHttpsString() {
		return secureSiteDomainHttpsString;
	}

	public int getAccountServiceVerificationPeriodAllowanceDays() {
		return verificationPeriodAllowanceDays;
	}

	public int getCookieExpirySeconds() {
		return cookieExpirySeconds;
	}

	public String getProviderSecureRequestHeaderName() {
		return providerSecureRequestHeaderName;
	}

    public int getIntuitionExpiration() {
        return intuitionExpiration;
    }

	public ClientConfigDto getClientConfig() {
		return clientConfig;
	}

	public String getUserGuestImageUrl() {
		return userGuestImageUrl;
	}

	public String getUserDefaultImageUrl() {
		return userDefaultImageUrl;
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public String getSiteDomainOverride() {
		return siteDomainOverride;
	}
}
