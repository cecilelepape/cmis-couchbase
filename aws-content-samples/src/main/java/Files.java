import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

public class Files {
    private static AmazonS3 s3client;
    private static String bucketName = "couchbase-repo";

    private static void UploadFile(String localPath, String bucketName, String folderName, String fileName) {
        String keyName = folderName + "/" + fileName;
        System.out.println("Uploading a new object to S3 from a file\n");
        File file = new File(localPath);
        s3client.putObject(new PutObjectRequest(bucketName, keyName, file));
    }

    private static void DownloadFile(String localPath, String bucketName, String folderName, String fileName) {
        String keyName = folderName + "/" + fileName;
        System.out.println("Downloading an object from S3 to a file\n");

        File file = new File(localPath);
        s3client.getObject(new GetObjectRequest(bucketName, keyName), file);
    }

    private static List<String> GetFileList(String bucketName, String folderName) {
        if(folderName.length() > 0 && !folderName.endsWith("/"))
            folderName = folderName + "/";

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(folderName);
        ObjectListing objectListing;
        List<String> files= new ArrayList<>();
        String fileName;

        do {
            objectListing = s3client.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
//                System.out.println(" - " + objectSummary.getKey() + "  " +
//                        "(size = " + objectSummary.getSize() +
//                        ")");

                fileName = objectSummary.getKey().replace(folderName, "");
                if(fileName.trim().length() > 0)
                    files.add(fileName);
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());

        return files;
    }

    public static void main(String[] args) throws IOException {
        s3client = new AmazonS3Client(new ProfileCredentialsProvider("guest"));

        try {

            UploadFile("pom.xml", bucketName, "test", "test.xml");
            DownloadFile("downloaded.xml", bucketName, "test", "test.xml");

            List<String> files = GetFileList(bucketName, "");
            for(String file : files)
                System.out.println(file);

        } catch (AmazonServiceException ase) {
            System.out.println("Amazon Service Error:");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Amazon Client Error:");
            System.out.println("Error Message: " + ace.getMessage());
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}