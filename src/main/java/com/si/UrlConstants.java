/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si;

/**
 *
 *
 * 
 * @author wstevens
 */
public final class UrlConstants 
{
	private UrlConstants() {}

	public static final char URL_PATH_SEPARATOR_CHAR 			= '/';
	public static final char QUERY_STRING_SEPARATOR 			= '?';
	public static final char QUERY_STRING_ASSIGNMENT			= '=';
	public static final char QUERY_STRING_AND					= '&';
	public static final String URL_PATH_SEPARATOR 				= Character.toString(URL_PATH_SEPARATOR_CHAR);
	public static final String REST_API_PATH                    = URL_PATH_SEPARATOR_CHAR + "api";
	public static final String INTERCEPTOR_WILDCARD             = URL_PATH_SEPARATOR_CHAR + "**";
    public static final String SPA_PREFIX                       = "#";

    // Top-level Page paths
    public static final String HOME_PAGE                        = URL_PATH_SEPARATOR_CHAR + "";
    public static final String INVALID_BROWSER                  = URL_PATH_SEPARATOR_CHAR + "invalidbrowser";
    public static final String PRIVACY_POLICY                   = URL_PATH_SEPARATOR_CHAR + "privacypolicy";
    public static final String TERMS_OF_SERVICE                 = URL_PATH_SEPARATOR_CHAR + "termsofservice";
    public static final String HOW_IT_WORKS                     = URL_PATH_SEPARATOR_CHAR + "howitworks";
    public static final String CONTACT_US_POST                  = URL_PATH_SEPARATOR_CHAR + "contactus"; // only a POST for this URL
    public static final String DASHBOARD						= URL_PATH_SEPARATOR_CHAR + "dashboard";
    public static final String PROFILE                          = URL_PATH_SEPARATOR_CHAR + "profile";

    // Registration and login
    public static final String SIGNUP                           = URL_PATH_SEPARATOR_CHAR + "signup";
    public static final String SIGNUP_SOCIAL_PLATFORM           = SIGNUP + URL_PATH_SEPARATOR_CHAR + "social-platform";

    public static final String SIGNUP_VERIFY                    = URL_PATH_SEPARATOR_CHAR + "verify";
    public static final String VERIFICATION                     = URL_PATH_SEPARATOR_CHAR + "verification";

    public static final String SEARCH                           = URL_PATH_SEPARATOR_CHAR + "search";
    public static final String SEARCH_EMAIL                     = SEARCH + URL_PATH_SEPARATOR_CHAR + "email"  + URL_PATH_SEPARATOR_CHAR + "{email:.+}";
    public static final String SEARCH_USERNAME                  = SEARCH + URL_PATH_SEPARATOR_CHAR + "username" + URL_PATH_SEPARATOR_CHAR + "{username}";
    public static final String SEARCH_NAME                      = SEARCH + URL_PATH_SEPARATOR_CHAR + "name" + URL_PATH_SEPARATOR_CHAR + "{name}";

    public static final String USERS                            = URL_PATH_SEPARATOR_CHAR + "users";
    public static final String USERS_ALL                        = USERS + URL_PATH_SEPARATOR_CHAR + "all";
    public static final String LOGIN							= URL_PATH_SEPARATOR_CHAR + "login";
    public static final String LOGIN_SOCIAL_PLATFORM			= LOGIN + URL_PATH_SEPARATOR_CHAR + "social-platform";
    public static final String LOGIN_DEVICE_SESSION             = LOGIN + URL_PATH_SEPARATOR_CHAR + "device-session";
    public static final String LOGOUT                           = URL_PATH_SEPARATOR_CHAR + "logout";
    public static final String USER                             = URL_PATH_SEPARATOR_CHAR + "user";

    // General Misc paths
    public static final String RESOURCES_DIR                    = URL_PATH_SEPARATOR_CHAR + "r";
    public static final String RESOURCES_DIR_WILDCARD           = RESOURCES_DIR + URL_PATH_SEPARATOR_CHAR + INTERCEPTOR_WILDCARD;
    public static final String FAVICON                          = "favicon.ico";
    public static final String REFERRAL_STATIC                  = URL_PATH_SEPARATOR_CHAR + "rf" + URL_PATH_SEPARATOR_CHAR + "{referralCode}";
    public static final String REFERRAL_SPA_PREFIX              = "r" + URL_PATH_SEPARATOR_CHAR;
    public static final String SHARE                            = URL_PATH_SEPARATOR_CHAR + "share";
    public static final String SHARE_FACEBOOK_PREFIX            = SHARE + URL_PATH_SEPARATOR_CHAR + "facebook";
    public static final String SHARE_FACEBOOK                   = SHARE_FACEBOOK_PREFIX + URL_PATH_SEPARATOR_CHAR + "{intuitionId}";
    public static final String SHARE_TWITTER                    = SHARE + URL_PATH_SEPARATOR_CHAR + "twitter" + URL_PATH_SEPARATOR_CHAR + "{intuitionId}";
    public static final String DEVICE                           = URL_PATH_SEPARATOR + "device";
    public static final String DEVICE_WILDCARD                  = INTERCEPTOR_WILDCARD + DEVICE + URL_PATH_SEPARATOR + INTERCEPTOR_WILDCARD;
    public static final String DEVICE_API_PREFIX                = REST_API_PATH + URL_PATH_SEPARATOR + "device";

    public static final String COHORT                           = URL_PATH_SEPARATOR_CHAR + "cohort";
    public static final String COHORT_ACCEPT                    = COHORT + URL_PATH_SEPARATOR_CHAR + "accept";
    public static final String COHORT_IGNORE                    = COHORT + URL_PATH_SEPARATOR_CHAR + "ignore";
    public static final String COHORT_UNCOHORT                  = COHORT + URL_PATH_SEPARATOR_CHAR + "uncohort";
    public static final String COHORT_ALL                       = COHORT + URL_PATH_SEPARATOR_CHAR + "all";
    public static final String COHORT_INVITES                   = COHORT + URL_PATH_SEPARATOR_CHAR + "invites";

    public static final String NOTIFICATION                     = URL_PATH_SEPARATOR_CHAR + "notification";
    public static final String NOTIFICATION_HANDLED             = NOTIFICATION + URL_PATH_SEPARATOR_CHAR + "handled";

    public static final String INTUITION                        = URL_PATH_SEPARATOR_CHAR + "intuition";
    public static final String INTUITION_API                    = REST_API_PATH + INTUITION;
    public static final String INTUITION_NEW_SETTINGS           = URL_PATH_SEPARATOR_CHAR + "new-intuition-settings";
    public static final String INTUITION_STATIC_PREFIX          = INTUITION + URL_PATH_SEPARATOR_CHAR;
    public static final String INTUITION_STATIC                 = INTUITION_STATIC_PREFIX + "{intuitionId}";
    public static final String FEEDBACK                         = URL_PATH_SEPARATOR_CHAR + "feedback";
    public static final String FETCH_CLIENT_CONFIG              = URL_PATH_SEPARATOR_CHAR + "fetch-client-config";
    public static final String FETCH_REFERRAL                   = URL_PATH_SEPARATOR_CHAR + "fetch-referral" + URL_PATH_SEPARATOR_CHAR + "{referralCode}";

    // SPA Templates
    public static final String TEMPLATES                        = URL_PATH_SEPARATOR_CHAR + "templates";
    public static final String TEMPLATES_HOME                   = TEMPLATES + URL_PATH_SEPARATOR_CHAR + "home";
    public static final String TEMPLATES_SIGNUP                 = TEMPLATES + SIGNUP;
    public static final String TEMPLATES_LOGIN                  = TEMPLATES + LOGIN;
    public static final String TEMPLATES_VERIFICATION           = TEMPLATES + VERIFICATION;
    public static final String TEMPLATES_PROFILE                = TEMPLATES + PROFILE;
    public static final String TEMPLATES_DASHBOARD              = TEMPLATES + DASHBOARD;
    public static final String TEMPLATES_INTUITION              = TEMPLATES + INTUITION;
    public static final String TEMPLATES_PROGRESS               = TEMPLATES + URL_PATH_SEPARATOR_CHAR + "progress";
    public static final String TEMPLATES_SINK                   = TEMPLATES + URL_PATH_SEPARATOR_CHAR + "sink";
    public static final String TEMPLATES_SCORE_HISTORY          = TEMPLATES + URL_PATH_SEPARATOR_CHAR + "score-history";
    public static final String TEMPLATES_PRIVACY_POLICY         = TEMPLATES + URL_PATH_SEPARATOR_CHAR + "privacy-policy";
    public static final String TEMPLATES_TERMS_OF_SERVICE       = TEMPLATES + URL_PATH_SEPARATOR_CHAR + "terms-of-service";
    public static final String TEMPLATES_404                    = TEMPLATES + URL_PATH_SEPARATOR_CHAR + "404";
    // Partials
    public static final String TEMPLATES_PARTIALS               = TEMPLATES + URL_PATH_SEPARATOR_CHAR + "partials";
    public static final String TEMPLATES_FEED                   = TEMPLATES_PARTIALS + URL_PATH_SEPARATOR_CHAR + "feed";


    public static final String[] SECURE_INTERCEPTOR_PATTERNS = {
            TEMPLATES_LOGIN,
            TEMPLATES_DASHBOARD,
            TEMPLATES_FEED
    };

    public static final String[] ATTEMPTED_LOGIN_INTERCEPTOR_PATTERNS = {
            TEMPLATES_PROFILE,
            INTUITION_API + URL_PATH_SEPARATOR_CHAR + "fetch" + INTERCEPTOR_WILDCARD
    };

    public static final String[] SESSION_REQUIRED_INTERCEPTOR_PATTERNS = {
            REST_API_PATH + USER
    };

//    public static final String[] DEVICE_SESSION_EXCLUSION_PATTERNS = {
//            DEVICE + SIGNUP,
//            DEVICE + LOGIN,
//            DEVICE + LOGIN_DEVICE_SESSION,
//            DEVICE + SEARCH_USERNAME,
//            DEVICE + SEARCH_EMAIL
//    };
}
