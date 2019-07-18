package com.si.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.si.Category;
import com.si.dto.IntuitionDto;
import com.si.dto.NewIntuitionDto;
import com.si.entity.*;
import com.si.framework.*;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.AccountService;
import com.si.service.ApplicationService;
import com.si.service.ConfigurationService;
import com.si.service.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static com.si.Constants.DEVICE_ID;
import static com.si.Constants.USER_ID;
import static com.si.UrlConstants.INTUITION_API;
import static com.si.UrlConstants.INTUITION_NEW_SETTINGS;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping(INTUITION_API)
@RestController
public class ApiRestIntuitionController
{
	private static final Logger logger = LogManager.manager().newLogger(ApiRestIntuitionController.class, Category.CONTROLLER);
	@Autowired private SessionManager sessionManager;
	@Autowired private AccountService accountService;
    @Autowired private ApplicationService applicationService;
    @Autowired private UtilityService utilityService;
    @Autowired private ConfigurationService configurationService;


    @RequestMapping(value="/{id}", method=GET, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> getIntuitionByDevice(@RequestHeader(USER_ID) String userId,
                                                          @RequestHeader(DEVICE_ID) String deviceId,
                                                          HttpSession httpSession,
                                                          @PathVariable String id) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.getIntuitionDtoById(id, user);
    }

    @RequestMapping(value="/{id}", method=GET)
    public Response<IntuitionDto> getIntuitionByWeb(HttpSession httpSession,
                                                     @PathVariable String id,
                                                     @RequestParam(required=false) boolean isGuest) {
        User user = sessionManager.getUserForWeb(httpSession, isGuest);
        return applicationService.getIntuitionDtoById(id, user);
    }


    @RequestMapping(value=INTUITION_NEW_SETTINGS, method=GET, headers={USER_ID, DEVICE_ID})
    public Response<NewIntuitionDto> getNewIntuitionSettingsByDevice(@RequestHeader(USER_ID) String userId,
                                                            @RequestHeader(DEVICE_ID) String deviceId,
                                                            HttpSession httpSession) {
        return applicationService.getNewIntuitionDto();
    }

    @RequestMapping(value=INTUITION_NEW_SETTINGS, method=GET)
    public Response<NewIntuitionDto> getNewIntuitionSettingsByWeb() {
        return applicationService.getNewIntuitionDto();
    }

    @RequestMapping(value="/fetch/activity", method=GET, headers={USER_ID, DEVICE_ID})
    public Response<List<IntuitionDto>> fetchIntuitionsForActivityFeedByDevice(@RequestHeader(USER_ID) String userId,
                                                                               @RequestHeader(DEVICE_ID) String deviceId,
                                                                               HttpSession httpSession,
                                                                               @RequestParam int start,
                                                                               @RequestParam int quantity,
                                                                               @RequestParam(required=false) Optional<String> lastUpdateTimestamp) {
        Session session = sessionManager.getSessionForDevice(userId, deviceId, httpSession);
        User user = session.getUser();
        List<String> cohortIds = session.getCohortIds();
        return applicationService.fetchIntuitionsForActivityFeed(user, cohortIds, start, quantity, lastUpdateTimestamp);
    }

    @RequestMapping(value="/fetch/activity", method=GET)
    public Response<List<IntuitionDto>> fetchIntuitionsForActivityFeedByWeb(HttpSession httpSession,
                                                                              @RequestParam int start,
                                                                              @RequestParam int quantity,
                                                                                @RequestParam(required=false) Optional<String> lastUpdateTimestamp) {
        Session session = sessionManager.getSession(httpSession);
        User user = session.getUser();
        List<String> cohortIds = session.getCohortIds();
        return applicationService.fetchIntuitionsForActivityFeed(user, cohortIds, start, quantity, lastUpdateTimestamp);
    }

    @RequestMapping(value="/fetch/top", method=GET, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> fetchTopIntuitionByDevice(@RequestHeader(USER_ID) String userId,
                                                           @RequestHeader(DEVICE_ID) String deviceId,
                                                           HttpSession httpSession) {
        Session session = sessionManager.getSessionForDevice(userId, deviceId, httpSession);
        User user = session.getUser();
        return applicationService.fetchTopIntuition(user);
    }

    @RequestMapping(value="/fetch/profile/{username}", method=GET, headers={USER_ID, DEVICE_ID})
    public Response<List<IntuitionDto>> fetchIntuitionsForProfileByDevice(@RequestHeader(USER_ID) String userId,
                                                                            @RequestHeader(DEVICE_ID) String deviceId,
                                                                            HttpSession httpSession,
                                                                            @PathVariable String username,
                                                                            @RequestParam int start,
                                                                            @RequestParam int quantity) {
        Session session = sessionManager.getSessionForDevice(userId, deviceId, httpSession);
        return applicationService.fetchIntuitionsForProfile(username, session, start, quantity);
    }

    @RequestMapping(value="/fetch/profile/{username}", method=GET)
    public Response<List<IntuitionDto>> fetchIntuitionsForProfileByWeb(HttpSession httpSession,
                                                                         @PathVariable String username,
                                                                         @RequestParam int start,
                                                                         @RequestParam int quantity,
                                                                         @RequestParam(required=false) boolean isGuest) {
        Session session = sessionManager.getSession(httpSession, isGuest);
        return applicationService.fetchIntuitionsForProfile(username, session, start, quantity);
    }

    @RequestMapping(value="/add", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> addIntuitionByDevice(@RequestParam("request") String jsonRequestString,
                                                       @RequestParam(value="file", required=false) MultipartFile file,
                                                         @RequestHeader(USER_ID) String userId,
                                                         @RequestHeader(DEVICE_ID) String deviceId,
                                                         HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        Response<IntuitionDto> response = addIntuition(jsonRequestString, file, user);
        return response;
    }

    @RequestMapping(value="/add", method=POST)
    public Response<IntuitionDto> addIntuitionByWeb(@RequestParam("request") String jsonRequestString,
                                                    @RequestParam(value="file", required=false) MultipartFile file,
                                                    HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession);
        Response<IntuitionDto> response = addIntuition(jsonRequestString, file, user);
        return response;
    }

    private Response<IntuitionDto> addIntuition(String jsonRequestString, MultipartFile file, User user) {
        Response<IntuitionDto> response = null;
        // This manual JSON parsing is a workaround due to ngFileUpload not setting a content-type of
        //      application/json on the form field object. This causes it to not be interpreted as JSON
        //      but rather only a string. Therefore we need to manually parse it.
        final ObjectMapper mapper = new ObjectMapper();
        Request<Intuition> request = null;
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            request = mapper.readValue(jsonRequestString, new TypeReference<Request<Intuition>>() {});
        } catch (IOException e) {
            logger.error("Error when manually parising the JSON string for add intuition.", e);
        }

        FileInfo intuitionPictureFileInfo = null;
        InputStream inputStream = null;
        if (file != null) {
            try {
                inputStream = file.getInputStream();
                intuitionPictureFileInfo = new FileInfo(file.getOriginalFilename(), file.getContentType(), inputStream);
            } catch (IOException e) {
                logger.error("Error when getting input stream for profile photo upload.", e);
            }
        }
        try {
            response = applicationService.addIntuition(request.data, intuitionPictureFileInfo, user);
        } catch (Exception e) {
            logger.error("Error when adding an intuition with or without a picture.", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) { /* swallow */ }
        }
        return response;
    }

    @RequestMapping(value="/remove", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> removeIntuitionByDevice(@RequestBody Request<Intuition> request,
                                                       @RequestHeader(USER_ID) String userId,
                                                       @RequestHeader(DEVICE_ID) String deviceId,
                                                       HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.removeIntuition(request.data, user);
    }

    @RequestMapping(value="/remove", method=POST)
    public Response<IntuitionDto> removeIntuitionByWeb(@RequestBody Request<Intuition> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession);
        return applicationService.removeIntuition(request.data, user);
    }

    @RequestMapping(value="/outcome", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> setOutcomeByDevice(@RequestBody Request<Outcome> request,
                                                          @RequestHeader(USER_ID) String userId,
                                                          @RequestHeader(DEVICE_ID) String deviceId,
                                                          HttpSession httpSession) {
        Session session = sessionManager.getSessionForDevice(userId, deviceId, httpSession);
        return applicationService.setOutcome(request.data, request.intuitionId, session);
    }

    @RequestMapping(value="/outcome", method=POST)
    public Response<IntuitionDto> setOutcomeByWeb(@RequestBody Request<Outcome> request, HttpSession httpSession) {
        Session session = sessionManager.getSession(httpSession);
        return applicationService.setOutcome(request.data, request.intuitionId, session);
    }

    @RequestMapping(value="/like", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> addLikeByDevice(@RequestBody Request<Like> request,
                                                 @RequestHeader(USER_ID) String userId,
                                                 @RequestHeader(DEVICE_ID) String deviceId,
                                                 HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.addLike(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/like", method=POST)
    public Response<IntuitionDto> addLikeByWeb(@RequestBody Request<Like> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.addLike(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/like/remove", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> removeLikeByDevice(@RequestBody Request<Like> request,
                                                   @RequestHeader(USER_ID) String userId,
                                                   @RequestHeader(DEVICE_ID) String deviceId,
                                                   HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.removeLike(request.data, user);
    }

    @RequestMapping(value="/like/remove", method=POST)
    public Response<IntuitionDto> removeLikeByWeb(@RequestBody Request<Like> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.removeLike(request.data, user);
    }

    @RequestMapping(value="/comment", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> addCommentByDevice(@RequestBody Request<Comment> request,
                                                   @RequestHeader(USER_ID) String userId,
                                                   @RequestHeader(DEVICE_ID) String deviceId,
                                                   HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.addComment(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/comment", method=POST)
    public Response<IntuitionDto> addCommentByWeb(@RequestBody Request<Comment> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.addComment(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/comment/remove", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> removeCommentByDevice(@RequestBody Request<Comment> request,
                                                      @RequestHeader(USER_ID) String userId,
                                                      @RequestHeader(DEVICE_ID) String deviceId,
                                                      HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.removeComment(request.data, user);
    }

    @RequestMapping(value="/comment/remove", method=POST)
    public Response<IntuitionDto> removeCommentByWeb(@RequestBody Request<Comment> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.removeComment(request.data, user);
    }

    @RequestMapping(value="/comment/like", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> addCommentLikeByDevice(@RequestBody Request<Like> request,
                                                      @RequestHeader(USER_ID) String userId,
                                                      @RequestHeader(DEVICE_ID) String deviceId,
                                                      HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.addCommentLike(request.data, request.intuitionId, request.commentId, user);
    }

    @RequestMapping(value="/comment/like", method=POST)
    public Response<IntuitionDto> addCommentLikeByWeb(@RequestBody Request<Like> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.addCommentLike(request.data, request.intuitionId, request.commentId, user);
    }

    @RequestMapping(value="/comment/like/remove", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> removeCommentLikeByDevice(@RequestBody Request<Like> request,
                                                         @RequestHeader(USER_ID) String userId,
                                                         @RequestHeader(DEVICE_ID) String deviceId,
                                                         HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.removeCommentLike(request.data, request.intuitionId, request.commentId, user);
    }

    @RequestMapping(value="/comment/like/remove", method=POST)
    public Response<IntuitionDto> removeCommentLikeByWeb(@RequestBody Request<Like> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.removeCommentLike(request.data, request.intuitionId, request.commentId, user);
    }

    @RequestMapping(value="/outcome/like", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> addOutcomeLikeByDevice(@RequestBody Request<Like> request,
                                                       @RequestHeader(USER_ID) String userId,
                                                       @RequestHeader(DEVICE_ID) String deviceId,
                                                       HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.addOutcomeLike(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/outcome/like", method=POST)
    public Response<IntuitionDto> addOutcomeLikeByWeb(@RequestBody Request<Like> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.addOutcomeLike(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/outcome/like/remove", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> removeOutcomeLikeByDevice(@RequestBody Request<Like> request,
                                                          @RequestHeader(USER_ID) String userId,
                                                          @RequestHeader(DEVICE_ID) String deviceId,
                                                          HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.removeOutcomeLike(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/outcome/like/remove", method=POST)
    public Response<IntuitionDto> removeOutcomeLikeByWeb(@RequestBody Request<Like> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.removeOutcomeLike(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/outcome/comment", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> addOutcomeCommentByDevice(@RequestBody Request<Comment> request,
                                                          @RequestHeader(USER_ID) String userId,
                                                          @RequestHeader(DEVICE_ID) String deviceId,
                                                          HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.addOutcomeComment(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/outcome/comment", method=POST)
    public Response<IntuitionDto> addOutcomeCommentByWeb(@RequestBody Request<Comment> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.addOutcomeComment(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/outcome/comment/remove", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> removeOutcomeCommentByDevice(@RequestBody Request<Comment> request,
                                                             @RequestHeader(USER_ID) String userId,
                                                             @RequestHeader(DEVICE_ID) String deviceId,
                                                             HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.removeOutcomeComment(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/outcome/comment/remove", method=POST)
    public Response<IntuitionDto> removeOutcomeCommentByWeb(@RequestBody Request<Comment> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.removeOutcomeComment(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/outcome/comment/like", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> addOutcomeCommentLikeByDevice(@RequestBody Request<Like> request,
                                                              @RequestHeader(USER_ID) String userId,
                                                              @RequestHeader(DEVICE_ID) String deviceId,
                                                              HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.addOutcomeCommentLike(request.data, request.intuitionId, request.commentId, user);
    }

    @RequestMapping(value="/outcome/comment/like", method=POST)
    public Response<IntuitionDto> addOutcomeCommentLikeByWeb(@RequestBody Request<Like> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.addOutcomeCommentLike(request.data, request.intuitionId, request.commentId, user);
    }

    @RequestMapping(value="/outcome/comment/like/remove", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> removeOutcomeCommentLikeByDevice(@RequestBody Request<Like> request,
                                                                 @RequestHeader(USER_ID) String userId,
                                                                 @RequestHeader(DEVICE_ID) String deviceId,
                                                                 HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.removeOutcomeCommentLike(request.data, request.intuitionId, request.commentId, user);
    }

    @RequestMapping(value="/outcome/comment/like/remove", method=POST)
    public Response<IntuitionDto> removeOutcomeCommentLikeByWeb(@RequestBody Request<Like> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.removeOutcomeCommentLike(request.data, request.intuitionId, request.commentId, user);
    }

    @RequestMapping(value="/predicted-outcome", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> addPredictionChoiceByDevice(@RequestBody Request<String> request,
                                                  @RequestHeader(USER_ID) String userId,
                                                  @RequestHeader(DEVICE_ID) String deviceId,
                                                  HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.addPotentialOutcome(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/predicted-outcome", method=POST)
    public Response<IntuitionDto> addPredictionChoiceByWeb(@RequestBody Request<String> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.addPotentialOutcome(request.data, request.intuitionId, user);
    }

    @RequestMapping(value="/predicted-outcome/remove", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> removePredictionChoiceByDevice(@RequestBody Request<Outcome> request,
                                                     @RequestHeader(USER_ID) String userId,
                                                     @RequestHeader(DEVICE_ID) String deviceId,
                                                     HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.removePotentialOutcome(request.data, user);
    }

    @RequestMapping(value="/predicted-outcome/remove", method=POST)
    public Response<IntuitionDto> removePredictionChoiceByWeb(@RequestBody Request<Outcome> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.removePotentialOutcome(request.data, user);
    }

    @RequestMapping(value="/predicted-outcome/cohort-vote", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> addCohortVoteByDevice(@RequestBody Request<Outcome> request,
                                                      @RequestHeader(USER_ID) String userId,
                                                      @RequestHeader(DEVICE_ID) String deviceId,
                                                      HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.addCohortPrediction(request.intuitionId, request.data.getId(), user);
    }

    @RequestMapping(value="/predicted-outcome/cohort-vote", method=POST)
    public Response<IntuitionDto> addCohortVoteByWeb(@RequestBody Request<Outcome> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.addCohortPrediction(request.intuitionId, request.data.getId(), user);
    }

    @RequestMapping(value="/predicted-outcome/cohort-vote/remove", method=POST, headers={USER_ID, DEVICE_ID})
    public Response<IntuitionDto> removeCohortVoteByDevice(@RequestBody Request<Outcome> request,
                                                         @RequestHeader(USER_ID) String userId,
                                                         @RequestHeader(DEVICE_ID) String deviceId,
                                                         HttpSession httpSession) {
        User user = sessionManager.getUserForDevice(userId, deviceId, httpSession);
        return applicationService.removeCohortPrediction(request.intuitionId, request.data.getId(), user);
    }

    @RequestMapping(value="/predicted-outcome/cohort-vote/remove", method=POST)
    public Response<IntuitionDto> removeCohortVoteByWeb(@RequestBody Request<Outcome> request, HttpSession httpSession) {
        User user = sessionManager.getUserForWeb(httpSession, request.isGuest);
        return applicationService.removeCohortPrediction(request.intuitionId, request.data.getId(), user);
    }


}
