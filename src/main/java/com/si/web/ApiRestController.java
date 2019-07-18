package com.si.web;

import com.si.Category;
import com.si.UrlBuilder;
import com.si.dto.DeviceLoginDto;
import com.si.dto.NotificationDto;
import com.si.dto.ProfileDto;
import com.si.dto.ScoreDto;
import com.si.entity.*;
import com.si.framework.FileInfo;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.si.Constants.*;
import static com.si.UrlConstants.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping(REST_API_PATH)
@RestController
public class ApiRestController
{
	private static final Logger logger = LogManager.manager().newLogger(ApiRestController.class, Category.CONTROLLER);
	@Autowired private SessionManager sessionManager;
	@Autowired private AccountService accountService;
    @Autowired private ApplicationService applicationService;
    @Autowired private UtilityService utilityService;
    @Autowired private ConfigurationService configurationService;

    @RequestMapping(value=LOGIN, method=POST)
    public Response webLoginFromSocialIntuition(@RequestBody User user, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Response<User> response = new Response<>();
        Response<Session> serviceResponse = accountService.webLoginFromSocialIntuitionByUsernameOrEmailAndPassword(user.getUsername(), user.getPassword());
        finalizeSession(response, serviceResponse, httpServletRequest, httpServletResponse);
        return response;
    }

    @RequestMapping(value=LOGIN_SOCIAL_PLATFORM, method=POST)
    public Response webLoginFromFacebook(@RequestBody User user, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Response<User> response = new Response<>();
        Response<Session> serviceResponse = accountService.socialLoginByEmail(user.getEmail());
        finalizeSession(response, serviceResponse, httpServletRequest, httpServletResponse);
        return response;
    }

    private void finalizeSession(Response<User> response, Response<Session> serviceResponse, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (serviceResponse.isSuccess()) {
            Session session = serviceResponse.getData();
            HttpSession httpSession = httpServletRequest.getSession();
            sessionManager.commitWebSession(session, httpSession);
            // ensure cookie is response for future hits
            User user = session.getUser();
            String cookieValue = user.getCookieValue();
            Cookie sessionCookie = utilityService.findSessionCookie(httpServletRequest.getCookies());
            if (sessionCookie != null) { // either not created yet or cleared from logout
                // if the old cookie value does not equal what is sitting on the user object, then reset with
                //      a whole new cookie (including expirey); this could happen if a user logged out
                if (!cookieValue.equals(sessionCookie.getValue())) {
                    sessionCookie = utilityService.newSessionCookie(cookieValue);
                }
            } else {
                sessionCookie = utilityService.newSessionCookie(cookieValue); // create a new cookie with that
            }
            httpServletResponse.addCookie(sessionCookie);
            // clear flag for login process since now successful
            httpSession.removeAttribute(LOGIN_PROCESS_FLAG);

            // Now determine and return view name.
            String successfulLoginTargetUrl = HOME_PAGE; // default to home page
            String targetSecureUrl = (String) httpSession.getAttribute(TARGET_URL);
            httpSession.removeAttribute(TARGET_URL);
            if (targetSecureUrl != null && !targetSecureUrl.equals(LOGIN)) {
                // If targetSecureUri is set, then the user originally desired to hit some other secured URL, like /dashboard or /account.
                successfulLoginTargetUrl = targetSecureUrl;
            }
            response.setTargetUrl(successfulLoginTargetUrl);
            if (logger.isFine()) {
                logger.fine("Successfully logged in user %s.", user.getUsername());
            }

            // now we are done so overwrite session object from service result with a simplified user object before converting to DTO
            User simplifiedUser = accountService.simplifyUser(session.getUser());
            response.setData(simplifiedUser);
            response.setSuccess(true);
        } else {
            // else failed login
            response.setMessage(serviceResponse.getMessage());
        }
    }

    @RequestMapping(value=LOGIN, method=POST, headers={DEVICE_ID})
    public ResponseEntity<Response> deviceLogin(@RequestBody User user, @RequestHeader(value = DEVICE_ID) String deviceId, HttpSession httpSession) {
        String httpSessionId = httpSession.getId();
        Response<DeviceLoginDto> response = accountService.deviceLoginByUsernameAndPassword(user.getUsername(), user.getPassword(), deviceId, httpSessionId);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HTTP_SESSION_ID, httpSessionId);
        return new ResponseEntity<Response>(response, responseHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value=LOGIN_DEVICE_SESSION, method=POST, headers={USER_ID, DEVICE_ID})
    public ResponseEntity<Response> deviceLoginByDeviceSession(@RequestHeader(USER_ID) String userId,
                                                               @RequestHeader(value = DEVICE_ID) String deviceId,
                                                               HttpSession httpSession) {
        String httpSessionId = httpSession.getId();
        DeviceSession deviceSession = new DeviceSession(userId, deviceId, httpSessionId);
        Response<DeviceLoginDto> response = accountService.deviceLoginByDeviceSession(deviceSession);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HTTP_SESSION_ID, httpSessionId);
        return new ResponseEntity<Response>(response, responseHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value=LOGOUT, method=POST, headers={USER_ID, DEVICE_ID})
    public Response deviceLogout(@RequestHeader(USER_ID) String userId,
                                    @RequestHeader(DEVICE_ID) String deviceId,
                                    HttpSession httpSession) {
        Response response = accountService.deviceLogout(new DeviceSession(userId, deviceId, httpSession.getId()));
        return response;
    }

    @RequestMapping(LOGOUT)
    public Response webLogout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Response response = new Response();

        sessionManager.removeWebSession(httpServletRequest, httpServletResponse);
        // redirect to non-secure homepage
        UrlBuilder homePageBuilder = new UrlBuilder(configurationService);
        homePageBuilder.setDomainPrefixSecure();
        String homePage = homePageBuilder.buildUrl();
        if (logger.isFine()) {
            logger.fine("After logout, redirecting to %s.", homePage);
        }
        response.setTargetUrl(homePage);
        response.setSuccess(true);

        return response;
    }

    @RequestMapping(value=USER, method=GET, headers={USER_ID, DEVICE_ID})
    public Response<User> getUserByDevice(@RequestHeader(USER_ID) String userId,
                                          @RequestHeader(DEVICE_ID) String deviceId,
                                          HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return accountService.getSimplifiedUser(user);
    }

    @RequestMapping(value=USER, method=GET)
    public Response<User> getUserByWeb(HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession);
        return accountService.getSimplifiedUser(user);
    }

//    @RequestMapping(value=SIGNUP, method=POST)
//    public Response registerFromSocialIntuition(@RequestBody User user, @RequestParam(required=false) String src) {
//        return accountService.registerFromSocialIntuition(user);
//    }


    @RequestMapping(value=SIGNUP, method=POST)
    public ResponseEntity<Response> registerFromSocialIntuition(@RequestBody User user, @RequestParam(required=false) String src) {
        ResponseEntity<Response> responseEntity = null;

        // set user registration source
        if (logger.isFine()) {
            logger.fine("Registering user with found registration source of %s.", src);
        }
        if (src != null) {
            if (src.equals("siw")) {
                user.setRegistrationSource(SOURCE_SOCIAL_INTUITION_WEB);
            } else if (src.equals("sii")) {
                user.setRegistrationSource(SOURCE_SOCIAL_INTUITION_IOS);
            }
        } else {
            // TODO: Update this once IOS client update deployed to WARN for hacker not providing source
            // for now, interpret to be mobile
            user.setRegistrationSource(SOURCE_SOCIAL_INTUITION_IOS);
        }

        Response response = null;
        if (accountService.isValidUser(user)) {
            response = accountService.registerFromSocialIntuition(user);
            responseEntity = new ResponseEntity<Response>(response, HttpStatus.OK);
        } else {
            response = new Response();
            response.setMessage("Error");
            responseEntity = new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
            logger.warn("InvalidUser: %s", user);
        }

        return responseEntity;
    }

    @RequestMapping(value=SIGNUP_SOCIAL_PLATFORM, method=POST)
    public Response registerFromFacebook(@RequestBody User user) {
        return accountService.registerFromSocialPlatform(user);
    }

    @RequestMapping(value=SEARCH_EMAIL, method=GET)
    public Response searchEmail(@PathVariable String email) {
        return accountService.checkEmailAvailability(email);
    }

    @RequestMapping(value=SEARCH_USERNAME, method=GET)
    public Response searchUsername(@PathVariable String username) {
        return accountService.checkUsernameAvailability(username);
    }

    @RequestMapping(value=USERS_ALL, method=GET, headers={USER_ID, DEVICE_ID})
    public Response fetchAllUsersByDevice(@RequestParam(required=false, defaultValue="0") int limit,
                                       @RequestHeader(USER_ID) String userId,
                                       @RequestHeader(DEVICE_ID) String deviceId,
                                       HttpSession httpSession) {
        Session session = null;
        if (sessionManager.hasSessionForDevice(userId, deviceId, httpSession)) {
            session = sessionManager.getSessionForDevice(userId, deviceId, httpSession);
        }

        return applicationService.fetchAllUsers(limit, session);
    }

    @RequestMapping(value=USERS_ALL, method=GET)
    public Response fetchAllUsersByWeb(@RequestParam(required=false, defaultValue="0") int limit, HttpSession httpSession) {
        Session session = null;
        if (sessionManager.hasSessionForWeb(httpSession)) {
            session = sessionManager.getSession(httpSession);
        }

        return applicationService.fetchAllUsers(limit, session);
    }



    @RequestMapping(value=SEARCH_NAME, method=GET, headers={USER_ID, DEVICE_ID})
    public Response searchNameByDevice(@PathVariable String name,
                                       @RequestHeader(USER_ID) String userId,
                                       @RequestHeader(DEVICE_ID) String deviceId,
                                       HttpSession httpSession) {
        Session session = null;
        if (sessionManager.hasSessionForDevice(userId, deviceId, httpSession)) {
            session = sessionManager.getSessionForDevice(userId, deviceId, httpSession);
        }

        return applicationService.searchUserFullNames(name, session);
    }

    @RequestMapping(value=SEARCH_NAME, method=GET)
    public Response searchNameByWeb(@PathVariable String name, HttpSession httpSession) {
        Session session = null;
        if (sessionManager.hasSessionForWeb(httpSession)) {
            session = sessionManager.getSession(httpSession);
        }

        return applicationService.searchUserFullNames(name, session);
    }

    @RequestMapping(value=COHORT, method=POST, headers={USER_ID, DEVICE_ID})
    public Response addCohortByDevice(@RequestBody User consentUser,
                                      @RequestHeader(USER_ID) String userId,
                                      @RequestHeader(DEVICE_ID) String deviceId,
                                      HttpSession httpSession) {
        User inviteUser = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return accountService.addCohort(inviteUser, consentUser);
    }

    @RequestMapping(value=COHORT, method=POST)
    public Response addCohortByWeb(@RequestBody User consentUser, HttpSession httpSession) {
        User inviteUser = sessionManager.getUserForWeb(httpSession);
        return accountService.addCohort(inviteUser, consentUser);
    }

    @RequestMapping(value=COHORT_ACCEPT, method=POST, headers={USER_ID, DEVICE_ID})
    public Response acceptCohortByDevice(@RequestBody Notification<Cohort> notification,
                                      @RequestHeader(USER_ID) String userId,
                                      @RequestHeader(DEVICE_ID) String deviceId,
                                      HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return accountService.acceptCohort(notification, user);
    }

    @RequestMapping(value=COHORT_ACCEPT, method=POST)
    public Response acceptCohortByWeb(@RequestBody Notification<Cohort> notification, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession);
        return accountService.acceptCohort(notification, user);
    }

    @RequestMapping(value=COHORT_IGNORE, method=POST, headers={USER_ID, DEVICE_ID})
    public Response ignoreCohortByDevice(@RequestBody Notification<Cohort> notification,
                                         @RequestHeader(USER_ID) String userId,
                                         @RequestHeader(DEVICE_ID) String deviceId,
                                         HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return accountService.ignoreCohort(notification, user);
    }

    @RequestMapping(value=COHORT_IGNORE, method=POST)
    public Response ignoreCohortByWeb(@RequestBody Notification<Cohort> notification, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession);
        return accountService.ignoreCohort(notification, user);
    }

    @RequestMapping(value=COHORT_UNCOHORT, method=POST, headers={USER_ID, DEVICE_ID})
    public Response unCohortByDevice(@RequestBody Cohort cohort,
                                         @RequestHeader(USER_ID) String userId,
                                         @RequestHeader(DEVICE_ID) String deviceId,
                                         HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return accountService.unCohort(cohort, user);
    }

    @RequestMapping(value=COHORT_UNCOHORT, method=POST)
    public Response unCohortByWeb(@RequestBody Cohort cohort, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession);
        return accountService.unCohort(cohort, user);
    }

    @RequestMapping(value=COHORT_ALL, headers={USER_ID, DEVICE_ID})
    public Response<List<Cohort>> getAllCohortsByDevice(
                                         @RequestHeader(USER_ID) String userId,
                                         @RequestHeader(DEVICE_ID) String deviceId,
                                         HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return accountService.findCohorts(user);
    }

    @RequestMapping(COHORT_ALL)
    public Response<List<Cohort>> getAllCohortsByWeb(HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession);
        return accountService.findCohorts(user);
    }

    @RequestMapping(value=COHORT_INVITES, headers={USER_ID, DEVICE_ID})
    public Response<List<Cohort>> getCohortInvitesByDevice(
                                         @RequestHeader(USER_ID) String userId,
                                         @RequestHeader(DEVICE_ID) String deviceId,
                                         HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return accountService.findCohortInvites(user);
    }

    @RequestMapping(NOTIFICATION)
    public Response<List<NotificationDto>> getUnhandledNotificationsByWeb(HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession);
        return accountService.findUnhandledNotifications(user);
    }

    @RequestMapping(value=NOTIFICATION, method=GET, headers={USER_ID, DEVICE_ID})
    public Response<List<NotificationDto>> getUnhandledNotificationsByDevice(
                                        @RequestHeader(USER_ID) String userId,
                                        @RequestHeader(DEVICE_ID) String deviceId,
                                        HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return accountService.findUnhandledNotifications(user);
    }

    @RequestMapping(value=NOTIFICATION_HANDLED, method=POST)
    public Response notificationHandledByWeb(@RequestBody Notification notification, HttpSession httpSession) {
        sessionManager.verifySessionForWeb(httpSession);
        return accountService.notificationHandled(notification);
    }

    @RequestMapping(value=NOTIFICATION_HANDLED, method=POST, headers={USER_ID, DEVICE_ID})
    public Response notificationHandledByDevice(
                                        @RequestHeader(USER_ID) String userId,
                                        @RequestHeader(DEVICE_ID) String deviceId,
                                        HttpSession httpSession,
                                        @RequestBody Notification notification) {
        sessionManager.verifySessionForDevice(userId, deviceId, httpSession.getId());
        return accountService.notificationHandled(notification);
    }

    @RequestMapping(value="/profile/{username}", method=GET, headers={USER_ID, DEVICE_ID})
    public Response<ProfileDto> fetchProfileByDevice(@RequestHeader(USER_ID) String userId,
                                                      @RequestHeader(DEVICE_ID) String deviceId,
                                                      HttpSession httpSession,
                                                      @PathVariable String username) {
        Session session = sessionManager.getSessionForDevice(userId, deviceId, httpSession);
        return accountService.getProfile(username, session);
    }

    @RequestMapping(value="/profile/{username}", method=GET)
    public Response<ProfileDto> fetchProfileByWeb(HttpSession httpSession, @PathVariable String username) {
        Session session = sessionManager.getSession(httpSession, false);
        return accountService.getProfile(username, session);
    }

    @RequestMapping(value="/profile/photo", method=POST)
    public Response<User> saveProfilePhotoByWeb(HttpSession httpSession, @RequestParam("file") MultipartFile file) {
        Response<User> response = null;
        User user = sessionManager.getUserForWeb(httpSession);

        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            FileInfo fileInfo = new FileInfo(file.getOriginalFilename(), file.getContentType(), inputStream);
            response = accountService.saveProfilePhoto(user, fileInfo);
        } catch (IOException e) {
            logger.error("Error when getting input stream for profile photo upload.", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) { /* swallow */ }
        }

        return response;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(value="/profile/photo", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<User> saveProfilePhotoByDevice(HttpSession httpSession,
                                                   @RequestHeader(USER_ID) String userId,
                                                   @RequestHeader(DEVICE_ID) String deviceId,
                                                   @RequestParam("file") MultipartFile file) {
        Response<User> response = null;
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);

        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            FileInfo fileInfo = new FileInfo(file.getOriginalFilename(), file.getContentType(), inputStream);
            response = accountService.saveProfilePhoto(user, fileInfo);
        } catch (IOException e) {
            logger.error("Error when getting input stream for profile photo upload.", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) { /* swallow */ }
        }

        return response;
    }

    @RequestMapping(value="/score-history", method=GET, headers={USER_ID, DEVICE_ID})
    public Response<ScoreDto> getScoreHistoryByDevice(@RequestHeader(USER_ID) String userId,
                                                         @RequestHeader(DEVICE_ID) String deviceId,
                                                         HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return accountService.getScoreHistory(user);
    }

    @RequestMapping(value="/score-history", method=GET)
    public Response<ScoreDto> getScoreHistoryByWeb(HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession);
        return accountService.getScoreHistory(user);
    }

    @RequestMapping(value=FEEDBACK, method=POST)
    public Response addFeedbackByWeb(@RequestBody Feedback feedback) {
        return applicationService.addFeedback(feedback);
    }

    @RequestMapping(value=FEEDBACK, method=POST, headers={USER_ID, DEVICE_ID})
    public Response<List<NotificationDto>> addFeedbackByDevice(
            @RequestHeader(USER_ID) String userId,
            @RequestHeader(DEVICE_ID) String deviceId,
            @RequestBody Feedback feedback) {
        return applicationService.addFeedback(feedback);
    }

    @RequestMapping(value=FETCH_CLIENT_CONFIG, method=GET)
    public Response fetchClientConfigByWeb() {
        return applicationService.fetchClientConfig();
    }

    @RequestMapping(value=FETCH_REFERRAL, method=GET)
    public Response fetchReferralByWeb(@PathVariable String referralCode) {
        return applicationService.fetchReferral(referralCode);
    }
}
