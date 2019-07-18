package com.si.dao;

import com.si.Category;
import com.si.Util;
import com.si.entity.*;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.si.Constants.VISIBILITY_COHORT;
import static com.si.Constants.VISIBILITY_PUBLIC;

/**
 * DAO implementation doing basic database operations.
 *
 * @author wstevens
 */
@Repository
public class ApplicationDao
{

    private static final Logger logger = LogManager.manager().newLogger(ApplicationDao.class, Category.DATABASE);
    @Autowired private MongoTemplate mongoTemplate;
    private FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();

    @PostConstruct
    public void init() {
        findAndModifyOptions.returnNew(true);
    }

    public void insert(Object object) {
        mongoTemplate.insert(object);
    }

    public void save(Object object) {
        mongoTemplate.save(object); // upsert
    }

    public void remove(Object object) {
        mongoTemplate.remove(object);
    }

    public void removeAll(Class klass) {
        mongoTemplate.findAllAndRemove(new Query(), klass);
    }

    public void removeNotificationByDataId(String dataId) {
        Query query = Query.query(Criteria.where("data._id").is(new ObjectId(dataId)));
        mongoTemplate.remove(query, Notification.class);
    }

    public <T> T findById(String id, Class<T> type) {
        Query query = new Query(Criteria.where("id").is(new ObjectId(id)));
        return mongoTemplate.findOne(query, type);
    }

    public List<Config> getAllConfigs() {
        return mongoTemplate.findAll(Config.class);
    }

    public Score findScoreByUserId(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.findOne(query, Score.class);
    }

    public User findUserById(String userId) {
        Query query = new Query(Criteria.where("id").is(userId));
        return mongoTemplate.findOne(query, User.class);
    }

	public User findUserByUsername(String username) {
        Query query = new Query(Criteria.where("username").is(username));
        return mongoTemplate.findOne(query, User.class);
    }

    public List<User> findUserByUsernameLikeString(String username) {
        Query query = new Query(Criteria.where("username").regex(username));
        return mongoTemplate.find(query, User.class);
    }

    public User findUserByEmail(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        return mongoTemplate.findOne(query, User.class);
    }

    public User findUserByCookieValue(String cookieValue) {
        Query query = new Query(Criteria.where("cookieValue").is(cookieValue));
        return mongoTemplate.findOne(query, User.class);
    }

    public Referral findReferralByCode(String referralCode) {
        Query query = new Query(Criteria.where("referralCode").is(referralCode));
        return mongoTemplate.findOne(query, Referral.class);
    }

    public List<User> findUsersByFirstAndLastNames(String... nameParts) {
        Query query = TextQuery.query(new TextCriteria().matchingAny(nameParts));
        return mongoTemplate.find(query, User.class);
    }

    public List<User> findUsersByNameInformation(String... nameParts) {
        List<Criteria> criterias = new ArrayList();
        for (String namePart : nameParts) {
            criterias.add(Criteria.where("username").regex(namePart, "i"));
            criterias.add(Criteria.where("fullName").regex(namePart, "i"));
        }
        Criteria criteria = new Criteria();
        criteria.orOperator(criterias.toArray(new Criteria[criterias.size()]));
        Query query = new Query(criteria);
        query.limit(50);
        List<User> users = mongoTemplate.find(query, User.class);
        return users;
    }

    public List<User> findAllUsers(int limit) {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria);
        query.limit(limit);
        List<User> users = mongoTemplate.find(query, User.class);
        return users;
    }

    public DeviceSession findAndUpdateDeviceSession(DeviceSession deviceSession) {
        Criteria deviceSessionCriteria = Criteria.where("userId").is(deviceSession.getUserId());
        deviceSessionCriteria = deviceSessionCriteria.and("deviceId").is(deviceSession.getDeviceId());
        Query query = new Query(deviceSessionCriteria);
        Update update = Update.update("httpSessionId", deviceSession.getHttpSessionId());
        update.set("updateTimestamp", Util.now());
        return mongoTemplate.findAndModify(query, update, findAndModifyOptions, DeviceSession.class);
    }

    public void deviceSessionLogin(DeviceSession deviceSession) {
        Criteria deviceSessionCriteria = Criteria.where("userId").is(deviceSession.getUserId());
        deviceSessionCriteria = deviceSessionCriteria.and("deviceId").is(deviceSession.getDeviceId());
        Query query = new Query(deviceSessionCriteria);
        Update update = Update.update("httpSessionId", deviceSession.getHttpSessionId());
        update = update.set("updateTimestamp", Util.now());
        mongoTemplate.upsert(query, update, DeviceSession.class);
    }

    public void deviceSessionLogout(DeviceSession deviceSession) {
        Criteria deviceSessionCriteria = Criteria.where("userId").is(deviceSession.getUserId());
        deviceSessionCriteria = deviceSessionCriteria.and("deviceId").is(deviceSession.getDeviceId());
        deviceSessionCriteria = deviceSessionCriteria.and("httpSessionId").is(deviceSession.getHttpSessionId());
        Query query = new Query(deviceSessionCriteria);
        mongoTemplate.findAndRemove(query, DeviceSession.class);
    }

    public void deviceSessionExpired(DeviceSession deviceSession) {
        Criteria deviceSessionCriteria = Criteria.where("userId").is(deviceSession.getUserId());
        deviceSessionCriteria = deviceSessionCriteria.and("deviceId").is(deviceSession.getDeviceId());
        Query query = new Query(deviceSessionCriteria);
        Update update = Update.update("httpSessionId", null);
        update = update.set("updateTimestamp", Util.now());
        mongoTemplate.findAndModify(query, update, DeviceSession.class);
    }

    public Cohort findCohortRequestForUser(String inviterUserId, String consenterUserId) {
        Criteria criteria = Criteria.where("inviterUserId").is(inviterUserId);
        criteria = criteria.and("consenterUserId").is(consenterUserId);
        criteria = criteria.and("isAccepted").is(false);
        criteria = criteria.and("isIgnored").is(false);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, Cohort.class);
    }

    public Cohort setCohortToAccepted(Cohort cohort) {
        Criteria criteria = Criteria.where("id").is(cohort.getId());
        Query query = new Query(criteria);
        Update update = Update.update("isAccepted", true);
        update = update.set("updateTimestamp", Util.now());
        return mongoTemplate.findAndModify(query, update, findAndModifyOptions, Cohort.class);
    }

    public Cohort setCohortToIgnored(Cohort cohort) {
        Criteria criteria = Criteria.where("id").is(cohort.getId());
        Query query = new Query(criteria);
        Update update = Update.update("isIgnored", true);
        update = update.set("updateTimestamp", Util.now());
        return mongoTemplate.findAndModify(query, update, findAndModifyOptions,  Cohort.class);
    }

    public Cohort unCohort(Cohort cohort) {
        Criteria criteria = Criteria.where("id").is(cohort.getId());
        Query query = new Query(criteria);
        Update update = Update.update("deleteTimestamp", Util.now());
        return mongoTemplate.findAndModify(query, update, findAndModifyOptions, Cohort.class);
    }

    public List<Cohort> findCohorts(User user) {
        String userId = user.getId();
        Criteria criteria = Criteria.where("isAccepted").is(true).orOperator(
                Criteria.where("inviterUserId").is(userId),
                Criteria.where("consenterUserId").is(userId)
        ).and("deleteTimestamp").is(null);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Cohort.class);
    }

    public List<Cohort> findCohortInvites(User user) {
        String userId = user.getId();
        Criteria criteria = Criteria.where("inviterUserId").is(userId);
        criteria = criteria.and("isAccepted").is(false);
        criteria = criteria.and("isIgnored").is(false);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Cohort.class);
    }

    public void addIntuition(Intuition intuition) {
        // set embedded object IDs
        Outcome prediction = intuition.getPredictedOutcome();
        for (Outcome potentialOutcome : intuition.getPotentialOutcomes()) {
            String newObjectId = newObjectId();
            potentialOutcome.setId(newObjectId);
            if (potentialOutcome.getPredictionText().equals(prediction.getPredictionText())) {
                prediction.setId(newObjectId); // the choice should match the prediction
            }
        }

        save(intuition);
    }

    public Intuition addLike(Like like, String intuitionId) {
        like.setId(newObjectId());
        Query query = new Query(Criteria.where("id").is(intuitionId));
        Update update = new Update().push("likes", like);
        return mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
    }

    public Intuition removeLike(Like like) {
        Query query = Query.query(Criteria.where("likes._id").is(new ObjectId(like.getId())));
        Update update = new Update().pull("likes", like);
        return mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
    }

    public Intuition addPotentialOutcome(Outcome predictedOutcome, String intuitionId) {
        predictedOutcome.setId(newObjectId());
        Query query = new Query(Criteria.where("id").is(intuitionId));
        Update update = new Update().push("potentialOutcomes", predictedOutcome);
        return mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
    }

    public Intuition removePotentialOutcome(Outcome predictedOutcome) {
        Query query = Query.query(Criteria.where("potentialOutcomes._id").is(new ObjectId(predictedOutcome.getId())));
        Update update = new Update().pull("potentialOutcomes", predictedOutcome);
        return mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
    }

    public Intuition addOutcomeVoter(User cohort, String intuitionId, String predictedOutcomeId) {
        Criteria criteria = Criteria
                .where("id").is(intuitionId)
                .and("potentialOutcomes").elemMatch(Criteria.where("_id").is(predictedOutcomeId));
        Query query = new Query(criteria);
        Update update = new Update().push("potentialOutcomes.$.outcomeVoters", cohort);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Intuition removeOutcomeVoter(User cohort, String intuitionId, String predictedOutcomeId) {
        Criteria criteria = Criteria
                .where("id").is(intuitionId)
                .and("potentialOutcomes").elemMatch(Criteria.where("_id").is(predictedOutcomeId));
        Query query = new Query(criteria);
        Update update = new Update().pull("potentialOutcomes.$.outcomeVoters", cohort);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Intuition addComment(Comment comment, String intuitionId) {
        comment.setId(newObjectId());
        Criteria criteria = Criteria.where("id").is(intuitionId);
        Query query = new Query(criteria);
        Update update = new Update().push("comments", comment);
        return mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
    }

    public Intuition removeComment(Comment comment) {
        Query query = Query.query(Criteria.where("comments._id").is(new ObjectId(comment.getId())));
        return mongoTemplate.findAndModify(query, new Update().pull("comments", comment), findAndModifyOptions, Intuition.class);
    }

    public Intuition addCommentLike(Like like, String intuitionId, String commentId) {
        like.setId(newObjectId());
        Criteria criteria = Criteria
                .where("id").is(intuitionId)
                .and("comments").elemMatch(Criteria.where("_id").is(commentId));
        Query query = new Query(criteria);
        Update update = new Update().push("comments.$.likes", like);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Intuition removeCommentLike(Like like, String intuitionId, String commentId) {
        Criteria criteria = Criteria
                .where("id").is(intuitionId)
                .and("comments").elemMatch(Criteria.where("_id").is(commentId));
        Query query = new Query(criteria);
        Update update = new Update().pull("comments.$.likes", like);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Intuition setOutcome(Outcome outcome, String intuitionId) {
//        outcome.setId(newObjectId());
        Criteria criteria = Criteria.where("id").is(intuitionId);
        Query query = new Query(criteria);
        Update update = new Update().set("outcome", outcome);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Intuition addOutcomeLike(Like like, String intuitionId) {
        like.setId(newObjectId());
        Criteria criteria = Criteria.where("id").is(intuitionId);
        Query query = new Query(criteria);
        Update update = new Update().push("outcome.likes", like);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Intuition removeOutcomeLike(Like like, String intuitionId) {
        Criteria criteria = Criteria.where("id").is(intuitionId);
        Query query = new Query(criteria);
        Update update = new Update().pull("outcome.likes", like);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Intuition addOutcomeComment(Comment comment, String intuitionId) {
        comment.setId(newObjectId());
        Criteria criteria = Criteria.where("id").is(intuitionId);
        Query query = new Query(criteria);
        Update update = new Update().push("outcome.comments", comment);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Intuition removeOutcomeComment(Comment comment, String intuitionId) {
        Criteria criteria = Criteria.where("id").is(intuitionId);
        Query query = new Query(criteria);
        Update update = new Update().pull("outcome.comments", comment);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Intuition addOutcomeCommentLike(Like like, String intuitionId, String commentId) {
        like.setId(newObjectId());
        Criteria criteria = Criteria
                .where("id").is(intuitionId)
                .and("outcome.comments").elemMatch(Criteria.where("_id").is(commentId));
        Query query = new Query(criteria);
        Update update = new Update().push("outcome.comments.$.likes", like);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Intuition removeOutcomeCommentLike(Like like, String intuitionId, String commentId) {
        Criteria criteria = Criteria
                .where("id").is(intuitionId)
                .and("outcome.comments").elemMatch(Criteria.where("_id").is(commentId));
        Query query = new Query(criteria);
        Update update = new Update().pull("outcome.comments.$.likes", like);
        Intuition intuition = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Intuition.class);
        return intuition;
    }

    public Score incrementScoreOwnedCorrect(String userId, Intuition simpleIntuition) {
        return incrementScore(userId, "ownedCorrect", simpleIntuition);
    }

    public Score incrementScoreOwnedIncorrect(String userId, Intuition simpleIntuition) {
        return incrementScore(userId, "ownedIncorrect", simpleIntuition);
    }

    public Score incrementScoreCohortCorrect(String userId, Intuition simpleIntuition) {
        return incrementScore(userId, "cohortCorrect", simpleIntuition);
    }

    public Score incrementScoreCohortIncorrect(String userId, Intuition simpleIntuition) {
        return incrementScore(userId, "cohortIncorrect", simpleIntuition);
    }

    private Score incrementScore(String userId, String columnName, Intuition simpleIntuition) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = new Query(criteria);
        Update update = new Update().push(columnName, simpleIntuition);
        Score score = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Score.class);
        return score;
    }

    public List<Intuition> findExpiredIntuitions(long nowMillis) {
        Criteria criteria = new Criteria();
        criteria.andOperator(
            Criteria.where("outcome").exists(false),
            Criteria.where("expirationMillis").lt(nowMillis)
        );
        Query query = new Query(criteria);
        List<Intuition> intuitions = mongoTemplate.find(query, Intuition.class);
        return intuitions;
    }


    public List<Intuition> fetchTopIntuition(User user) {
        String userId = user.getId();
        Criteria criteria = new Criteria();
        criteria.orOperator(
                Criteria.where("user._id").is(new ObjectId(userId))
                        .andOperator(Criteria.where("visibility").in(VISIBILITY_PUBLIC, VISIBILITY_COHORT))
        );

        Query query = new Query(criteria);
        query.limit(1);
        query.with(new Sort(Sort.Direction.DESC, "insertTimestamp"));
        List<Intuition> intuitions = mongoTemplate.find(query, Intuition.class);
        if (logger.isFiner()) {
            logger.finer("fetchTopIntuition: Found %d intuitions for username %s.", intuitions.size(), user.getUsername());
        }
        return intuitions;
    }


    public List<Intuition> fetchIntuitions(User user, List<String> cohortIds, int start, int quantity, Optional<String> lastUpdateTimestamp) {
        String userId = user.getId();
        Criteria criteria = new Criteria();
        List<ObjectId> objectIds = new ArrayList<>();
        for (String cohortId : cohortIds) {
            objectIds.add(new ObjectId(cohortId));
        }
        criteria.orOperator(
                Criteria.where("user._id").is(new ObjectId(userId)),
                Criteria.where("user._id").in(objectIds)
                        .andOperator(Criteria.where("visibility").in(VISIBILITY_PUBLIC, VISIBILITY_COHORT))
        );

        Query query = new Query(criteria);

        if (lastUpdateTimestamp.isPresent()) {
            String lastUpdateTimestampString = lastUpdateTimestamp.get();
            query.addCriteria(Criteria.where("insertTimestamp").gte(lastUpdateTimestampString));
        }

        query.skip(start).limit(quantity);
        query.with(new Sort(Sort.Direction.DESC, "insertTimestamp"));
        List<Intuition> intuitions = mongoTemplate.find(query, Intuition.class);
        if (logger.isFiner()) {
            logger.finer("fetchIntuitions: Found %d intuitions for username %s.", intuitions.size(), user.getUsername());
        }
        return intuitions;
    }

    public List<Intuition> fetchAllIntuitionsFromCommunity(User user, int start, int quantity, Optional<String> lastUpdateTimestamp) {
        String userId = user.getId();
        Criteria criteria = new Criteria();
        List<ObjectId> objectIds = new ArrayList<>();

        // RESTRICTED ID
        objectIds.add(new ObjectId("563e5e2ce4b06a671d1cb959")); // jenstevens
        objectIds.add(new ObjectId("5692f91ce4b0b37165b200ee")); // jen

        criteria.orOperator(
            Criteria.where("user._id").is(new ObjectId(userId)),
            Criteria.where("user._id").not().in(objectIds)
                    .andOperator(Criteria.where("visibility").in(VISIBILITY_PUBLIC, VISIBILITY_COHORT))
        );

        Query query = new Query(criteria);

        if (lastUpdateTimestamp.isPresent()) {
            String lastUpdateTimestampString = lastUpdateTimestamp.get();
            query.addCriteria(Criteria.where("insertTimestamp").gte(lastUpdateTimestampString));
        }

        query.skip(start).limit(quantity);
        query.with(new Sort(Sort.Direction.DESC, "insertTimestamp"));
        List<Intuition> intuitions = mongoTemplate.find(query, Intuition.class);
        if (logger.isFiner()) {
            logger.finer("fetchAllIntuitionsFromCommunity: Found %d intuitions for username %s.", intuitions.size(), user.getUsername());
        }
        return intuitions;
    }

    public List<Intuition> fetchAllVisibilityIntuitionsForUser(User user, int start, int quantity) {
        String userId = user.getId();
        Criteria criteria = Criteria.where("user._id").is(new ObjectId(userId));
        Query query = new Query(criteria);
        query.skip(start).limit(quantity);
        query.with(new Sort(Sort.Direction.DESC, "insertTimestamp"));
        List<Intuition> intuitions = mongoTemplate.find(query, Intuition.class);
        if (logger.isFiner()) {
            logger.finer("fetchAllVisibilityIntuitionsForUser: Found %d intuitions for username %s.", intuitions.size(), user.getUsername());
        }
        return intuitions;
    }

    public List<Intuition> fetchCohortAndPublicVisibleIntuitionsForUser(String username, int start, int quantity) {
        Criteria criteria = new Criteria();
        criteria.andOperator(
            Criteria.where("user.username").is(username),
            Criteria.where("visibility").in(VISIBILITY_PUBLIC, VISIBILITY_COHORT)
        );
        Query query = new Query(criteria);
        query.skip(start).limit(quantity);
        query.with(new Sort(Sort.Direction.DESC, "insertTimestamp"));
        List<Intuition> intuitions = mongoTemplate.find(query, Intuition.class);
        if (logger.isFiner()) {
            logger.finer("fetchCohortAndPublicVisibleIntuitionsForUser: Found %d intuitions for username %s.", intuitions.size(), username);
        }
        return intuitions;
    }

    public List<Intuition> fetchPublicVisibleIntuitionsForUser(String username, int start, int quantity) {
        Criteria criteria = new Criteria();
        criteria.andOperator(
                Criteria.where("user.username").is(username),
                Criteria.where("visibility").is(VISIBILITY_PUBLIC)
        );
        Query query = new Query(criteria);
        query.skip(start).limit(quantity);
        query.with(new Sort(Sort.Direction.DESC, "insertTimestamp"));
        List<Intuition> intuitions = mongoTemplate.find(query, Intuition.class);
        if (logger.isFiner()) {
            logger.finer("fetchPublicVisibleIntuitionsForUser: Found %d intuitions for username %s.", intuitions.size(), username);
        }
        return intuitions;
    }

    public List<Intuition> fetchIntuitionsSinceLastUpdate(String username, String lastUpdateTimestamp) {
        Criteria criteria = new Criteria();
        criteria.andOperator(
                Criteria.where("user.username").is(username)
        );
        Query query = new Query(criteria);
        query.addCriteria(Criteria.where("insertTimestamp").gte(lastUpdateTimestamp));
        List<Intuition> intuitions = mongoTemplate.find(query, Intuition.class);
        if (logger.isFiner()) {
            logger.finer("fetchIntuitionsSinceLastUpdate: Found %d intuitions for username %s.", intuitions.size(), username);
        }
        return intuitions;
    }

    public List<Notification> findUnhandledNotifications(User user) {
        String userId = user.getId();
        Criteria criteria = Criteria.where("userId").is(userId);
        criteria = criteria.and("isHandled").is(false);
        Query query = new Query(criteria);
        List<Notification> notifications = mongoTemplate.find(query, Notification.class);
        return notifications;
    }

    private String newObjectId() {
        return ObjectId.get().toString();
    }
}