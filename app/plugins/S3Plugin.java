package plugins;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.tika.Tika;
import play.Application;
import play.Logger;
import play.Plugin;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class S3Plugin extends Plugin {

    public static final String AWS_S3_BUCKET = "aws.s3.bucket";
    public static final String AWS_ACCESS_KEY = "aws.access.key";
    public static final String AWS_SECRET_KEY = "aws.secret.key";
    private final Application application;
    private static AmazonS3 amazonS3;
    private static String s3Bucket;

    public S3Plugin(Application application) {
        this.application = application;
    }

    @Override
    public void onStart() {
        String accessKey = application.configuration().getString(AWS_ACCESS_KEY);
        String secretKey = application.configuration().getString(AWS_SECRET_KEY);
        s3Bucket = application.configuration().getString(AWS_S3_BUCKET);

        if ((accessKey != null) && (secretKey != null)) {
            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            amazonS3 = new AmazonS3Client(awsCredentials);
            amazonS3.createBucket(s3Bucket);
        }
    }

    @Override
    public boolean enabled() {
        return (application.configuration().keys().contains(AWS_ACCESS_KEY) &&
                application.configuration().keys().contains(AWS_SECRET_KEY) &&
                application.configuration().keys().contains(AWS_S3_BUCKET));
    }

    public static String key2url(String key) {
        return "https://s3.amazonaws.com/" + s3Bucket + "/" + key;
    }

    public static String url2key(String url) {
        String prefix = key2url("");
        if (!prefix.equals(url.substring(0, prefix.length()))) return null;
        return url.substring(prefix.length());
    }

    public static PutObjectRequest putFile(String key, File file, Map<String, String> userMetadata) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(s3Bucket, key, file);
        ObjectMetadata meta = putObjectRequest.getMetadata();
        if (meta == null) {
            meta = new ObjectMetadata();
            putObjectRequest.setMetadata(meta);
        }
        meta.setUserMetadata(userMetadata);
        try {
            meta.setContentType(new Tika().detect(file));
        } catch (IOException e) {}
        putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
        amazonS3.putObject(putObjectRequest);

        return  putObjectRequest;
    }

    public static void removeFile(String key) {
        amazonS3.deleteObject(s3Bucket, key);
    }

}