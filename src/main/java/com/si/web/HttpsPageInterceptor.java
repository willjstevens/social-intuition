/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.web;

import com.si.Category;
import com.si.UrlBuilder;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.ConfigurationService;
import com.si.service.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *
 * 
 * @author wstevens
 */
@Component
public class HttpsPageInterceptor extends HandlerInterceptorAdapter
{
	private static final Logger logger = LogManager.manager().newLogger(HttpsPageInterceptor.class, Category.INTERCEPTOR_SECURE_RESERVED);
	@Autowired private ConfigurationService configurationService;
    @Autowired private UtilityService utilityService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		boolean doPassRequest = false;		
		String targetLocation = request.getRequestURI(); // default to relative URI
		// determine if the request for a secure page is over SSL, if not then rebuild URL for HTTPS 
		boolean isOverSsl = utilityService.isOverSsl(request);

		// Uncomment to troubleshoot locally without doing HTTPS redirect.
//		isOverSsl = true;

		if (isOverSsl) {
            doPassRequest = true;
        } else {
			// build and set target URL for over SSL
			UrlBuilder urlBuilder = new UrlBuilder(configurationService);
			urlBuilder.setDomainPrefixSecure();
            urlBuilder.appendActionPathPart(request.getRequestURI());
			targetLocation = urlBuilder.buildUrl();
			if (logger.isFiner()) {
				logger.finer("Redirecting to URL: %s.", targetLocation);
			}

            // if not over SSL then issue redirect to target location, with permanent 301 redirect since it's always over SSL
            utilityService.redirect(response, targetLocation, HttpServletResponse.SC_MOVED_PERMANENTLY);
		}

        return doPassRequest;
	}
		
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}
}
