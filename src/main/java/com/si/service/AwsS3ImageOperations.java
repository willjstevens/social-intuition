/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.service;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.si.Category;
import com.si.entity.Intuition;
import com.si.framework.FileInfo;
import com.si.log.LogManager;
import com.si.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.si.Constants.FORWARD_SLASH;

/**
 * @author wstevens
 */
@Component
public class AwsS3ImageOperations implements ImageOperations
{
    private static final Logger logger = LogManager.manager().newLogger(AwsS3ImageOperations.class, Category.SERVICE_APPLICATION);
    private static final String BUCKET_PREFIX = "si-";
    private static final String USERS_FOLDER = "u";
    private static final String INTUITIONS_FOLDER = "i";
    private static final String OUTCOME_FOLDER = "o";
    @Autowired private ConfigurationService configurationService;
    private AmazonS3 s3client;
    private String bucket;
    private boolean isInitialized;

    public void init() {
        s3client = new AmazonS3Client(new ProfileCredentialsProvider());

        // build bucket name
        String environment = configurationService.getEnvironmentString();
        bucket = BUCKET_PREFIX + environment.charAt(0);
        isInitialized = true;
    }

    @Override
    public String storeProfileImage(String username, FileInfo fileInfo) {
        String returnUrl = null;
        StringBuilder builder = buildUserDirectory(username);
        builder.append("profile");
        String key = builder.toString();
        returnUrl = storeImageInS3(key, fileInfo);
        return returnUrl;
    }

    @Override
    public List<String> storeIntuitionImages(String username, String intuitionId, List<FileInfo> fileInfos) {
        List<String> returnUrls = new ArrayList<>();
        StringBuilder builder = buildIntuitionKeyPrefix(username, intuitionId);
        String prefix = builder.toString();
        for (FileInfo fileInfo : fileInfos) {
            String key = prefix + fileInfo.name;
            String returnUrl = storeImageInS3(key, fileInfo);
            returnUrls.add(returnUrl);
        }
        return returnUrls;
    }

    @Override
    public List<String> storeIntuitionOutcomeImages(String username, String intuitionId, List<FileInfo> fileInfos) {
        List<String> returnUrls = new ArrayList<>();
        StringBuilder builder = buildIntuitionOutcomeKeyPrefix(username, intuitionId);
        String prefix = builder.toString();
        for (FileInfo fileInfo : fileInfos) {
            String key = prefix + fileInfo.name;
            String returnUrl = storeImageInS3(key, fileInfo);
            returnUrls.add(returnUrl);
        }
        return returnUrls;
    }

    @Override
    public void removeIntuitionImages(String username, Intuition intuition) {
        String intuitionId = intuition.getId();
        StringBuilder builder = buildIntuitionKeyPrefix(username, intuitionId);
        String keyPrefix = builder.toString();
        // referencing element 0 for now, due to issue below and currently not supporting
        //      more than one intuition image
        String key = keyPrefix + intuition.getImageInfos().get(0).getFileName();
        s3client.deleteObject(bucket, key);

        // BELOW (multiple object deletes) IS COMMENTED DUE TO ISSUE https://github.com/aws/aws-sdk-java/issues/333
//        DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucket);
//        List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<DeleteObjectsRequest.KeyVersion>();
//        multiObjectDeleteRequest.setKeys(keys);
//        intuition.getImageInfos().stream().forEach(imageInfo -> {
//            String key = keyPrefix + imageInfo.getFileName();
//            keys.add(new DeleteObjectsRequest.KeyVersion(key));
//        });
//        try {
//            DeleteObjectsResult delObjRes = s3client.deleteObjects(multiObjectDeleteRequest);
//            if (logger.isFine()) {
//                logger.fine("Deleted %d images from intuition with ID %s.", delObjRes.getDeletedObjects().size(), intuitionId);
//            }
//        } catch (MultiObjectDeleteException e) {
//            logger.error("Error while deleting intuition images.", e);
//        }
    }

    private StringBuilder buildUserDirectory(String username) {
        StringBuilder builder = new StringBuilder();
        builder.append(USERS_FOLDER).append(FORWARD_SLASH);
        builder.append(username).append(FORWARD_SLASH);
        return builder;
    }

    private StringBuilder buildIntuitionKeyPrefix(String username, String intuitionId) {
        StringBuilder builder = buildUserDirectory(username);
        builder.append(INTUITIONS_FOLDER).append(FORWARD_SLASH);
        builder.append(intuitionId).append(FORWARD_SLASH);
        return builder;
    }

    private StringBuilder buildIntuitionOutcomeKeyPrefix(String username, String intuitionId) {
        StringBuilder builder = buildUserDirectory(username);
        builder.append(INTUITIONS_FOLDER).append(FORWARD_SLASH);
        builder.append(intuitionId).append(FORWARD_SLASH);
        builder.append(OUTCOME_FOLDER).append(FORWARD_SLASH);
        return builder;
    }

    private String storeImageInS3(String key, FileInfo fileInfo) {
        String returnUrl = null;
        // put object first
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(fileInfo.contentType);
        PutObjectRequest request = new PutObjectRequest(bucket, key, fileInfo.inputStream, objectMetadata);
        request.setCannedAcl(CannedAccessControlList.PublicRead); // needed for public access
        try {
            logger.finer("About to put AWS object with key \"%s\".", key);
            s3client.putObject(request);
            logger.finer("Successfully put AWS object with key \"%s\".", key);
        } catch (Exception e) {
            logger.error("Failed to put AWS object with key \"%s\".", e, key);
        }
        returnUrl = generatePresignedUrl(bucket, key);
        return returnUrl;
    }

    @Override
    public String copyDefaultProfileImageToUser(String username) {
        String returnUrl = null;
        StringBuilder builder = buildUserDirectory(username);
        builder.append("profile");
        String key = builder.toString();
        CopyObjectRequest request = new CopyObjectRequest("si-static", "user-default-image", bucket, key);
        request.setCannedAccessControlList(CannedAccessControlList.PublicRead); // needed for public access
        try {
            logger.finer("About to copy default profile image AWS object for username \"%s\".", username);
            s3client.copyObject(request);
            logger.finer("Successfully copied AWS profile image for username \"%s\".", username);
        } catch (Exception e) {
            logger.error("Failed to copy AWS profile image for username \"%s\".", e, username);
        }
        returnUrl = generatePresignedUrl(bucket, key);
        return returnUrl;
    }

    private String generatePresignedUrl(String bucket, String key) {
        String returnUrl = null;
        // now generate non-expiring url
        URL url = s3client.generatePresignedUrl(bucket, key, null);
        returnUrl = url.toExternalForm();
        // chop off query string portion with access key and sensitive info
        returnUrl = returnUrl.substring(0, returnUrl.indexOf('?'));
        return returnUrl;
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }
}
