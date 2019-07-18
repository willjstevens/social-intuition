/*
 * Property of Will Stevens
 * All rights reserved.
 */

import com.si.Util;
import com.si.dao.ApplicationDao;
import com.si.dao.DaoConfig;
import com.si.entity.*;
import com.si.service.UtilityService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfig.class, DaoConfig.class })
public class MongoDbTests
{
    @Autowired private ApplicationDao applicationDao;
    @Autowired private UtilityService utilityService;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        applicationDao.removeAll(User.class);
        applicationDao.removeAll(Cohort.class);
        applicationDao.removeAll(Intuition.class);
        applicationDao.removeAll(Config.class);
        applicationDao.removeAll(DeviceSession.class);
        applicationDao.removeAll(Notification.class);

        System.out.println("Done removing all users, cohorts, intuitions, configs, notifications, device sessions.");
    }

    @Test
    public void testUser() {
        User u1 = newUserWill();
        applicationDao.save(u1);
        Assert.assertNotNull(u1.getId());
        u1 = applicationDao.findUserById(u1.getId());
        Assert.assertNotNull(u1);
        u1 = applicationDao.findUserByUsername(u1.getUsername());
        Assert.assertNotNull(u1);
        u1 = applicationDao.findUserByEmail(u1.getEmail());
        Assert.assertNotNull(u1);
        u1 = applicationDao.findUserByCookieValue(u1.getCookieValue());
        Assert.assertNotNull(u1);

        User u2 = newUserWill();
        u2.setFullName("U2 The Band");
        u2.setFirstName("Bono");
        u2.setLastName("Stevens");
        u2.setUsername("willj");
        applicationDao.save(u2);
        List<User> users = applicationDao.findUserByUsernameLikeString("will");
        Assert.assertEquals(2, users.size());
        users = applicationDao.findUserByUsernameLikeString("willjs");
        Assert.assertEquals(1, users.size());


        User u3 = newUserJimi();
        applicationDao.save(u3);
        users = applicationDao.findUsersByFirstAndLastNames("Bono", "Stevens");
        Assert.assertEquals(2, users.size()); // only will and Bono


        applicationDao.remove(u1);
        applicationDao.remove(u2);
        applicationDao.remove(u3);
    }

    @Ignore
    @Test
    public void testDeviceSession() {
        User u1 = newUserWill();
        applicationDao.save(u1);

        DeviceSession ds1 = newDeviceSession(u1);
        applicationDao.save(ds1);
//        Assert.assertNull(ds1.getUpdateTimestamp());
        ds1 = applicationDao.findAndUpdateDeviceSession(ds1);
//        Assert.assertNotNull(ds1.getUpdateTimestamp());


        applicationDao.remove(u1);
        applicationDao.remove(ds1);
    }

    @Test
    public void testNotifications() {
        User u1 = newUserWill();
        applicationDao.save(u1);

        Notification notification1 = newNotification();
        notification1.setUserId(u1.getId());
        applicationDao.save(notification1);
        Notification notification2 = newNotification();
        notification2.setUserId(u1.getId());
        applicationDao.save(notification2);
        Notification notification3 = newNotification();
        notification3.setUserId(u1.getId());
        notification3.setHandled(true);
        applicationDao.save(notification3);

        List<Notification> notifications = applicationDao.findUnhandledNotifications(u1);
        Assert.assertEquals(2, notifications.size());

        applicationDao.remove(u1);
        applicationDao.remove(notification1);
        applicationDao.remove(notification2);
        applicationDao.remove(notification3);
    }

    @Test
    public void testCohorts() {
        User inviter = newUserWill();
        User consenter = newUserStevie();
        applicationDao.save(inviter);
        applicationDao.save(consenter);

        Cohort cohort = newCohort(inviter, consenter);
        applicationDao.save(cohort);
        Assert.assertFalse(cohort.isAccepted());

        List<Cohort> cohorts = applicationDao.findCohortInvites(inviter);
        Assert.assertEquals(1, cohorts.size());

        cohort = applicationDao.setCohortToAccepted(cohort);
        Assert.assertTrue(cohort.isAccepted());

        cohorts = applicationDao.findCohorts(inviter);
        Assert.assertEquals(1, cohorts.size());

        cohort = applicationDao.unCohort(cohort);
        cohorts = applicationDao.findCohorts(inviter);
        Assert.assertEquals(0, cohorts.size());

        applicationDao.remove(inviter);
        applicationDao.remove(consenter);
        applicationDao.remove(cohort);
    }

    @Test
    public void testIntuition() {
        User will = newUserWill();
        User stevie = newUserStevie();
        applicationDao.save(will);
        applicationDao.save(stevie);

        Intuition intuition = newIntuition(will);
        applicationDao.addIntuition(intuition);

        // test adding and removing a like
        Like like = newLike(stevie);
        intuition = applicationDao.addLike(like, intuition.getId());
        Assert.assertEquals(1, intuition.getLikes().size());
        intuition = applicationDao.removeLike(like);
        intuition = applicationDao.findById(intuition.getId(), Intuition.class);
        Assert.assertEquals(0, intuition.getLikes().size());
        // test adding and removing a comment
        Comment comment = newComment(will);
        comment.setCommentText("This is a comment from Will.");
        intuition = applicationDao.addComment(comment, intuition.getId());
        Assert.assertEquals(1, intuition.getComments().size());
        intuition = applicationDao.removeComment(comment);
        Assert.assertTrue(intuition.getComments().isEmpty());
        // test adding an removing a comment like
        intuition = applicationDao.addComment(comment, intuition.getId());
        Assert.assertEquals(1, intuition.getComments().size());
        intuition = applicationDao.addCommentLike(like, intuition.getId(), comment.getId());
        Assert.assertEquals(1, intuition.getComments().get(0).getLikes().size());
        intuition = applicationDao.removeCommentLike(like, intuition.getId(), comment.getId());
        Assert.assertTrue(intuition.getComments().get(0).getLikes().isEmpty());

        // prediction choices
        Outcome willsPredictionChoice = newPredictionChoice(will);
        Outcome steviesPredictionChoice = newPredictionChoice(stevie);
        intuition = applicationDao.addPotentialOutcome(willsPredictionChoice, intuition.getId());
        intuition = applicationDao.addPotentialOutcome(steviesPredictionChoice, intuition.getId());
        Assert.assertEquals(2, intuition.getPotentialOutcomes().size());
        intuition = applicationDao.removePotentialOutcome(willsPredictionChoice);
        intuition = applicationDao.removePotentialOutcome(steviesPredictionChoice);
        Assert.assertTrue(intuition.getPotentialOutcomes().isEmpty());

        // cohort predictions
        Outcome willsPrediction = newPredictionChoice(will);
        Outcome steviesPrediction = newPredictionChoice(stevie);
        intuition = applicationDao.addPotentialOutcome(willsPrediction, intuition.getId());
        intuition = applicationDao.addPotentialOutcome(steviesPrediction, intuition.getId());
        Assert.assertEquals(2, intuition.getPotentialOutcomes().size());
        intuition = applicationDao.removePotentialOutcome(willsPrediction);
        intuition = applicationDao.removePotentialOutcome(steviesPrediction);
        Assert.assertTrue(intuition.getPotentialOutcomes().isEmpty());

        // Outcomes
        intuition = applicationDao.setOutcome(willsPredictionChoice, intuition.getId());
        // do adds
        Like outcomeLike = newLike(will);
        intuition = applicationDao.addOutcomeLike(outcomeLike, intuition.getId());
        Assert.assertEquals(1, intuition.getOutcome().getLikes().size());
        Comment outcomeComment = newComment(stevie);
        outcomeComment.setCommentText("This outcome is unbelievable!");
        intuition = applicationDao.addOutcomeComment(outcomeComment, intuition.getId());
        Assert.assertEquals(1, intuition.getOutcome().getComments().size());
        Like outcomeCommentLike = newLike(will);
        intuition = applicationDao.addOutcomeCommentLike(outcomeCommentLike, intuition.getId(), outcomeComment.getId());
        Assert.assertEquals(1, intuition.getOutcome().getComments().get(0).getLikes().size());
        // do removals
        intuition = applicationDao.removeOutcomeCommentLike(outcomeCommentLike, intuition.getId(), outcomeComment.getId());
        Assert.assertTrue(intuition.getOutcome().getComments().get(0).getLikes().isEmpty());
        intuition = applicationDao.removeOutcomeComment(outcomeComment, intuition.getId());
        Assert.assertTrue(intuition.getOutcome().getComments().isEmpty());
        intuition = applicationDao.removeOutcomeLike(outcomeLike, intuition.getId());
        Assert.assertTrue(intuition.getOutcome().getLikes().isEmpty());

        // change intuition all together and save
        intuition.setIntuitionText("Something else.");
        intuition.setPredictionType("multiple-choice");
        intuition.getOutcome().setPredictionText("False");
        intuition.setAllowPredictedOutcomeVoting(false);
        intuition.getPotentialOutcomes().add(willsPrediction);
        intuition.getPotentialOutcomes().add(steviesPrediction);
        intuition.getPotentialOutcomes().add(willsPredictionChoice);
        intuition.getPotentialOutcomes().add(steviesPredictionChoice);
        applicationDao.save(intuition);
        intuition = applicationDao.findById(intuition.getId(), Intuition.class);
        Assert.assertTrue(intuition.getIntuitionText().equals("Something else."));
        Assert.assertTrue(intuition.getPredictionType().equals("multiple-choice"));
        Assert.assertEquals(4, intuition.getPotentialOutcomes().size());

        applicationDao.remove(intuition);
        applicationDao.remove(will);
        applicationDao.remove(stevie);
    }

    @Test
    public void testIntuitionFetching() {
        User will = newUserWill();
        User stevie = newUserStevie();
        User jimi = newUserJimi();
        applicationDao.save(will);
        applicationDao.save(stevie);
        applicationDao.save(jimi);

        Cohort steveCohort = newCohort(will, stevie);
        applicationDao.save(steveCohort);
         
        // wills intuition
        Intuition willsWorkIntuition = newIntuition(will);
        willsWorkIntuition.getUser().setUsername("will");
        willsWorkIntuition.setIntuitionText("I will get a new job by the end of the year.");
        willsWorkIntuition.setVisibility("private");
        applicationDao.addIntuition(willsWorkIntuition);
        Intuition willsSportsIntuition = newIntuition(will);
        willsSportsIntuition.getUser().setUsername("will");
        willsSportsIntuition.setIntuitionText("The Bears will win the Super Bowl this year.");
        willsSportsIntuition.setVisibility("public");
        applicationDao.addIntuition(willsSportsIntuition);
        Intuition willsPoliticsIntuition = newIntuition(will);
        willsPoliticsIntuition.getUser().setUsername("will");
        willsPoliticsIntuition.setIntuitionText("The Republicans will take office next term.");
        willsPoliticsIntuition.setVisibility("cohort");
        applicationDao.addIntuition(willsPoliticsIntuition);

        // steve           
        Intuition stevesWorkIntuition = newIntuition(stevie);
        stevesWorkIntuition.getUser().setUsername("stevie");
        stevesWorkIntuition.setIntuitionText("I will be dating YYY by the end of the year.");
        stevesWorkIntuition.setVisibility("private");
        applicationDao.addIntuition(stevesWorkIntuition);
        Intuition stevesSportsIntuition = newIntuition(stevie);
        stevesSportsIntuition.setIntuitionText("My concerts will sell out.");
        stevesSportsIntuition.setVisibility("public");
        applicationDao.addIntuition(stevesSportsIntuition);
        Intuition stevesPoliticsIntuition = newIntuition(stevie);
        stevesPoliticsIntuition.setIntuitionText("Check out my new Strat guitar.");
        stevesPoliticsIntuition.setVisibility("cohort");
        applicationDao.addIntuition(stevesPoliticsIntuition);

        // jimi
        Intuition jimisWorkIntuition = newIntuition(jimi);
        jimisWorkIntuition.getUser().setUsername("jimi");
        jimisWorkIntuition.setIntuitionText("Jimis PRIVATE intuition.");
        jimisWorkIntuition.setVisibility("private");
        applicationDao.addIntuition(jimisWorkIntuition);
        Intuition jimisSportsIntuition = newIntuition(jimi);
        jimisSportsIntuition.setIntuitionText("Jimis PUBLIC intuition.");
        jimisSportsIntuition.setVisibility("public");
        applicationDao.addIntuition(jimisSportsIntuition);
        Intuition jimisPoliticsIntuition = newIntuition(jimi);
        jimisPoliticsIntuition.setIntuitionText("Jimis COHORT intuition.");
        jimisPoliticsIntuition.setVisibility("cohort");
        applicationDao.addIntuition(jimisPoliticsIntuition);

        // tests
        List<String> cohortIds = Arrays.asList(stevie.getId());
        List<Intuition> willsIntuitions = applicationDao.fetchIntuitions(will, cohortIds, 0, 10, Optional.ofNullable(null));
        Assert.assertEquals(5, willsIntuitions.size());
        // test visibility fetching
        List<Intuition> allWillsIntuitions = applicationDao.fetchAllVisibilityIntuitionsForUser(will, 0, 10);
        Assert.assertEquals(3, allWillsIntuitions.size());
        List<Intuition> publicAndCohortVisibleIntuitions = applicationDao.fetchCohortAndPublicVisibleIntuitionsForUser("will", 0, 10);
        Assert.assertEquals(2, publicAndCohortVisibleIntuitions.size());
        List<Intuition> publicVisibleIntuitions = applicationDao.fetchPublicVisibleIntuitionsForUser("will", 0, 10);
        Assert.assertEquals(1, publicVisibleIntuitions.size());

        applicationDao.remove(willsWorkIntuition);
        applicationDao.remove(willsSportsIntuition);
        applicationDao.remove(willsPoliticsIntuition);
        applicationDao.remove(stevesWorkIntuition);
        applicationDao.remove(stevesSportsIntuition);
        applicationDao.remove(stevesPoliticsIntuition);
        applicationDao.remove(jimisWorkIntuition);
        applicationDao.remove(jimisSportsIntuition);
        applicationDao.remove(jimisPoliticsIntuition);
        
        applicationDao.remove(will);
        applicationDao.remove(stevie);
        applicationDao.remove(jimi);
    }


    @Test
    public void testIntuitionFetchingSinceLastUpdate() {
        User will = newUserWill();
        applicationDao.save(will);

        // wills intuition
        Intuition willsWorkIntuition = newIntuition(will);
        willsWorkIntuition.getUser().setUsername("will");
        willsWorkIntuition.setIntuitionText("I will get a new job by the end of the year.");
        willsWorkIntuition.setVisibility("private");
        OffsetDateTime fourDaysAgo = utilityService.nowUtc().minusDays(4);
        willsWorkIntuition.setInsertTimestamp(utilityService.toPersistentString(fourDaysAgo));
        applicationDao.addIntuition(willsWorkIntuition);
        Intuition willsSportsIntuition = newIntuition(will);
        willsSportsIntuition.getUser().setUsername("will");
        willsSportsIntuition.setIntuitionText("The Bears will win the Super Bowl this year.");
        willsSportsIntuition.setVisibility("public");
        OffsetDateTime twoHoursAgo = utilityService.nowUtc().minusHours(2);
        willsSportsIntuition.setInsertTimestamp(utilityService.toPersistentString(twoHoursAgo));
        applicationDao.addIntuition(willsSportsIntuition);


        // tests
        List<Intuition> allWillsIntuitions = applicationDao.fetchAllVisibilityIntuitionsForUser(will, 0, 10);
        Assert.assertEquals(2, allWillsIntuitions.size());

        // create future intuition
        Intuition willsPoliticsIntuition = newIntuition(will);
        willsPoliticsIntuition.getUser().setUsername("will");
        willsPoliticsIntuition.setIntuitionText("The Republicans will take office next term.");
        willsPoliticsIntuition.setVisibility("public");
        OffsetDateTime oneMonthAhead = utilityService.nowUtc().plusMonths(1);
        willsPoliticsIntuition.setInsertTimestamp(utilityService.toPersistentString(oneMonthAhead));
        applicationDao.addIntuition(willsPoliticsIntuition);

        // now test getting new intuitions from back one hour, when last updated
        OffsetDateTime oneHourAgo = utilityService.nowUtc().minusHours(1);
        String oneHourAgoPersistentString = utilityService.toPersistentString(oneHourAgo);
        List<Intuition> recentIntuitions = applicationDao.fetchIntuitionsSinceLastUpdate("will", oneHourAgoPersistentString);
        Assert.assertEquals(1, recentIntuitions.size());


        applicationDao.remove(willsWorkIntuition);
        applicationDao.remove(willsSportsIntuition);
        applicationDao.remove(willsPoliticsIntuition);

        applicationDao.remove(will);
    }


    Like newLike(User user) {
        Like like = new Like();
        like.setUser(user);
        return like;
    }

    Comment newComment(User user) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setCommentText("This is comment text.");
        comment.setInsertTimestamp(Util.now().toString());
        return comment;
    }

    Outcome newOutcome() {
        Outcome outcome = new Outcome();
        outcome.setPredictionText("True");
        return outcome;
    }

    Intuition newIntuition(User user) {
        Intuition intuition = new Intuition();
        intuition.setUser(user);
//        intuition.getUser().setId(user.getId());
        intuition.setIntuitionText("I believe the product will rock!");
//        intuition.getUser().setFullName(user.getFullName());
        intuition.setVisibility("public");
        intuition.setDisplayPrediction(true);
        intuition.setAllowCohortsToContributePredictedOutcomes(true);
        intuition.setPredictionType("true-false");

        String nowUtcString = utilityService.nowUtcPersistentString();
        intuition.setInsertTimestamp(nowUtcString);

        Outcome prediction = new Outcome();
        prediction.setPredictionText("True");
        intuition.setPredictedOutcome(prediction);

        return intuition;
    }

    Outcome newPredictionChoice(User user) {
        Outcome predictionChoice = new Outcome();
        predictionChoice.setPredictionText("True");
        predictionChoice.setContributorUser(user);
        return predictionChoice;
    }

    User newUserWill() {
        User user = new User();
        user.setFirstName("Will");
        user.setLastName("Stevens");
        user.setFullName("Will Stevens");
        user.setUsername("willjstevens");
        user.setEmail("willjstevens@gmail.com");
        user.setCookieValue("1234567890");
        user.setInsertTimestamp(Util.now().toString());
        return user;
    }

    User newUserStevie() {
        User user = new User();
        user.setFirstName("Stevie");
        user.setLastName("Vaughn");
        user.setFullName("Stevie Ray Vaughn");
        user.setUsername("svaughn");
        user.setEmail("srvaughn@gmail.com");
        user.setCookieValue("asfdasdfoiy");
        user.setInsertTimestamp(Util.now().toString());
        return user;
    }

    User newUserJimi() {
        User user = new User();
        user.setFirstName("Jimi");
        user.setLastName("Hendrix");
        user.setFullName("Jimi Hendrix");
        user.setUsername("jhendrix");
        user.setEmail("jhendrix@gmail.com");
        user.setCookieValue("qwreqwerqweroiuwer");
        user.setInsertTimestamp(Util.now().toString());
        return user;
    }

    Cohort newCohort(User inviter, User consenter) {
        Cohort cohort = new Cohort();
        cohort.setInviterUserId(inviter.getId());
        cohort.setInviterFullName(inviter.getFullName());
        cohort.setConsenterUserId(consenter.getId());
        cohort.setConsenterFullName(consenter.getFullName());
        cohort.setInsertTimestamp(Util.now().toString());
        return cohort;
    }

    DeviceSession newDeviceSession(User user) {
        DeviceSession deviceSession = new DeviceSession();
        deviceSession.setUserId(user.getId());
        deviceSession.setDeviceId(UUID.randomUUID().toString());
        deviceSession.setHttpSessionId(UUID.randomUUID().toString());
        deviceSession.setInsertTimestamp(Util.now());
        return deviceSession;
    }

    Notification newNotification() {
        Notification notification = new Notification();
        notification.setType("add-cohort");
        notification.setMessage("Hello notification!!!");
        notification.setInsertTimestamp(Util.now().toString());
        return notification;
    }
}
