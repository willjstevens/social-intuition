/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si;

import com.si.dao.DaoConfig;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.ServiceConfig;
import com.si.web.WebSessionListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 *
 *
 * 
 * @author wstevens
 */
@EnableAsync
public class SiAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer
{
    private static final Logger logger = LogManager.manager().newLogger(SiAppInitializer.class, Category.FRAMEWORK);
    private WebApplicationContext webApplicationContext;

    @Override
    protected Class<?>[] getRootConfigClasses () {
        return new Class<?>[] {
//          RootConfig.class
            CommonConfig.class,
            DaoConfig.class,
            ServiceConfig.class
        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses () {
        return new Class<?>[] {
//            WebConfig.class
        };
    }

    @Override
    protected String[] getServletMappings () {
        return new String[] {"/*"};
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        WebSessionListener sessionListener = null;
        try {
            sessionListener = servletContext.createListener(WebSessionListener.class);
        } catch (ServletException e) {
            logger.error("Problem when creating the session listener: %s.", e, e);
        }
        sessionListener.setApplicationContext(webApplicationContext);
        servletContext.addListener(sessionListener);
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        webApplicationContext = super.createRootApplicationContext(); // save reference
        return webApplicationContext;
    }



}
