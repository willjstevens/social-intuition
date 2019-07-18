/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.service;

import com.si.Category;
import com.si.dao.DaoConfig;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 
 *
 * @author wstevens
 */
@Service
public class DependencyService
{
	private static final Logger logger = LogManager.manager().newLogger(DependencyService.class, Category.CONFIGURATION);
	@Autowired private DaoConfig daoConfig;
	@Autowired private ConfigurationService configurationService;
	@Autowired private UtilityService utilityService;
	@Autowired private ImageOperations imageOperations;

    @PostConstruct
	public void init() {
		while (notFullyInitialized()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Interruption exception when sleeping during initialization service initialization.", e);
			}

			if (daoConfig.isInitialized()) {
				logger.fine("Done with initializing DaoConfig. About to initialize configuration service.");
				configurationService.init();
				logger.fine("Done with initializing configuration service. About to initialize utility service.");
				utilityService.init();
				logger.fine("Done with initializing utility service. About to initialize image operations.");
				imageOperations.init();
				logger.fine("Done with initializing image operations.");
			}
		}
	}

	private boolean notFullyInitialized() {
		boolean retval = false;
		// now, image operations is the last one to initialize
		if (!imageOperations.isInitialized()) {
			retval = true;
		}
		return retval;
	}
}
