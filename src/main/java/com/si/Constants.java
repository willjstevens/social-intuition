/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si;

import java.nio.charset.Charset;

/**
 *
 *
 * 
 * @author wstevens
 */
public final class Constants 
{

    private Constants() {}

	public static final String SI_COM				        = "si.com";
    public static final String OWNER_EMAIL                  = "support@valloc.com";
	public static final String CHARSET_UTF_8_STR		    = "UTF-8";
	public static final String DEFAULT_CHARSET_STR		    = CHARSET_UTF_8_STR;
	public static final Charset CHARSET_UTF_8 			    = Charset.forName(CHARSET_UTF_8_STR);
	public static final Charset DEFAULT_CHARSET			    = CHARSET_UTF_8;
	public static final String TARGET_URL 				    = "targetUrl";
	public static final String COOKIE_SESSION_KEY 		    = "ssk";
	public static final String SECURE_SCHEME			    = "https";
	public static final String LOGIN_PROCESS_FLAG 		    = "siLoginProcess";

	public static final String SISESSION_KEY                = "siSession";
    public static final char FORWARD_SLASH                  = '/';
    public static final String FORWARD_SLASH_STR            = Character.toString(FORWARD_SLASH);
    public static final String VISIBILITY_PUBLIC            = "public";
    public static final String VISIBILITY_COHORT            = "cohort";
    public static final String VISIBILITY_PRIVATE           = "private";
    public static final String PREDICTION_TYPE_TRUE_FALSE       = "true-false";
    public static final String PREDICTION_TYPE_YES_NO           = "yes-no";
    public static final String PREDICTION_TYPE_MULTIPLE_CHOICE  = "multiple-choice";
    public static final String SOURCE_SOCIAL_INTUITION      = "social-intuition";
    public static final String SOURCE_SOCIAL_INTUITION_WEB  = SOURCE_SOCIAL_INTUITION + "-web";
    public static final String SOURCE_SOCIAL_INTUITION_IOS  = SOURCE_SOCIAL_INTUITION + "-ios";
    public static final String SOURCE_FACEBOOK              = "facebook";
    public static final String SOURCE_TWITTER               = "twitter";
    public static final String SOURCE_GOOGLE                = "google";

    // preferences
    public static final String PREFS_SHOW_SCORES            = "prefs-show-scores";

    // headers
    public static final String USER_ID                      = "userId";
    public static final String DEVICE_ID                    = "deviceId";
//    public static final String SESSION_ID                   = "sessionId";
    public static final String HTTP_SESSION_ID              = "httpSessionId";


    public static final String[] USERNAME_EXCLUSIONS = {
            "about",
            "register",
            "signup",
            "404",
            "dashboard",
            "profile",
            "score-history",
            "login",
            "logout",
            "feedback",
            "jen",
            "news",
            "quickstart",
            "si",
            "socialintuition",
            "social-intuition",
            "trending",
            "dev",
            "development",
            "activity",
            "feed",
            "cohort",
            "cohorts",
            "sink",
            "guest"
    };
}
