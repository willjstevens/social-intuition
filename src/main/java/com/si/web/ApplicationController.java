package com.si.web;

import com.si.Category;
import com.si.UrlBuilder;
import com.si.entity.Intuition;
import com.si.entity.User;
import com.si.framework.Response;
import com.si.framework.Session;
import com.si.framework.SessionManager;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.AccountService;
import com.si.service.ApplicationService;
import com.si.service.ConfigurationService;
import com.si.service.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static com.si.Constants.DEVICE_ID;
import static com.si.Constants.USER_ID;
import static com.si.UrlConstants.*;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Handles requests for the application home page.
 */
@Controller
public class ApplicationController
{
    private static final Logger logger = LogManager.manager().newLogger(ApplicationController.class, Category.CONTROLLER);
    @Autowired private SessionManager sessionManager;
    @Autowired private ConfigurationService configurationService;
    @Autowired private AccountService accountService;
    @Autowired private ApplicationService applicationService;
    @Autowired private UtilityService utilityService;
    @Autowired private ViewHelper viewHelper;

    @RequestMapping(TEMPLATES_HOME)
    public String home(HttpServletRequest request) {
        String view = "home-welcome";

        // determine whether the user is logged in, if so then display activity feed
        HttpSession httpSession = request.getSession(false); // do NOT create if not already present
        // if this request has an HttpSession and application Session, then set view
        if (httpSession != null) {
            Session session = sessionManager.getSession(httpSession);
            if (session != null && !session.getUser().isGuest() && !session.getUser().isUnidentified()) {
                // only return activity feed if user is not a guest
                view = "home-activity";
            }
        } else {
            // make an attempt to login by cookie
            Cookie cookie = utilityService.findSessionCookie(request.getCookies());
            if (cookie != null) {
                // cookie is present, so try and sign in via session cookie
                Response<Session> serviceResult = accountService.webLoginByCookie(cookie.getValue());
                if (serviceResult.isSuccess()) {
                    Session session = serviceResult.getData();
                    httpSession = request.getSession(true); // now create
                    sessionManager.commitWebSession(session, httpSession);
                    view = "home-activity";
                }
            }
        }

        return view;
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "<h1>Hi.</h1>";
    }

    @RequestMapping(value="/hello-in-response")
    @ResponseBody
    public Response sayHelloInResponseByWeb() {
        Response<User> response = new Response();
        response.setMessage("Helllloooooo to WEB client.");
        response.setCode(42);
        response.setSuccess(true);
        response.setTargetUrl("bla.html");
        response.setError("some 500 error");

        User user = new User();
        user.setUsername("willjstevens");
        user.setFirstName("Will");
        user.setLastName("Stevens");
        user.setAdmin(true);
        user.setDeleted(true);
        user.setUpdateTimestamp(new Date());
        response.setData(user);

        return response;
    }

    @RequestMapping(value="/hello-in-response", headers={USER_ID, DEVICE_ID})
    @ResponseBody
    public Response sayHelloInResponseByDevice(@RequestHeader(USER_ID) String userId,
                                               @RequestHeader(value = DEVICE_ID) String deviceId,
                                               @CookieValue(value = "JSESSIONID", required = false) String jsessionValue) {
        Response<User> response = new Response();
        response.setMessage("Helllloooooo to DEVICE client.");
        response.setCode(42);
        response.setSuccess(true);
        response.setTargetUrl("bla.html");
        response.setError("some 500 error");

        User user = new User();
        user.setUsername("willjstevens");
        user.setFirstName("Will");
        user.setLastName("Stevens");
        user.setAdmin(true);
        user.setDeleted(true);
        user.setUpdateTimestamp(new Date());
        response.setData(user);

        return response;
    }


    @RequestMapping(value="/echo-user", method=POST, headers={USER_ID, DEVICE_ID})
    @ResponseBody
    public Response postHelloUserInResponseByDevice(@RequestBody User user,
                                                @RequestHeader(USER_ID) String userId,
                                               @RequestHeader(value = DEVICE_ID) String deviceId,
                                               @CookieValue(value = "JSESSIONID", required = false) String jsessionValue) {
        Response<User> response = new Response();
        response.setMessage(user.getFullName());
        response.setCode(200);
        response.setData(user);
        response.setSuccess(true);

        return response;
    }

    @RequestMapping("/pulse")
    @ResponseBody
    public String pulse() {
        return "Beat.";
    }

    @RequestMapping(LOGOUT)
    public String webLogout(HttpServletRequest request, HttpServletResponse response) {
        sessionManager.removeWebSession(request, response);

        // redirect to non-secure homepage
        UrlBuilder homePageBuilder = new UrlBuilder(configurationService);
        homePageBuilder.setDomainPrefixSecure();
        String homePage = homePageBuilder.buildUrl();
        if (logger.isFine()) {
            logger.fine("After logout, redirecting to %s.", homePage);
        }
        return "redirect:" + homePage;
    }

    @RequestMapping(SIGNUP_VERIFY)
    public String registerVerifyWeb(@RequestParam String email, @RequestParam("vc") String verificationCode) {
        Response<String> response = accountService.verifyRegistration(email, verificationCode);

        // redirect to non-secure homepage
        UrlBuilder verificationPageBuilder = setupRedirectBuilder(VERIFICATION);
        verificationPageBuilder.addRequestParameterPair("t", response.getData());

        String verificationPage = verificationPageBuilder.buildUrl();
        if (logger.isFine()) {
            logger.fine("After verification, redirecting to %s.", verificationPage);
        }
        return "redirect:" + verificationPage;
    }

    @RequestMapping(SIGNUP)
    public String redirectToSignupWeb() {
        UrlBuilder signupBuilder = setupRedirectBuilder(SIGNUP);
        String redirectUrl = signupBuilder.buildUrl();
        if (logger.isFine()) {
        	logger.fine("Redirecting signup to: %s", redirectUrl);
        }
        return "redirect:" + redirectUrl;
    }

    @RequestMapping(REFERRAL_STATIC)
    public String referral(@PathVariable String referralCode) {
        // redirect to SPA page for handling
        String referralSpaPath = REFERRAL_SPA_PREFIX + referralCode;
        UrlBuilder referralPageBuilder = setupRedirectBuilder(referralSpaPath);
        String referralPage = referralPageBuilder.buildUrl();
        if (logger.isFine()) {
            logger.fine("Making referaral redirect to %s.", referralPage);
        }
        return "redirect:" + referralPage;
    }

    @RequestMapping(INTUITION_STATIC)
    public String intuitionDirect(@PathVariable String intuitionId) {
        String intuitionSpaPath = INTUITION_STATIC_PREFIX + intuitionId;
        UrlBuilder urlBuilder = setupRedirectBuilder(intuitionSpaPath);
        String spaPage = urlBuilder.buildUrl();
        if (logger.isFine()) {
            logger.fine("Making intuition static redirect to %s.", spaPage);
        }
        return "redirect:" + spaPage;
    }

    @RequestMapping(value=INTUITION_STATIC, headers="User-Agent=facebookexternalhit/1.1")
    @ResponseBody
    public String intuitionFromFacebookCrawlerShortHeader(@PathVariable String intuitionId) {
        if (logger.isFiner()) {
        	logger.finer("Facebook crawler hit with user agent: User-Agent=facebookexternalhit/1.1");
        }
        return getFacebookCrawlerContent(intuitionId);
    }

    @RequestMapping(value=INTUITION_STATIC, headers="User-Agent=facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)")
    @ResponseBody
    public String intuitionFromFacebookCrawlerLongHeader(@PathVariable String intuitionId) {
        if (logger.isFiner()) {
            logger.finer("Facebook crawler hit with user agent: User-Agent=facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)");
        }
        return getFacebookCrawlerContent(intuitionId);
    }

    private String getFacebookCrawlerContent(String intuitionId) {
        Intuition intuition = applicationService.getIntuitionById(intuitionId).getData();
        return viewHelper.buildFacebookSharePageObject(intuition, configurationService.getClientConfig().getFacebookAppId());
    }

    @RequestMapping(SHARE_FACEBOOK)
    @ResponseBody
    public String shareFacebook(@PathVariable String intuitionId) {
        Intuition intuition = applicationService.getIntuitionById(intuitionId).getData();
        return viewHelper.buildFacebookSharePageObject(intuition, configurationService.getClientConfig().getFacebookAppId());
    }

    @RequestMapping(SHARE_TWITTER)
    public String shareTwitterForUser(@PathVariable String intuitionId) {
        UrlBuilder pageBuilder = new UrlBuilder(configurationService);
        pageBuilder.setDomainPrefixSecure();
        pageBuilder.appendActionPathPart(INTUITION);
        pageBuilder.appendActionPathPart(intuitionId);
        String redirectUrl = pageBuilder.buildUrl();
        if (logger.isFiner()) {
            logger.finer("Twitter share URL hit, likely a user clicking on card, being redirected to: %s", redirectUrl);
        }
        return "redirect:" + redirectUrl;
    }

    @RequestMapping(value=SHARE_TWITTER, headers="User-Agent=Twitterbot")
    @ResponseBody
    public String shareTwitterForCrawler(@PathVariable String intuitionId) {
        if (logger.isFiner()) {
            logger.finer("Twitter crawler hit with user agent: User-Agent=Twitterbot");
        }
        return getTwitterCrawlerContent(intuitionId);
    }

    @RequestMapping(value=SHARE_TWITTER, headers="User-Agent=Twitterbot/1.0")
    @ResponseBody
    public String shareTwitterForCrawler_1_0(@PathVariable String intuitionId) {
        if (logger.isFiner()) {
            logger.finer("Twitter crawler hit with user agent: User-Agent=Twitterbot/1.0");
        }
        return getTwitterCrawlerContent(intuitionId);
    }

    private String getTwitterCrawlerContent(String intuitionId) {
        Intuition intuition = applicationService.getIntuitionById(intuitionId).getData();
        return viewHelper.buildTwitterTweetCardObject(intuition);
    }

    private UrlBuilder setupRedirectBuilder(String basePath) {
        UrlBuilder pageBuilder = new UrlBuilder(configurationService);
        pageBuilder.setDomainPrefixSecure();
        pageBuilder.setSpaPrefix();
        pageBuilder.appendActionPathPart(basePath);
        return pageBuilder;
    }
}
