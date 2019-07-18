package com.si.service;

import com.si.Category;
import com.si.UrlBuilder;
import com.si.Util;
import com.si.dao.ApplicationDao;
import com.si.dto.DeviceLoginDto;
import com.si.dto.NotificationDto;
import com.si.dto.ProfileDto;
import com.si.dto.ScoreDto;
import com.si.entity.*;
import com.si.framework.*;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.*;

import static com.si.Constants.PREFS_SHOW_SCORES;
import static com.si.Constants.USERNAME_EXCLUSIONS;
import static com.si.UrlConstants.SIGNUP_VERIFY;


/**
 * Standard implementation for account operations.
 *
 * @author wstevens
 */
@Service
public class AccountService extends AbstractServiceImpl 
{
	private static final Logger logger = LogManager.manager().newLogger(AccountService.class, Category.SERVICE_ACCOUNT);
	private Set<String> excludedUsernames = new HashSet<>();
    private long verificationPeriodDaysAllowanceMillis;
    @Autowired private SessionManager sessionManager;
    @Autowired private ApplicationDao applicationDao;
	@Autowired private UtilityService utilityService;
    @Autowired private ConfigurationService configurationService;
    @Autowired private ValidationService validationService;
    @Autowired private ImageOperations imageOperations;

	@PostConstruct
	public void init() {
		loadVerificationPeriodDaysAllowance();
        excludedUsernames.addAll(Arrays.asList(USERNAME_EXCLUSIONS));
	}

	private void loadVerificationPeriodDaysAllowance() {
		final int daysAllowance = configurationService.getAccountServiceVerificationPeriodAllowanceDays();
		verificationPeriodDaysAllowanceMillis = daysAllowance * 24 * 60 * 60 * 1000;
	}

	public Response<Session> webLoginFromSocialIntuitionByUsernameOrEmailAndPassword(String username, String password) {
		Response<Session> response = new Response<>();
		try {
            username = username.toLowerCase();
            // determine if user sent an email in the username/email field
            boolean isEmail = utilityService.isValidEmailAddress(username);
            // we know username on social intuition login so find user by either username or email
			User user = null;
            if (isEmail) {
                String email = username; // user actually sent an email not username
                user = applicationDao.findUserByEmail(email);
            } else {
                user = applicationDao.findUserByUsername(username);
            }

            verifyUser(user, password, response);
		} catch (Exception e) {
			logger.error("Error occurred during login from SI for username or email: %s.", e, username);
			ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
			throw throwMe;
		}
		return response;
	}

    public Response<Session> socialLoginByEmail(String email) {
        Response<Session> response = new Response<>();
        try {
            email = email.toLowerCase();
            User user = applicationDao.findUserByEmail(email);
            socialLoginFacade(user, response);
        } catch (Exception e) {
            logger.error("Error occurred during login from Facebook for email: %s.", e, email);
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }
        return response;
    }

    private void socialLoginFacade(User user, Response response) {
        if (user != null) {
            // already authenticated with OAuth on frontend so just load session
            loadSession(user, response);
        } else { // username was not found
            response.setMessage("No user found for social login.");
            if (logger.isFine()) {
                logger.fine("No user passed or found for social login for (incorrect) username.");
            }
        }
    }

    private void loadSession(User user, Response response) {
        Session session = sessionManager.initWebSession(user);
        if (user.getCookieValue() == null) {
            // load a new cookie value if not already set
            String cookieValue = utilityService.newUuidString();
            user.setCookieValue(cookieValue);
            applicationDao.save(user);
            sessionManager.getSessionByUser(user).setUser(user); // reset user
        }
        response.setData(session);
        response.setSuccess(true);
        if (logger.isFine()) {
            logger.fine("Loaded user into session with username \"%s\".", user.getUsername());
        }
    }

    private void verifyUser(User user, String password, Response<Session> response) {
        if (user != null) {
            // User found, now check for password validity.
            String sourcePassword = user.getPassword();
            if (sourcePassword.equals(password)) {
                // passwords match so success.
                loadSession(user, response);
            } else {
                // password entered was invalid.
                response.setMessage("Login invalid.");
                if (logger.isFine()) {
                    logger.fine("Password mismatch on login attempt for username %s. Submitted the incorrect password of %s but valid password is %s.", user.getUsername(), password, user.getPassword());
                }
            }
        } else { // username was not found
            response.setMessage("Login invalid.");
            if (user != null) {
                String username = user.getUsername();
                String id = username != null ? username : user.getEmail();
                if (logger.isFine()) {
                    logger.fine("Username or email login attempt could not be found for submitted (incorrect) username or email \"%s\".", id);
                }
            }
        }
    }

    // reflects basic frontend validation for registration
    public boolean isValidUser(User user) {
	    boolean retval = false;

	    if (validationService.isNotNullAndNotEmpty(user.getEmail()) &&
            validationService.isNotNullAndNotEmpty(user.getUsername()) &&
            validationService.isNotNullAndNotEmpty(user.getFirstName()) &&
            validationService.isNotNullAndNotEmpty(user.getLastName()) &&
            validationService.isNotNullAndNotEmpty(user.getPassword()) &&
                user.isInAgreement()) {
	        retval = true;
        } else {
            if (logger.isFine()) {
                logger.fine("Failed to validate user: %s", user);
            }
        }

        return retval;
    }

	// only called by page load service
	public Response<Session> webLoginByCookie(String cookieValue) {
		Response<Session> response = new Response<>();
		try {
			User user = applicationDao.findUserByCookieValue(cookieValue);
			if (user != null) {
				Session session = sessionManager.initWebSession(user);
				response.setData(session);
				response.setSuccess(true);
                if (logger.isFine()) {
                	logger.fine("Successfully logged username \"%s\" in by cookie value \"%s\".", user.getUsername(), cookieValue);
                }
			} else {
                if (logger.isFine()) {
                	logger.fine("Could not login in unknown user by submitted cookie value of \"%s\".", cookieValue);
                }
            }
		} catch (Exception e) {
			logger.error("Error occurred during login for cookie value: %s.", e, cookieValue);
			ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
			throw throwMe;
		}
		return response;
	}

    public Response<DeviceLoginDto> deviceLoginByUsernameAndPassword(String username, String password, String deviceId, String httpSessionId) {
        Response<DeviceLoginDto> response = new Response<>();
        try {
            username = username.toLowerCase();
            User user = applicationDao.findUserByUsername(username);
            if (user != null) {
                // User found, now check for password validity.
                String sourcePassword = user.getPassword();
                if (sourcePassword.equals(password)) {
                    // now that we have an authenticated user attempt to get existing Session object or initiate a new one

                    // first fetch app session
                    Session session = sessionManager.getSessionByUser(user);
                    if (session == null) {
                        session = sessionManager.initDeviceSession(user);
                    }

                    // now we need to register new device session
                    final String userId = user.getId();
                    final DeviceSession deviceSession = new DeviceSession(userId, deviceId, httpSessionId);
                    sessionManager.commitDeviceSession(session, deviceSession);

                    // register in database
                    applicationDao.deviceSessionLogin(deviceSession);

                    // load up return DTO
                    DeviceLoginDto deviceLoginDto = new DeviceLoginDto();
                    deviceLoginDto.setDeviceSession(deviceSession);
                    deviceLoginDto.setUser(simplifyUser(user));
                    response.setData(deviceLoginDto);

                    response.setSuccess(true);
                    if (logger.isFine()) {
                        logger.fine("Successfully logged username \"%s\" in by username and password.", user.getUsername());
                    }
                } else { // password entered was invalid.
                    response.setMessage("Login invalid.");
                    if (logger.isFine()) {
                        logger.fine("Password mismatch on login attempt for username %s. Submitted the incorrect password of %s but valid password is %s.", username, password, user.getPassword());
                    }
                }
            } else { // username was not found
                response.setMessage("Login invalid.");
                if (logger.isFine()) {
                    logger.fine("Username on login attempt could not be found for submitted (incorrect) username %s.", username);
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred during device login for username: %s.", e, username);
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }
        return response;
    }

    public Response<DeviceLoginDto> deviceLoginByDeviceSession(DeviceSession deviceSession) {
        Response<DeviceLoginDto> response = new Response<>();
        try {
            // At this point, the DeviceSession parameter is probably invalid Session object
            //      could be gone. So we should start a clean login process

            // the things we know now are:
            final String userId = deviceSession.getUserId();
            final String deviceId = deviceSession.getDeviceId();
            final String httpSessionId = deviceSession.getHttpSessionId();
            if (logger.isFiner()) {
            	logger.finer("Device login by new DeviceSession and Http Session ID \"%s\" and user ID \"%s\" and device ID ", httpSessionId, userId, deviceId);
            }

            // now first try to fetch or load an app Session object
            User user = applicationDao.findUserById(userId);
            Session session = sessionManager.getSessionByUser(user);
            if (session == null) {
                session = sessionManager.initDeviceSession(user);
            }

            // commit in session manager and register in database
            sessionManager.commitDeviceSession(session, deviceSession);
            applicationDao.deviceSessionLogin(deviceSession);

            // load up return DTO
            DeviceLoginDto deviceLoginDto = new DeviceLoginDto();
            deviceLoginDto.setDeviceSession(deviceSession);
            deviceLoginDto.setUser(simplifyUser(user));
            response.setData(deviceLoginDto);

            response.setSuccess(true);
            if (logger.isFine()) {
                logger.fine("Successfully logged username \"%s\" in by device ID \"%s\".", user.getUsername(), deviceSession.getDeviceId());
            }
        } catch (Exception e) {
            logger.error("Error occurred during login for device ID value: %s.", e, deviceSession.getDeviceId());
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }
        return response;
    }

    public Response deviceLogout(DeviceSession deviceSession) {
        Response response = new Response();
        try {
            // first cleanup session manager
            sessionManager.removeDeviceSession(deviceSession);
            // now wipe session from database
            applicationDao.deviceSessionLogout(deviceSession);

            response.setSuccess(true);
            if (logger.isFine()) {
                logger.fine("Successfully logged out user with user ID \"%s\".", deviceSession.getUserId());
            }
        } catch (Exception e) {
            logger.error("Error occurred during logout for device ID value: %s.", e, deviceSession.getDeviceId());
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }
        return response;
    }

    public Response deviceSessionExpired(DeviceSession deviceSession) {
        Response response = new Response();
        try {
            // first cleanup session manager
            sessionManager.removeDeviceSession(deviceSession);
            // now keep device session record but wipe session ID
            applicationDao.deviceSessionExpired(deviceSession);

            response.setSuccess(true);
            if (logger.isFine()) {
                logger.fine("Successfully expired device session for user with user ID \"%s\".", deviceSession.getUserId());
            }
        } catch (Exception e) {
            logger.error("Error occurred during logout for device ID value: %s.", e, deviceSession.getDeviceId());
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }
        return response;
    }

    public Response addCohort(User inviteUser, User consenterUser) {
        Response response = new Response();
        try {
            Date now = Util.now();
            Cohort cohort = new Cohort();
            cohort.setInviterUserId(inviteUser.getId());
            cohort.setInviterFullName(inviteUser.getFullName());
            cohort.setInviterUsername(inviteUser.getUsername());
            cohort.setInviterImageInfo(inviteUser.getImageInfo());
            cohort.setConsenterUserId(consenterUser.getId());
            cohort.setConsenterFullName(consenterUser.getFullName());
            cohort.setConsenterUsername(consenterUser.getUsername());
            cohort.setConsenterImageInfo(consenterUser.getImageInfo());
            cohort.setInsertTimestamp(utilityService.nowUtcPersistentString());
            applicationDao.save(cohort);

            // send notification
            Notification<Cohort> consenterNotification = new Notification();
            consenterNotification.setUserId(consenterUser.getId());
            consenterNotification.setType("add-cohort");
            consenterNotification.setMessage(inviteUser.getFullName() + " has invited you to be a cohort.");
            consenterNotification.setData(cohort);
            consenterNotification.setInsertTimestamp(utilityService.nowUtcPersistentString());

            applicationDao.save(consenterNotification);

            response.setSuccess(true);
            response.setMessage("Cohort invite sent.");
        } catch (Exception e) {
            logger.error("Exception in addCohort: ", e);
        }
        return response;
    }

    public Response acceptCohort(Notification<Cohort> notification, User user) {
        Response response = new Response();
        try {
            Cohort cohort = notification.getData();
            cohort = applicationDao.setCohortToAccepted(cohort);

            notification.setData(cohort);
            notification.setHandled(true);
            applicationDao.save(notification);

            // now update in session manager
            Session userSession = sessionManager.getSessionByUser(user);
            List<Cohort> userCohorts = applicationDao.findCohorts(user); // reload refresh
            userSession.setCohorts(userCohorts);
            // now do the same on cohort so he can see, IF he has an established session
            User cohortUser = applicationDao.findUserById(cohort.getInviterUserId());
            Session cohortSession = sessionManager.getSessionByUser(cohortUser);
            // might not be logged in:
            if (cohortSession != null) {
                List<Cohort> cohortCohorts = applicationDao.findCohorts(cohortUser);
                cohortSession.setCohorts(cohortCohorts);
            }

            // send notification
            Notification<Cohort> inviterNotification = new Notification();
            inviterNotification.setUserId(cohort.getInviterUserId());
            inviterNotification.setType("invite-accepted");
            inviterNotification.setMessage(cohort.getConsenterFullName() + " has accepted your invite.");
            inviterNotification.setData(cohort);
            inviterNotification.setInsertTimestamp(utilityService.nowUtcPersistentString());
            applicationDao.save(inviterNotification);

            response.setSuccess(true);
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
        return response;
    }

    public Response ignoreCohort(Notification<Cohort> notification, User user) {
        Response response = new Response();
        try {
            Cohort cohort = notification.getData();
            cohort = applicationDao.setCohortToIgnored(cohort);

            notification.setData(cohort);
            notification.setHandled(true);
            applicationDao.save(notification);

            response.setSuccess(true);
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
        return response;
    }

    public Response unCohort(Cohort cohort, User user) {
        Response response = new Response();
        try {
            applicationDao.unCohort(cohort);
            response.setSuccess(true);

            boolean isInviter = user.getId().equals(cohort.getInviterUserId());
            String msg = String.format("Cohort link broken with %s.", isInviter ? cohort.getConsenterFullName() : cohort.getInviterFullName());
            response.setMessage(msg);
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
        return response;
    }

    public Response<List<Cohort>> findCohorts(User user) {
        Response<List<Cohort>> response = new Response();
        try {
            List<Cohort> cohorts = applicationDao.findCohorts(user);
            response.setSuccess(true);
            response.setData(cohorts);
            response.setMessage("Cohort invite sent.");
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
        return response;
    }

    public Response<List<Cohort>> findCohortInvites(User user) {
        Response<List<Cohort>> response = new Response();
        try {
            List<Cohort> cohorts = applicationDao.findCohortInvites(user);
            response.setSuccess(true);
            response.setData(cohorts);
            response.setMessage("Cohort invite sent.");
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
        return response;
    }

    public Response<ProfileDto> getProfile(String profileUsername, Session requestingUserSession) {
        Response<ProfileDto> response = new Response();
        try {
            ProfileDto profileDto = new ProfileDto();
            User profileUser = null;

            // lookup user and set into dto
            profileUser = applicationDao.findUserByUsername(profileUsername);
            String profileUserId = profileUser.getId();

            // if the viewer of the profile is logged in, then determine how much visibility the user has
            if (!requestingUserSession.getUser().isUnidentified() && !requestingUserSession.getUser().isGuest()) {
                profileDto.setShowCohortButtonSection(true);
                profileDto.setHasSession(true);
                User requestingUser = requestingUserSession.getUser();
                if (profileUsername.equals(requestingUser.getUsername())) {
                    // if here then the user is looking at his own profile
                    profileUser = requestingUser;
                    profileDto.setOwner(true);
                } else if (requestingUserSession.hasCohort(profileUsername)) {
                    // the user is already a cohort of the profile he is looking at
                    profileDto.getUserDto().setCohort(true);
                    profileDto.setIsCohort(true);
                } else {
                    // the user has a session but is not a cohort, check if request is sent
                    Cohort cohort = applicationDao.findCohortRequestForUser(requestingUser.getId(), profileUserId);
                    boolean isRequestSent = cohort != null;
                    profileDto.setCohortRequestSent(isRequestSent);
                }
            }

            profileUser = simplifyUser(profileUser);
            profileDto.getUserDto().setUser(profileUser);
            profileDto.setScoreDto(loadScoreDto(profileUserId));

            response.setData(profileDto);
            response.setSuccess(true);
            if (logger.isFiner()) {
                logger.finer("Returning profile user %s %s.", profileUser.getFirstName(), profileUser.getLastName());
            }
        } catch (Exception e) {
            logger.error("Exception in getProfile: ", e);
        }
        return response;
    }

    private ScoreDto loadScoreDto(String userId) {
        ScoreDto scoreDto = new ScoreDto();
        Score score = applicationDao.findScoreByUserId(userId);
        scoreDto.setScore(score);
        scoreDto.calculateSums();

        // calculate percentages
        List<Intuition> ownedCorrect = score.getOwnedCorrect();
        List<Intuition> ownedIncorrect = score.getOwnedIncorrect();
        List<Intuition> cohortCorrect = score.getCohortCorrect();
        List<Intuition> cohortIncorrect = score.getCohortIncorrect();

        int ownedTotal = ownedCorrect.size() + ownedIncorrect.size();
        int cohortTotal = cohortCorrect.size() + cohortIncorrect.size();
        int total = ownedTotal + cohortTotal;
        if (total >= 1) {
            int ownedCorrectCount = ownedCorrect.size();
            int cohortCorrectCount = cohortCorrect.size();
            if (ownedTotal >= 1) {
                float result = ((float) ownedCorrectCount / ownedTotal) * 100;
                scoreDto.setOwnedPercent((int) result);
            }
            if (cohortTotal >= 1) {
                float result = ((float) cohortCorrectCount / cohortTotal) * 100;
                scoreDto.setCohortPercent((int) result);
            }
            float result = ((float) (ownedCorrectCount + cohortCorrectCount) / total) * 100;
            scoreDto.setTotalPercent((int) result);
        }

        return scoreDto;
    }

    public Response<ScoreDto> getScoreHistory(User user) {
        Response<ScoreDto> response = new Response();
        try {
            ScoreDto scoreDto = loadScoreDto(user.getId());
            mergeAndOrderIntuitionOutcomes(scoreDto);

            response.setData(scoreDto);
            response.setSuccess(true);
            if (logger.isFiner()) {
                logger.finer("Returning score history for user ID %s.", user.getId());
            }
        } catch (Exception e) {
            logger.error("Exception in getScoreHistory: ", e);
        }
        return response;
    }

    private void mergeAndOrderIntuitionOutcomes(ScoreDto scoreDto) {
        Score score = scoreDto.getScore();
        List<Intuition> ownedCorrect = score.getOwnedCorrect();
        List<Intuition> ownedIncorrect = score.getOwnedIncorrect();
        List<Intuition> cohortCorrect = score.getCohortCorrect();
        List<Intuition> cohortIncorrect = score.getCohortIncorrect();

        // merged and order intuition objects
        SortedMap<OffsetDateTime, Intuition> orderedIntuitions = new TreeMap<>();
        addIntuitionsToOrderingMap(orderedIntuitions, ownedCorrect);
        addIntuitionsToOrderingMap(orderedIntuitions, ownedIncorrect);
        scoreDto.setAllOwned(new ArrayList<>(orderedIntuitions.values()));

        orderedIntuitions.clear();
        addIntuitionsToOrderingMap(orderedIntuitions, cohortCorrect);
        addIntuitionsToOrderingMap(orderedIntuitions, cohortIncorrect);
        scoreDto.setAllCohort(new ArrayList<>(orderedIntuitions.values()));
    }

    private void addIntuitionsToOrderingMap(SortedMap<OffsetDateTime, Intuition> orderedIntuitions, List<Intuition> intuitions) {
        for (Intuition intuition : intuitions) {
            // order by outcome date
            Outcome outcome = intuition.getOutcome();
            OffsetDateTime outcomeDate = utilityService.toUtcOffsetDateTime(outcome.getInsertTimestamp());
            orderedIntuitions.put(outcomeDate, intuition);
        }
    }

    public Response registerFromSocialIntuition(User user) {

	    // TODO: Validation here:
        // ...
        // or throw new BadRequestPageResultException()

        // stage fields on user object for registration from SI

        final String fullName = user.getFirstName() + " " + user.getLastName();
        user.setFullName(fullName);

        // DISABLED FOR NOW to prevent drop-offs
        boolean verificationEnabled = false;

        if (!verificationEnabled) {
            // disabled so implicitly set verification complete code
            user.setVerificationCodeCompletedTimestamp(utilityService.nowUtcPersistentString());
        }

        Response response = registerUser(user);

        if (verificationEnabled) {
            String encodedVerificationCode = utilityService.urlEncode(user.getVerificationCode());
            UrlBuilder accountVerificationLinkBuilder = new UrlBuilder(configurationService);
            accountVerificationLinkBuilder.setDomainPrefixSecure();
            accountVerificationLinkBuilder.appendActionPathPart(SIGNUP_VERIFY);
            accountVerificationLinkBuilder.addRequestParameterPair("email", user.getEmail());
            accountVerificationLinkBuilder.addRequestParameterPair("vc", encodedVerificationCode);
            String accountVerificationLink = accountVerificationLinkBuilder.buildUrl();
            String emailBody = utilityService.loadFileStringContentsFromClasspath("classpath:email-verification.html");
            emailBody = emailBody.replace("${verification-link}", accountVerificationLink);
            // send verification email
            utilityService.sendHtmlEmail(user.getEmail(), "SI Account Verification", emailBody);
        }

        return response;
    }

    public Response registerFromSocialPlatform(User user) {
        // set this field implicitly since already verified through social platform
        user.setVerificationCodeCompletedTimestamp(utilityService.nowUtcPersistentString());

        return registerUser(user);
    }

    private Response registerUser(User user) {
        Response response = new Response();
        try {
            // normalize fields
            String username = user.getUsername().toLowerCase();
            username = username.replaceAll("\\.", "");

            // stage fields on user object
            user.setUsername(username);
            user.setEmail(user.getEmail().toLowerCase());
            user.setVerificationCodeIssuedTimestamp(utilityService.nowUtcPersistentString());
            user.setVerificationCode(utilityService.newUuidString());
            user.setInAgreement(true); // implied if here
            // set default image url
            String userDefaultImageUrl = imageOperations.copyDefaultProfileImageToUser(user.getUsername());
            StoredImageInfo storedImageInfo = new StoredImageInfo();
            storedImageInfo.setSecureUrl(userDefaultImageUrl);
            user.setImageInfo(storedImageInfo);
            // insert into database
            applicationDao.insert(user);

            // now query for newly added user for Id and setup score object
            User newUser = applicationDao.findUserByEmail(user.getEmail());
            Score score = new Score();
            score.setUserId(newUser.getId());
            applicationDao.save(score);

            // set default preferences
            Preference showScoresPref = new Preference();
            showScoresPref.setUserId(newUser.getId());
            showScoresPref.setKey(PREFS_SHOW_SCORES);
            showScoresPref.setValue(Boolean.TRUE.toString());
            applicationDao.save(showScoresPref);

            // fire welcome notification
            Notification<User> welcomeNotification = new Notification();
            welcomeNotification.setUserId(newUser.getId());
            welcomeNotification.setType("welcome");
            welcomeNotification.setMessage("Welcome to Social Intuition! Get going and add some notifications!");
            welcomeNotification.setData(simplifyUser(newUser));
            welcomeNotification.setInsertTimestamp(utilityService.nowUtcPersistentString());
            applicationDao.save(welcomeNotification);

            // fire email
            StringBuilder builder = new StringBuilder();
            builder.append("<p>Name: ").append(user.getFullName()).append("</p>");
            builder.append("<p>Email: ").append(user.getEmail()).append("</p>");
            builder.append("<p>Username: ").append(user.getUsername()).append("</p>");
            builder.append("<p>Registration source: ").append(user.getRegistrationSource()).append("</p>");
            String msg = builder.toString();
            utilityService.sendHtmlEmail("willjstevens@gmail.com", "NEW USER: " + user.getFullName(), msg);

            response.setData(newUser);
            response.setSuccess(true);
            if (logger.isInfo()) {
                logger.info("Registered user with user ID \"%s\".", user.getUsername());
            }
        } catch (Exception e) {
            logger.error("Error occurred when registering user: %s.", e, user.getEmail());
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }
        return response;
    }

    public Response verifyRegistration(String email, String verificationCode) {
        Response response = new Response();
        try {
            User user = applicationDao.findUserByEmail(email);
            if (user != null) {
                if (user.getVerificationCodeCompletedTimestamp() == null) { // not yet verified so proceed
                    Date now = Util.now();
                    long nowMillis = now.getTime();
                    long issuedTimestampMillis = utilityService.toUtcOffsetDateTime(user.getVerificationCodeIssuedTimestamp()).toInstant().getEpochSecond();
                    long differenceMillis = nowMillis - issuedTimestampMillis;
                    boolean isWithinAllowancePeriod = differenceMillis <= verificationPeriodDaysAllowanceMillis;
                    if (isWithinAllowancePeriod) {
                        boolean verificationCodesMatch = user.getVerificationCode().equals(verificationCode);
                        if (verificationCodesMatch) { // verification codes match
                            user.setEnabled(true);
                            user.setVerificationCodeCompletedTimestamp(utilityService.nowUtcPersistentString());
                            applicationDao.save(user);
                            response.setSuccess(true);
                            response.setData("vs"); // for Verification Success
                            if (logger.isFine()) {
                                logger.fine("Successfully verified user account with username %s.", user.getUsername());
                            }
                        } else { // verification codes do not match
                            response.setCode(610);
                            response.setData("bd"); // for Bad Verification
                            response.setMessage("Bad verification code.");
                            if (logger.isInfo()) {
                                logger.info("User account with username %s had verification codes which did not match.", user.getUsername());
                            }
                        }
                    } else { // the user surpassed the allowable period for verification
                        response.setCode(620);
                        response.setData("ec"); // for Expired Code
                        response.setMessage("Expired verification period and code.");
                        if (logger.isInfo()) {
                            logger.info("User account with username %s let the allowable period expire for account verification.", user.getUsername());
                        }
                    }
                } else { // the user has already verified the email address
                    response.setCode(630);
                    response.setData("vac"); // for Verify Already Completed
                    response.setMessage("Registration already verified.");
                    if (logger.isInfo()) {
                        logger.info("User account with username %s has already completed account verification.", user.getUsername());
                    }
                }
            } else { // user could not be found by way of email
                response.setCode(640);
                response.setData("uni"); // for User Not Identifiable
                response.setMessage("You could not be identified.");
                if (logger.isInfo()) {
                    logger.info("Could not locate user object for email %s.", email);
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while verifying the account: " + e, e);
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }

        return response;
    }

    public Response checkUsernameAvailability(String username) {
        Response response = new Response();
        username = username.toLowerCase(); // condition argument
        try {
            if (excludedUsernames.contains(username)) {
                if (logger.isFiner()) {
                    logger.finer("Username \"%s\" is a prohibited name.", username);
                }
            } else { // continue, not on the exclude list
                User user = applicationDao.findUserByUsername(username);
                if (user == null) { // user is available since not in database
                    response.setSuccess(true);
                    if (logger.isFiner()) {
                        logger.finer("Username \"%s\" available so far.", username);
                    }
                } else {
                    if (logger.isFiner()) {
                        logger.finer("Username \"%s\" (or typed fragment) is already taken.", username);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception in checkUsernameAvailability for username=%s.", e, username);
        }
        return response;
    }

    public Response checkEmailAvailability(String email) {
        Response response = new Response();
        email = email.toLowerCase(); // condition argument
        try {
            User users = applicationDao.findUserByEmail(email);
            if (users == null) { // user is available since not in database
                response.setSuccess(true);
                if (logger.isFiner()) {
                    logger.finer("Email \"%s\" available so far.", email);
                }
            } else {
                if (logger.isFiner()) {
                    logger.finer("Email \"%s\" (or typed fragment) is already taken.", email);
                }
            }
        } catch (Exception e) {
            logger.error("Exception in checkEmailAvailability for email=%s.", e, email);
        }
        return response;
    }

    public Response<List<NotificationDto>> findUnhandledNotifications(User user) {
        Response response = new Response();
        try {
            List<NotificationDto> notificationDtos = new ArrayList<>();
            List<Notification> notifications = applicationDao.findUnhandledNotifications(user);
            if (!notifications.isEmpty()) {
               notifications.stream()
                       .forEach(n -> {
                           OffsetDateTime insertTimestamp = utilityService.toEstOffsetDateTime(n.getInsertTimestamp());
                           String prettyTimestamp = utilityService.toPrettyTime(insertTimestamp);
                           notificationDtos.add(new NotificationDto(n, prettyTimestamp));
                       });
            }
            response.setData(notificationDtos);
            response.setSuccess(true);
        } catch (Exception e) {
            logger.error("Exception in findUnhandledNotifications for username=%s.", e, user.getUsername());
        }
        return response;
    }

    public Response notificationHandled(Notification notification) {
        Response response = new Response();
        try {
            notification.setHandled(true);
            applicationDao.save(notification);
            response.setSuccess(true);
        } catch (Exception e) {
            logger.error("Exception in notificationHandled for notification = %s.", e, notification.getId());
        }
        return response;
    }

    public Response<User> getSimplifiedUser(User fullUser) {
        Response<User> response = new Response<>();
        User simplifiedUser = simplifyUser(fullUser);
        response.setData(simplifiedUser);
        response.setSuccess(true);
        return response;
    }

    public User simplifyUser(User user) {
        User simpleUser = new User();
        simpleUser.setId(user.getId());
        simpleUser.setFullName(user.getFullName());
        simpleUser.setFirstName(user.getFirstName());
        simpleUser.setLastName(user.getLastName());
        simpleUser.setUsername(user.getUsername());
        simpleUser.setEmail(user.getEmail());
        simpleUser.setImageInfo(user.getImageInfo());
        simpleUser.setGuest(user.isGuest());
        return simpleUser;
    }

    public Response<User> saveProfilePhoto(User user, FileInfo fileInfo) {
        Response response = new Response();
        try {
            Session session = sessionManager.getSessionByUser(user);
            // store to online image store
            String profileImageUrl = imageOperations.storeProfileImage(user.getUsername(), fileInfo);
            // set in database
            User attachedUser = applicationDao.findUserById(user.getId());
            StoredImageInfo storedImageInfo = new StoredImageInfo();
            storedImageInfo.setSecureUrl(profileImageUrl);
            attachedUser.setImageInfo(storedImageInfo);
            applicationDao.save(attachedUser);
            session.setUser(attachedUser); // reset attached user with photo

            // simplify and attach user
            attachedUser = simplifyUser(attachedUser);
            response.setData(attachedUser);
            response.setSuccess(true);
            if (logger.isFine()) {
                logger.fine("Successfully saved profile photo for user ID \"%s\".", user.getId());
            }
        } catch (Exception e) {
            logger.error("Error occurred saving profile photo for ID value: %s.", e, user.getId());
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }
        return response;
    }

}
