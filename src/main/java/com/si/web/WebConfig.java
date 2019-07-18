package com.si.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static com.si.UrlConstants.*;

/**
 * Configuration for web related components.
 * 
 * See Spring MVC documentation for specifics on each method below.
 * 
 * @author wstevens
 */
@EnableWebMvc
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter
{
    @Autowired private HttpsPageInterceptor httpsPageInterceptor;
    @Autowired private SecureReservedPageInterceptor secureReservedPageInterceptor;
    @Autowired private AttemptLoginInterceptor attemptLoginInterceptor;
    @Autowired private SessionRequiredInterceptor sessionRequiredInterceptor;
    @Autowired private DeviceSessionInterceptor deviceSessionInterceptor;


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(HOME_PAGE).setViewName("index");
        registry.addViewController(TEMPLATES_SIGNUP).setViewName("signup");
        registry.addViewController(TEMPLATES_LOGIN).setViewName("login");
        registry.addViewController(TEMPLATES_VERIFICATION).setViewName("verification");
        registry.addViewController(TEMPLATES_PROFILE).setViewName("profile");
        registry.addViewController(TEMPLATES_DASHBOARD).setViewName("dashboard");
        registry.addViewController(TEMPLATES_INTUITION).setViewName("intuition");
        registry.addViewController(TEMPLATES_PROGRESS).setViewName("progress");
        registry.addViewController(TEMPLATES_SINK).setViewName("sink");
        registry.addViewController(TEMPLATES_SCORE_HISTORY).setViewName("score-history");
        registry.addViewController(TEMPLATES_PRIVACY_POLICY).setViewName("privacy-policy");
        registry.addViewController(TEMPLATES_TERMS_OF_SERVICE).setViewName("terms-of-service");
        registry.addViewController(TEMPLATES_404).setViewName("404");
    }

    @Bean
    public InternalResourceViewResolver internalResourceViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/html/");
        viewResolver.setSuffix(".html");
        return viewResolver;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(10000000);
        multipartResolver.setMaxUploadSize(10000000);
        return multipartResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // interceptor to enforce all HTTPS

        // TODO: Uncomment me:
//        registry.addInterceptor(httpsPageInterceptor)
//                .addPathPatterns(INTERCEPTOR_WILDCARD)
//                .excludePathPatterns("/pulse");

        // interceptor for controlling login and having sessions
        registry.addInterceptor(secureReservedPageInterceptor)
                .addPathPatterns(SECURE_INTERCEPTOR_PATTERNS);

        // interceptor for attempted auto login
        registry.addInterceptor(attemptLoginInterceptor)
                .addPathPatterns(ATTEMPTED_LOGIN_INTERCEPTOR_PATTERNS);

        // interceptor for requests where session required but no login redirect
        //      NOTE: session here is only web session
        registry.addInterceptor(sessionRequiredInterceptor)
                .addPathPatterns(SESSION_REQUIRED_INTERCEPTOR_PATTERNS);

        registry.addInterceptor(deviceSessionInterceptor)
                .addPathPatterns(REST_API_PATH + INTERCEPTOR_WILDCARD);
    }

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("/");
	}

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
        super.configureDefaultServletHandling(configurer);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false).favorParameter(true);
    }
}