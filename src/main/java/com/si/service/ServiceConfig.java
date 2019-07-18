/**
 * 
 */
package com.si.service;

import com.si.Category;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the application services.
 * 
 * @author wstevens
 */
@Configuration
public class ServiceConfig
{
    private static final Logger logger = LogManager.manager().newLogger(ServiceConfig.class, Category.SERVICE);

//	@Bean
//	public AccountService accountService() {
//		return new AccountService();
//	}
//
//	@Bean
//	public ConfigurationService configurationService() {
//		return new ConfigurationService();
//	}
//
//	@Bean
//	public UtilityService utilityService() {
//		return new UtilityService();
//	}
//
//	@Bean
//	public ApplicationService applicationService() {
//		return new ApplicationService();
//	}
//
//	@Bean
//	public ValidationService validationService() {
//		return new ValidationService();
//	}

}
