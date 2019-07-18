/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.service;

import com.si.Category;
import com.si.Constants;
import com.si.dao.ApplicationDao;
import com.si.dto.*;
import com.si.entity.*;
import com.si.framework.*;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.*;

import static com.si.Constants.*;


/**
 * 
 *
 * @author wstevens
 */
@Service
public class ApplicationService extends AbstractServiceImpl
{
    private static final Logger logger = LogManager.manager().newLogger(ApplicationService.class, Category.SERVICE_APPLICATION);
    private NewIntuitionDto newIntuitionDto;
    private ClientConfigDto clientConfigDto;
    private Map<String, Referral> referralCache = new HashMap<>();
    @Autowired private ApplicationDao applicationDao;
    @Autowired private ConfigurationService configurationService;
    @Autowired private AccountService accountService;
    @Autowired private UtilityService utilityService;
    @Autowired private ImageOperations imageOperations;
    @Autowired private SessionManager sessionManager;

    @PostConstruct
    public void init() {
        loadNewIntuitionDto();
    }

    private void loadNewIntuitionDto() {
        newIntuitionDto = new NewIntuitionDto();
        newIntuitionDto.setDefaultVisibility(Constants.VISIBILITY_PUBLIC);
        newIntuitionDto.setDefaultPredictionType(Constants.PREDICTION_TYPE_TRUE_FALSE);
        newIntuitionDto.setActiveWindows(loadActiveWindows());
        newIntuitionDto.setPredictionChoicesYesNo(loadPredictionChoicesYesNo());
        newIntuitionDto.setPredictionChoicesTrueFalse(loadPredictionChoicesTrueFalse());
        newIntuitionDto.setScoreIntuition(true);
        newIntuitionDto.setDisplayPrediction(true);
        newIntuitionDto.setDisplayCohortsPredictions(true);
        newIntuitionDto.setAllowCohortsToContributePredictedOutcomes(true);
        newIntuitionDto.setAllowPredictedOutcomeVoting(true);
    }

    public Response<NewIntuitionDto> getNewIntuitionDto() {
        Response<NewIntuitionDto> response = new Response<>();
        response.setData(newIntuitionDto);
        response.setSuccess(true);
        return response;
    }

    public Response<SearchDto> searchUserFullNames(String fullNameString, Session session) {
        Response<SearchDto> response = new Response();

        boolean isUserLoggedIn = session != null;
        SearchDto searchDto = new SearchDto();
        searchDto.setRequestingUserLoggedIn(isUserLoggedIn);
        response.setData(searchDto);

        try {
            fullNameString = fullNameString.trim().replaceAll(" +", " ");
            String[] nameParts = fullNameString.split(" ");

            // first all users by name parts
            List<User> users = applicationDao.findUsersByNameInformation(nameParts);

            if (!users.isEmpty()) { // user is available since not in database
                List<UserDto> userResults = searchDto.getUserResults();

                String requestingUserId = null;
                if (isUserLoggedIn) {
                    requestingUserId = session.getUser().getId();
                }
                for (User user : users) {
                    if (isUserLoggedIn && requestingUserId.equals(user.getId())) {
                        continue; // don't add this user to see himself in the dropdown
                    }
                    UserDto userDto = packageSimpleUserIntoDto(user, session);
                    userResults.add(userDto);
                }

                response.setSuccess(true);
                if (logger.isFiner()) {
                    logger.finer("Found %d users for name \"%s\".", users.size(), fullNameString);
                }
            }
        } catch (Exception e) {
            logger.error("Exception in searchUserFullNames for name=%s.", e, fullNameString);
        }

        return response;
    }

    public Response<SearchDto> fetchAllUsers(int limit, Session session) {
        Response<SearchDto> response = new Response();

        boolean isUserLoggedIn = session != null;
        SearchDto searchDto = new SearchDto();
        searchDto.setRequestingUserLoggedIn(isUserLoggedIn);
        response.setData(searchDto);

        try {
            // safety check and limit
            if (limit <= 0 || limit >= 50) {
                limit = 25; // safe size
            }

            // load all users by limit
            List<User> users = applicationDao.findAllUsers(limit);

            if (!users.isEmpty()) { // user is available since not in database
                List<UserDto> userResults = searchDto.getUserResults();

                String requestingUserId = null;
                if (isUserLoggedIn) {
                    requestingUserId = session.getUser().getId();
                }
                for (User user : users) {
                    if (isUserLoggedIn && requestingUserId.equals(user.getId())) {
                        continue; // don't add this user to see himself in the dropdown
                    }
                    UserDto userDto = packageSimpleUserIntoDto(user, session);
                    userResults.add(userDto);
                }

                response.setSuccess(true);
                if (logger.isFiner()) {
                    logger.finer("Found %d users in all search.", users.size());
                }
            }
        } catch (Exception e) {
            logger.error("Exception in fetchAllUsers.", e);
        }

        return response;
    }

    public Response<Intuition> getIntuitionById(String intuitionId) {
        Response<Intuition> response = new Response();
        try {
            Intuition intuition = applicationDao.findById(intuitionId, Intuition.class);
            response.setData(intuition);
            response.setSuccess(true);
        } catch (Exception e) {
            logger.error("Exception in getIntuitionById: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> getIntuitionDtoById(String intuitionId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            Intuition intuition = applicationDao.findById(intuitionId, Intuition.class);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in getIntuitionDtoById: ", e);
        }
        return response;
    }

    public Response removeIntuition(Intuition intuition, User user) {
        Response response = new Response();
        try {
            applicationDao.removeNotificationByDataId(intuition.getId());
            applicationDao.remove(intuition);
            if (intuition.hasImageInfos()) {
                imageOperations.removeIntuitionImages(user.getUsername(), intuition);
            }
            response.setSuccess(true);
        } catch (Exception e) {
            logger.error("Exception in removeIntuition: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> addIntuition(Intuition intuition, FileInfo intuitionPictureFileInfo, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            User intuitionUser = accountService.simplifyUser(user);
            intuitionUser.setEmail(null);
            intuition.setUser(intuitionUser);
            intuition.getPredictedOutcome().setIntuitionOwnerContributed(true);

            OffsetDateTime nowUtc = utilityService.nowUtc();
            String nowUtcString = utilityService.toPersistentString(nowUtc);
            intuition.setInsertTimestamp(nowUtcString);
            OffsetDateTime expirationTimestamp = loadExpiration(nowUtc, intuition.getActiveWindow());
            intuition.setExpirationTimestamp(utilityService.toPersistentString(expirationTimestamp));
            intuition.setExpirationMillis(expirationTimestamp.toEpochSecond());

            User simplifiedUser = accountService.simplifyUser(user);
            for (Outcome potentialOutcome : intuition.getPotentialOutcomes()) {
                potentialOutcome.setIntuitionOwnerContributed(true);
                potentialOutcome.setContributorUser(simplifiedUser);
                potentialOutcome.setInsertTimestamp(nowUtcString);
            }

            applicationDao.addIntuition(intuition);

            // store to online image store
            if (intuitionPictureFileInfo != null) {
                List<FileInfo> intuitionImages = Arrays.asList(intuitionPictureFileInfo);
                List<String> urls = imageOperations.storeIntuitionImages(user.getUsername(), intuition.getId(), intuitionImages);
                urls.stream().forEach(url -> {
                    StoredImageInfo imageInfo = new StoredImageInfo(intuitionPictureFileInfo.name, url);
                    intuition.addImageInfo(imageInfo);
                });
                applicationDao.save(intuition);
            }

            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);

            // fire email
            StringBuilder builder = new StringBuilder();
            builder.append("<p>Name: ").append(user.getFullName()).append("</p>");
            builder.append("<p>Username: ").append(user.getUsername()).append("</p>");
            builder.append("<p>Intuition: ").append(intuition.getIntuitionText()).append("</p>");
            String msg = builder.toString();
            utilityService.sendHtmlEmail("willjstevens@gmail.com", "NEW INTUITION from SocialIntuition.co", msg);

            response.setData(intuitionDto);
            response.setSuccess(true);
        } catch (Exception e) {
            logger.error("Exception in addIntuition: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> setOutcome(Outcome outcome, String intuitionId, Session session) {
        Response<IntuitionDto> response = new Response();
        try {
            // set outcome
            Intuition intuition = applicationDao.findById(intuitionId, Intuition.class);
            String predictionId = intuition.getPredictedOutcome().getId();
            boolean isCorrect = predictionId.equals(outcome.getId());
            outcome.setCorrect(isCorrect);
            outcome.setInsertTimestamp(utilityService.nowUtcPersistentString());
            intuition = applicationDao.setOutcome(outcome, intuitionId);

            User user = session.getUser();
            String userId = user.getId();

            if (intuition.doScoreIntuition()) {
                // increment score for owner and cohorts
                Intuition simplifiedIntuition = simplifyIntuition(intuition);
                if (isCorrect) {
                    applicationDao.incrementScoreOwnedCorrect(userId, simplifiedIntuition);
                } else {
                    applicationDao.incrementScoreOwnedIncorrect(userId, simplifiedIntuition);
                }
                for (Outcome potentialOutcome : intuition.getPotentialOutcomes()) {
                    boolean isCorrectOutcome = potentialOutcome.getId().equals(predictionId);
                    for (User cohortVoter : potentialOutcome.getOutcomeVoters()) {
                        if (isCorrectOutcome) {
                            applicationDao.incrementScoreCohortCorrect(cohortVoter.getId(), simplifiedIntuition);
                        } else {
                            applicationDao.incrementScoreCohortIncorrect(cohortVoter.getId(), simplifiedIntuition);
                        }
                    }
                }
            }

            // blast notifications if right visibility
            if (utilityService.isIntuitionVisible(intuition, VISIBILITY_PUBLIC, VISIBILITY_COHORT)) {
                String message = String.format("%s set the outcome of an intuition. Check it out!", user.getFullName());
                for (String cohortId : session.getCohortIds()) {
                    Notification<Intuition> notification = new Notification();
                    notification.setUserId(cohortId);
                    notification.setType("outcome-set");
                    notification.setMessage(message);
                    notification.setData(intuition);
                    notification.setInsertTimestamp(utilityService.nowUtcPersistentString());
                    applicationDao.save(notification);
                }
            }

            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setData(intuitionDto);
            response.setSuccess(true);
        } catch (Exception e) {
            logger.error("Exception in setOutcome: ", e);
        }
        return response;
    }

    @Scheduled(fixedDelay=30000)
    public void setExpiredOutcomes() {
        long nowMillis = utilityService.nowUtc().toEpochSecond();
        List<Intuition> intuitions = applicationDao.findExpiredIntuitions(nowMillis);
        if (!intuitions.isEmpty()) {
            for (Intuition intuition : intuitions) {
                // first setup and save expired outcome
                Outcome outcome = new Outcome();
                outcome.setId(ObjectId.get().toString());
                outcome.setWrongByExpiration(true);
                outcome.setInsertTimestamp(utilityService.nowUtcPersistentString());
                intuition.setOutcome(outcome);
                applicationDao.save(intuition);

                if (intuition.doScoreIntuition()) {
                    // now increment score of expired outcome for owner only
                    Intuition simplifiedIntuition = simplifyIntuition(intuition);
                    applicationDao.incrementScoreOwnedIncorrect(intuition.getUser().getId(), simplifiedIntuition);
                }
            }
            logger.info("Set %d intuitions outcome to wrong by expiration.", intuitions.size());
        }
    }

    public Response<IntuitionDto> addLike(Like like, String intuitionId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
//            like.setUserId(user.getId());
//            like.setUserFullName(user.getFullName());
//            like.setUserImageInfo(user.getImageInfo());
//            like.setUsername(user.getUsername());
            like.setUser(accountService.simplifyUser(user));
            Intuition intuition = applicationDao.addLike(like, intuitionId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in addLike: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> removeLike(Like like, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            Intuition intuition = applicationDao.removeLike(like);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in removeLike: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> addComment(Comment comment, String intuitionId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            comment.setUser(accountService.simplifyUser(user));
            comment.setInsertTimestamp(utilityService.nowUtcPersistentString());
            Intuition intuition = applicationDao.addComment(comment, intuitionId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in addComment: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> removeComment(Comment comment, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            Intuition intuition = applicationDao.removeComment(comment);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in removeComment: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> addCommentLike(Like like, String intuitionId, String commentId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            like.setUser(accountService.simplifyUser(user));
            Intuition intuition = applicationDao.addCommentLike(like, intuitionId, commentId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in addCommentLike: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> removeCommentLike(Like like, String intuitionId, String commentId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            Intuition intuition = applicationDao.removeCommentLike(like, intuitionId, commentId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in removeCommentLike: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> addOutcomeLike(Like like, String intuitionId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            like.setUser(accountService.simplifyUser(user));
            Intuition intuition = applicationDao.addOutcomeLike(like, intuitionId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in addLike: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> removeOutcomeLike(Like like, String intuitionId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            Intuition intuition = applicationDao.removeOutcomeLike(like, intuitionId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in removeLike: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> addOutcomeComment(Comment comment, String intuitionId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            comment.setUser(accountService.simplifyUser(user));
            comment.setInsertTimestamp(utilityService.nowUtcPersistentString());
            Intuition intuition = applicationDao.addOutcomeComment(comment, intuitionId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in addOutcomeComment: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> removeOutcomeComment(Comment comment, String intuitionId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            Intuition intuition = applicationDao.removeOutcomeComment(comment, intuitionId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in removeOutcomeComment: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> addOutcomeCommentLike(Like like, String intuitionId, String commentId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            like.setUser(accountService.simplifyUser(user));
            Intuition intuition = applicationDao.addOutcomeCommentLike(like, intuitionId, commentId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in addOutcomeCommentLike: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> removeOutcomeCommentLike(Like like, String intuitionId, String commentId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            Intuition intuition = applicationDao.removeOutcomeCommentLike(like, intuitionId, commentId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in removeOutcomeCommentLike: ", e);
        }
        return response;
    }


    public Response<IntuitionDto> addPotentialOutcome(String predictionText, String intuitionId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            String requestingUserId = user.getId();
            Outcome potentialOutcome = new Outcome();
            potentialOutcome.setPredictionText(predictionText);
            potentialOutcome.setContributorUser(accountService.simplifyUser(user));
            potentialOutcome.setInsertTimestamp(utilityService.nowUtcPersistentString());
            Intuition intuition = applicationDao.findById(intuitionId, Intuition.class);
            potentialOutcome.setIntuitionOwnerContributed(requestingUserId.equals(intuition.getUser().getId()));

            intuition = applicationDao.addPotentialOutcome(potentialOutcome, intuitionId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);

            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in addPotentialOutcome: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> removePotentialOutcome(Outcome potentialOutcome, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            Intuition intuition = applicationDao.removePotentialOutcome(potentialOutcome);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in removePotentialOutcome: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> addCohortPrediction(String intuitionId, String predictedOutcomeId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            User cohort = accountService.simplifyUser(user);
            Intuition intuition = applicationDao.addOutcomeVoter(cohort, intuitionId, predictedOutcomeId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in addCohortPrediction: ", e);
        }
        return response;
    }

    public Response<IntuitionDto> removeCohortPrediction(String intuitionId, String predictedOutcomeId, User user) {
        Response<IntuitionDto> response = new Response();
        try {
            User cohort = accountService.simplifyUser(user);
            Intuition intuition = applicationDao.removeOutcomeVoter(cohort, intuitionId, predictedOutcomeId);
            IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
            response.setSuccess(true);
            response.setData(intuitionDto);
        } catch (Exception e) {
            logger.error("Exception in removeCohortPrediction: ", e);
        }
        return response;
    }

    public Response<List<IntuitionDto>> fetchIntuitionsForProfile(String profileUsername, Session requestingUserSession, int start, int quantity) {
        Response<List<IntuitionDto>> response = new Response();
        try {
            User requestingUser = requestingUserSession != null ? requestingUserSession.getUser() : null;
            List<Intuition> intuitions = null;
            // the request for profile intuitions will only have a session, if the user is logged in.  If the user is not
            //      logged in, then only public intuitions for the profile will be served up
            if (requestingUserSession != null) {
                if (profileUsername.equals(requestingUser.getUsername())) {
                    // if the profile username is the same as the requesting user, then the user is requesting his own
                    //      profile and should get access to all private, cohort and public intuitions
                    intuitions = applicationDao.fetchAllVisibilityIntuitionsForUser(requestingUser, start, quantity);
                } else if (requestingUserSession.hasCohort(profileUsername)) {
                    // if this user is has the requested profile username as one of his cohorts then load public and cohort intuitions
                    intuitions = applicationDao.fetchCohortAndPublicVisibleIntuitionsForUser(profileUsername, start, quantity);
                } else {
                    // if not profile not his own and not a cohort, then only allow public
                    intuitions = applicationDao.fetchPublicVisibleIntuitionsForUser(profileUsername, start, quantity);
                }
            } else {
                // if no requesting user session, then the user is not logged in and we cannot identify his allowable
                //      access to the profile intuitions.  Therefore allow only public intuitions
                intuitions = applicationDao.fetchPublicVisibleIntuitionsForUser(profileUsername, start, quantity);
                // person not logged in so he is unidentified; setup and load a mock object
                requestingUser = User.newUnidentifiedUser();
            }

            List<IntuitionDto> intuitionDtos = new ArrayList<>();
            // prep for display
            for (Intuition intuition : intuitions) {
                IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, requestingUser);
                intuitionDtos.add(intuitionDto);
            }

            response.setSuccess(true);
            response.setData(intuitionDtos);
            if (logger.isFiner()) {
                logger.finer("Returning %d intuitions for fetchIntuitionsForProfile.", intuitionDtos.size());
            }
        } catch (Exception e) {
            logger.error("Exception in fetchIntuitionsForProfile: ", e);
        }
        return response;
    }

    public Response<List<IntuitionDto>> fetchIntuitionsForActivityFeed(User user, List<String> cohortIds, int start, int quantity, Optional<String> lastUpdateTimestamp) {
        Response<List<IntuitionDto>> response = new Response();
        try {
            // NOTE: Fetching intuitions for only users and cohorts is turned OFF for now

            // TODO: REMOVE ME
//            cohortIds.clear();

//            List<Intuition> intuitions = applicationDao.fetchIntuitions(user, cohortIds, start, quantity, lastUpdateTimestamp);
            List<Intuition> intuitions = applicationDao.fetchAllIntuitionsFromCommunity(user, start, quantity, lastUpdateTimestamp);

            List<IntuitionDto> intuitionDtos = new ArrayList<>();
            // prep for display
            for (Intuition intuition : intuitions) {
                IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
                // always set to true since the activity feed is either his/her private intuitions or cohort's
                intuitionDto.setCanMakeSocialContributions(true);
                intuitionDtos.add(intuitionDto);
            }

            response.setLastUpdateTimestamp(utilityService.nowUtcPersistentString());
            response.setData(intuitionDtos);
            response.setSuccess(true);
            if (logger.isFiner()) {
                logger.finer("Returning %d intuitions.", intuitionDtos.size());
            }
        } catch (Exception e) {
            logger.error("Exception in : ", e);
        }
        return response;
    }

    public Response<IntuitionDto> fetchTopIntuition(User user) {
        Response<IntuitionDto> response = new Response();
        try {
            // NOTE: Fetching intuitions for only users and cohorts is turned OFF for now
            List<Intuition> intuitions = applicationDao.fetchTopIntuition(user);

            List<IntuitionDto> intuitionDtos = new ArrayList<>();
            // prep for display
            for (Intuition intuition : intuitions) {
                IntuitionDto intuitionDto = packageIntuitionIntoDto(intuition, user);
                // always set to true since the activity feed is either his/her private intuitions or cohort's
                intuitionDto.setCanMakeSocialContributions(true);
                intuitionDtos.add(intuitionDto);
            }

            response.setSuccess(true);
            response.setData(intuitionDtos.get(0));
            if (logger.isFiner()) {
                logger.finer("Returning %d intuitions.", intuitionDtos.size());
            }
        } catch (Exception e) {
            logger.error("Exception in : ", e);
        }
        return response;
    }

    private IntuitionDto packageIntuitionIntoDto(Intuition intuition, User requestingUser) {
        IntuitionDto intuitionDto = null;

        String requestingUserId = requestingUser.getId();
        OffsetDateTime nowUtc = utilityService.nowUtc();
        OffsetDateTime intuitionTimestamp = utilityService.toUtcOffsetDateTime(intuition.getInsertTimestamp());
        OffsetDateTime expirationTimestamp = utilityService.toUtcOffsetDateTime(intuition.getExpirationTimestamp());

        // set state flags
        boolean isExpired = nowUtc.isAfter(expirationTimestamp);
        boolean hasOutcome = intuition.getOutcome() != null;

        OffsetDateTime intuitionEstTime = utilityService.utcToEst(intuitionTimestamp);
        OffsetDateTime expirationEstTime = utilityService.utcToEst(expirationTimestamp);

        String postPrettyTime = utilityService.toPrettyTime(intuitionEstTime);
        String expirationPrettyTime = utilityService.toPrettyTime(expirationEstTime);
        boolean isIntuitionOwner = intuition.getUser().getId().equals(requestingUserId);
        intuitionDto = new IntuitionDto(intuition, isIntuitionOwner, postPrettyTime, expirationPrettyTime);
        intuitionDto.setPostTimestamp(utilityService.toDisplayTimestamp(intuitionEstTime));
        intuitionDto.setExpirationTimestamp(utilityService.toDisplayTimestamp(expirationEstTime));

        // active if either still before the expiration or no outcome has yet been set
        boolean isActive = !isExpired && !hasOutcome;
        intuitionDto.setActive(isActive);

        if (!isActive) { // if outcome is set, then set right/wrong
            boolean isResultSet = false;
            if (!isIntuitionOwner && intuition.isAllowPredictedOutcomeVoting()) {
                String outcomeId = intuition.getOutcome().getId();
                for (Outcome potentialOutcome : intuition.getPotentialOutcomes()) {
                    for (User outcomeVoter : potentialOutcome.getOutcomeVoters()) {
                        if (outcomeVoter.getId().equals(requestingUserId) && !requestingUser.isGuest()) {
                            boolean isCorrect = outcomeId.equals(potentialOutcome.getId());
                            intuitionDto.setIsCorrect(isCorrect);
                            isResultSet = true;
                            break;
                        }
                    }
                }
            }
            if (!isResultSet) { // if not set individually, then default to the intuition owner's result
                intuitionDto.setIsCorrect(intuition.getOutcome().isCorrect());
            }
        }

//        boolean isLoggedIn = requestingUserId != null;
        boolean isLoggedIn = !requestingUser.isUnidentified();
        intuitionDto.setInteractive(true);

        if (intuition.isAllowCohortsToContributePredictedOutcomes() &&
                (isLoggedIn || requestingUser.isGuest()) &&
                utilityService.isIntuitionVisible(intuition, VISIBILITY_COHORT, VISIBILITY_PUBLIC) &&
                intuition.getPredictionType().equals(PREDICTION_TYPE_MULTIPLE_CHOICE)) {
            intuitionDto.setCanContributeOutcome(true);
        }

        if (isIntuitionOwner) {
            // always display for owner
            intuition.setDisplayPrediction(true);
        }

        // pack up data and personalize to current user
        for (Like like : intuition.getLikes()) {
            boolean isLikeOwner = requestingUserId.equals(like.getUser().getId());
            LikeDto likeDto = new LikeDto(like, isLikeOwner);
            if (isLikeOwner) {
                intuitionDto.setSelfLikeDto(likeDto);
            } else {
                if (like.getUser().isGuest()) {
                    intuitionDto.addGuestLikeDto(likeDto);
                } else {
                    intuitionDto.addLikeDto(likeDto);
                }
            }
        }
        for (Comment comment : intuition.getComments()) {
            boolean isCommentOwner = requestingUserId.equals(comment.getUser().getId());
            OffsetDateTime commentTimestamp = utilityService.toEstOffsetDateTime(comment.getInsertTimestamp());
            String commentPrettyTime = utilityService.toPrettyTime(commentTimestamp);
            CommentDto commentDto = new CommentDto(comment, isCommentOwner, commentPrettyTime);
            commentDto.setDisplayTimestamp(utilityService.toDisplayTimestamp(commentTimestamp));
            intuitionDto.addCommentDto(commentDto);
            for (Like like : comment.getLikes()) {
                boolean isLikeOwner = requestingUserId.equals(like.getUser().getId());
                LikeDto likeDto = new LikeDto(like, isLikeOwner);
                if (isLikeOwner) {
                    commentDto.setSelfLikeDto(likeDto);
                } else {
                    if (like.getUser().isGuest()) {
                        commentDto.addGuestLikeDto(likeDto);
                    } else {
                        commentDto.addLikeDto(likeDto);
                    }
                }
            }
        }

        if ((isLoggedIn || requestingUser.isGuest()) &&
                !isIntuitionOwner &&
                intuition.isAllowPredictedOutcomeVoting()) {
            intuitionDto.setCanVote(true); // default to true and switch off if already voted
        }

        Outcome predictedOutcome = intuition.getPredictedOutcome();
        for (Outcome potentialOutcome : intuition.getPotentialOutcomes()) {
            boolean isPredictionChoiceOwner = potentialOutcome.getContributorUser().getId().equals(requestingUserId);
            OffsetDateTime predictionChoiceEstTimestamp = utilityService.toEstOffsetDateTime(potentialOutcome.getInsertTimestamp());
            String predictionChoicePrettyTime = utilityService.toPrettyTime(predictionChoiceEstTimestamp);
            OutcomeDto potentialOutcomeDto = new OutcomeDto(potentialOutcome, isPredictionChoiceOwner, predictionChoicePrettyTime);
            intuitionDto.addPotentialOutcomeDto(potentialOutcomeDto);
            if (predictedOutcome.getId().equals(potentialOutcome.getId())) {
                potentialOutcomeDto.setPredicted(true);
            }
            for (User outcomeVoter : potentialOutcome.getOutcomeVoters()) {
                if (outcomeVoter.getId().equals(requestingUserId)) {
                    intuitionDto.setCohortVotedOutcomeDto(potentialOutcomeDto);
                    intuitionDto.setCanVote(false); // already voted
                    break;
                }
            }
        }
        Outcome outcome = intuition.getOutcome();
        if (outcome != null) {
            OffsetDateTime outcomeEstTimestamp = utilityService.toEstOffsetDateTime(outcome.getInsertTimestamp());
            String outcomePrettyTime = utilityService.toPrettyTime(outcomeEstTimestamp);

            OutcomeDto outcomeDto = new OutcomeDto(outcome, isIntuitionOwner, outcomePrettyTime);
            intuitionDto.setOutcomeDto(outcomeDto);

            for (Like like : outcome.getLikes()) {
                boolean isLikeOwner = like.getUser().getId().equals(requestingUserId);
                LikeDto likeDto = new LikeDto(like, isLikeOwner);
                if (isLikeOwner) {
                    outcomeDto.setSelfLikeDto(likeDto);
                } else {
                    if (like.getUser().isGuest()) {
                        outcomeDto.addGuestLikeDto(likeDto);
                    } else {
                        outcomeDto.addLikeDto(likeDto);
                    }
                }
            }
            for (Comment comment : outcome.getComments()) {
                boolean isCommentOwner = comment.getUser().getId().equals(requestingUserId);
                OffsetDateTime commentTimestamp = utilityService.toEstOffsetDateTime(comment.getInsertTimestamp());
                String commentPrettyTime = utilityService.toPrettyTime(commentTimestamp);
                CommentDto commentDto = new CommentDto(comment, isCommentOwner, commentPrettyTime);
                commentDto.setDisplayTimestamp(utilityService.toDisplayTimestamp(commentTimestamp));
                outcomeDto.addCommentDto(commentDto);
                for (Like like : comment.getLikes()) {
                    boolean isLikeOwner = like.getUser().getId().equals(requestingUserId);
                    LikeDto likeDto = new LikeDto(like, isLikeOwner);
                    if (isLikeOwner) {
                        commentDto.setSelfLikeDto(likeDto);
                    } else {
                        commentDto.addLikeDto(likeDto);
                    }
                }
            }
        }

        adjustForPrivacy(intuitionDto, requestingUser);

        // scrub sensitive data
        if (!isIntuitionOwner && !intuition.isDisplayPrediction() && isActive) {
            intuition.setPredictedOutcome(null); // prevent from going out to client
        }


        return intuitionDto;
    }

    public void adjustForPrivacy(IntuitionDto intuitionDto, User requestingUser) {
        String profileUsername = intuitionDto.getIntuition().getUser().getUsername();
        // the request for profile intuitions will only have a session, if the user is logged in.  If the user is not
        //      logged in, then only public intuitions for the profile will be served up
        boolean isLoggedIn = !requestingUser.isUnidentified();
        if (isLoggedIn) {
            Session requestingUserSession = sessionManager.getSessionByUser(requestingUser);
            if (profileUsername.equals(requestingUser.getUsername())) {
                // if the profile username is the same as the requesting user, then the user is requesting his own
                //      profile and should get access to all private, cohort and public intuitions
                intuitionDto.setCanMakeSocialContributions(true);
            } else if (requestingUserSession != null && requestingUserSession.hasCohort(profileUsername)) {
                // if this user is has the requested profile username as one of his cohorts then load public and cohort intuitions
                intuitionDto.setCanMakeSocialContributions(true);
            } else if (utilityService.isIntuitionVisible(intuitionDto.getIntuition(), VISIBILITY_PUBLIC)) {
                intuitionDto.setCanMakeSocialContributions(true);
            }
        } else if (requestingUser.isGuest() && utilityService.isIntuitionVisible(intuitionDto.getIntuition(), VISIBILITY_PUBLIC)) {
            intuitionDto.setCanMakeSocialContributions(true);
        } else {
            // if requesting user is unidentified, then the user is not logged in and we cannot identify his allowable
            //      access to the profile intuitions.  Therefore allow only public intuitions
        }
    }

    public Response addFeedback(Feedback feedback) {
        Response response = new Response();
        try {
            feedback.setInsertTimestamp(utilityService.nowUtcPersistentString());
            applicationDao.save(feedback);

            StringBuilder builder = new StringBuilder();
            builder.append("Message from: ");
            builder.append("<p>Email: ").append(feedback.getEmail()).append("</p>");
            builder.append("<p>Name: ").append(feedback.getName()).append("</p>");
            builder.append("<p>Comment: ").append(feedback.getComment()).append("</p>");
            String msg = builder.toString();
            utilityService.sendHtmlEmail("willjstevens@gmail.com", "FEEDBACK from SocialIntuition.co", msg);

            response.setSuccess(true);
            if (logger.isInfo()) {
                logger.info("Feedback message email: \"%s\".", msg);
            }
        } catch (Exception e) {
            logger.error("Error occurred when saving feedback from user %s and email %s.", e, feedback.getName(), feedback.getEmail());
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }
        return response;
    }

    public Response fetchReferral(String referralCode) {
        Response<Referral> response = new Response();
        try {
            Referral referral = null;
            if (referralCache.containsKey(referralCode)) {
                referral = referralCache.get(referralCode);
            } else {
                referral = applicationDao.findReferralByCode(referralCode);
                referralCache.put(referralCode, referral);
            }

            response.setData(referral);
            response.setSuccess(true);
            if (logger.isFine()) {
                logger.fine("Fetched referral for code \"%s\" for title \"%s\".", referralCode, referral.getTitle());
            }
        } catch (Exception e) {
            logger.error("Error occurred when fetching referral for code \"%s\".", e, referralCode);
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }
        return response;
    }

    public Response fetchClientConfig() {
        Response response = new Response();
        try {
            response.setData(configurationService.getClientConfig());
            response.setSuccess(true);
        } catch (Exception e) {
            logger.error("Error occurred when fetching and loading the client config.", e);
            ServerErrorPageResultException throwMe = getServerErrorPageResultException(response);
            throw throwMe;
        }
        return response;
    }

    private UserDto packageUserIntoDto(User targetUser, Session requestingUserSession) {
        UserDto userDto = new UserDto();
        userDto.setUser(targetUser);
        if (requestingUserSession != null) { // could be null if user not logged in
            requestingUserSession.hasCohort(targetUser.getUsername());
        }
        return userDto;
    }

    private UserDto packageSimpleUserIntoDto(User targetUser, Session requestingUserSession) {
        User simpleUser = accountService.simplifyUser(targetUser);
        return packageUserIntoDto(simpleUser, requestingUserSession);
    }

    private OffsetDateTime loadExpiration(OffsetDateTime startDate, String expirationInterval) {
        OffsetDateTime expirationOffsetDateTime = startDate;

        switch (expirationInterval) {
            case "minutes-5":
                expirationOffsetDateTime = startDate.plusMinutes(5);
                break;
            case "hours-1":
                expirationOffsetDateTime = startDate.plusHours(1);
                break;
            case "hours-6":
                expirationOffsetDateTime = startDate.plusHours(6);
                break;
            case "hours-12":
                expirationOffsetDateTime = startDate.plusHours(12);
                break;
            case "days-1":
                expirationOffsetDateTime = startDate.plusDays(1);
                break;
            case "days-5":
                expirationOffsetDateTime = startDate.plusDays(5);
                break;
            case "weeks-1":
                expirationOffsetDateTime = startDate.plusWeeks(1);
                break;
            case "weeks-2":
                expirationOffsetDateTime = startDate.plusWeeks(2);
                break;
            case "months-1":
                expirationOffsetDateTime = startDate.plusMonths(1);
                break;
            case "months-6":
                expirationOffsetDateTime = startDate.plusMonths(6);
                break;
            case "years-1":
                expirationOffsetDateTime = startDate.plusYears(1);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized expirationInterval: " + expirationInterval);
        }

        return expirationOffsetDateTime;
    }

    private List<ActiveWindow> loadActiveWindows() {
        List<ActiveWindow> activeWindows = new ArrayList<>();
        activeWindows.add(new ActiveWindow("minutes-5", "5 Minutes"));
        activeWindows.add(new ActiveWindow("hours-1", "1 Hour"));
        activeWindows.add(new ActiveWindow("hours-6", "6 Hours"));
        activeWindows.add(new ActiveWindow("hours-12", "12 Hours"));
        activeWindows.add(new ActiveWindow("days-1", "1 Day"));
        activeWindows.add(new ActiveWindow("days-5", "5 Days"));
        activeWindows.add(new ActiveWindow("weeks-1", "1 Week"));
        activeWindows.add(new ActiveWindow("weeks-2", "2 Weeks"));
        activeWindows.add(new ActiveWindow("months-1", "1 Month"));
        activeWindows.add(new ActiveWindow("months-6", "6 Months"));
        activeWindows.add(new ActiveWindow("years-1", "1 Year"));
        return activeWindows;
    }

    private List<Outcome> loadPredictionChoicesYesNo() {
        List<Outcome> predictionChoices = new ArrayList<>();
        predictionChoices.add(new Outcome("Yes"));
        predictionChoices.add(new Outcome("No"));
        return predictionChoices;
    }

    private List<Outcome> loadPredictionChoicesTrueFalse() {
        List<Outcome> predictionChoices = new ArrayList<>();
        predictionChoices.add(new Outcome("True"));
        predictionChoices.add(new Outcome("False"));
        return predictionChoices;
    }

    public Intuition simplifyIntuition(Intuition intuition) {
        Intuition simpleIntuition = new Intuition();
        simpleIntuition.setId(intuition.getId());
        simpleIntuition.setUser(intuition.getUser());
        simpleIntuition.setIntuitionText(intuition.getIntuitionText());
        simpleIntuition.setInsertTimestamp(intuition.getInsertTimestamp());
        simpleIntuition.setOutcome(intuition.getOutcome());
        return simpleIntuition;
    }
}
