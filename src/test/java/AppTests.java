/*
 * Property of Will Stevens
 * All rights reserved.
 */

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfig.class })
public class AppTests
{

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testS3() {

        String bucket = "si-d";
        AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());

// Send sample request (list objects in a given bucket).
        ObjectListing objectListing = s3client.listObjects(new
                ListObjectsRequest().withBucketName(bucket));

        System.out.println(objectListing.getObjectSummaries().size());

        String key = "loves/love.jpg";
        File file = new File("/Users/willstevens/tmp/love.jpg");
        PutObjectRequest request = new PutObjectRequest(bucket, key, file);
        request.setCannedAcl(CannedAccessControlList.PublicRead);
        PutObjectResult result = s3client.putObject(request);
        System.out.println(result.getExpirationTimeRuleId());

        S3Object object = s3client.getObject(bucket, key);
        System.out.println(object.getRedirectLocation());


        java.util.Date expiration = new java.util.Date();
        long milliSeconds = expiration.getTime();
        milliSeconds += 1000 * 15; // Add 15 sec.
        expiration.setTime(milliSeconds);
        URL url = s3client.generatePresignedUrl(bucket, key, null);
        System.out.println("******: " + url.toExternalForm());

    }


}
