/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si;

import com.si.dao.DaoConfig;
import com.si.service.ServiceConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * The root configuration class which ties all other common and tier configurations together at 
 * runtime.  This class is only used when the web application runs, and not used in unit testing.
 * 
 * @author wstevens
 */
@Configuration
@Import({
	CommonConfig.class,
	DaoConfig.class,
	ServiceConfig.class
})
//@ComponentScan(basePackageClasses={Si.class})
@ComponentScan
public class RootConfig
{
}