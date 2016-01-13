package org.apache.chemistry.opencmis.couchbase;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PartialContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AWSStorageService implements StorageService {

	private static final Logger LOG = LoggerFactory
			.getLogger(AWSStorageService.class);

	private static final int BUFFER_SIZE = 64 * 1024;
	private static final String STORAGE_ID = "aws";

	private final String bucket;
	private final String folder;
	private AmazonS3 s3client;

	public AWSStorageService(String bucket, String folder) {
		this.bucket = bucket;
		this.folder = folder;
		s3client = new AmazonS3Client(new ProfileCredentialsProvider("guest"));
	}

	public void close() {
	}

	public void writeContent(String dataId, ContentStream contentStream)
			throws StorageException {
		debug("writeContent dataId=" + dataId);

		try {
			Long contentLength = contentStream.getLength();
			 ObjectMetadata metadata = new ObjectMetadata();
			    metadata.setContentLength(contentLength);
			    
			s3client.putObject(new PutObjectRequest(bucket, dataId,
					contentStream.getStream(), metadata));
			printFiles();
		} catch (AmazonServiceException ase) {
			System.out.println("Amazon Service Error:");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
			throw new StorageException("Content could not be added : "
					+ ase.getMessage());
		} catch (AmazonClientException ace) {
			System.out.println("Amazon Client Error:");
			System.out.println("Error Message: " + ace.getMessage());
			throw new StorageException("Content could not be added : "
					+ ace.getMessage());
		} catch (Exception ex) {
			System.out.println(ex.toString());
			throw new StorageException("Content could not be added : "
					+ ex.getMessage());
		}

	}

	public boolean deleteContent(String dataId) {
		debug("deleteContent dataId=" + dataId);

		try {
			System.out.println("before delete");
			printFiles();
			s3client.deleteObject(new DeleteObjectRequest(bucket, dataId));
			System.out.println("after delete");
			printFiles();
			return true;
		} catch (AmazonServiceException ase) {
			System.out.println("Amazon Service Error:");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
			return false;
		} catch (AmazonClientException ace) {
			System.out.println("Amazon Client Error:");
			System.out.println("Error Message: " + ace.getMessage());
			return false;
		} catch (Exception ex) {
			System.out.println(ex.toString());
			return false;
		}
	}

	public org.apache.chemistry.opencmis.commons.data.ContentStream getContent(
			String dataId, BigInteger offset, BigInteger length, String filename)
			throws StorageException {
		debug("getContent dataId="+dataId);
		try {
			File file = File.createTempFile(dataId, "tmp");
			s3client.getObject(new GetObjectRequest(bucket, dataId), file);

			if (!file.isFile()) {
				throw new StorageException("Not a file!");
			}

			if (file.length() == 0) {
				throw new StorageException("Document has no content!");
			}

			InputStream stream = null;
			try {
				stream = new BufferedInputStream(new FileInputStream(file),
						BUFFER_SIZE);
				if (offset != null || length != null) {
					stream = new ContentRangeInputStream(stream, offset, length);
				}
			} catch (FileNotFoundException e) {
				throw new CmisObjectNotFoundException(e.getMessage(), e);
			}

			// compile data
			ContentStreamImpl result;
			if ((offset != null && offset.longValue() > 0) || length != null) {
				result = new PartialContentStreamImpl();
			} else {
				result = new ContentStreamImpl();
			}

			result.setFileName(filename);
			result.setLength(BigInteger.valueOf(file.length()));
			result.setMimeType(MimeTypes.getMIMEType(file));
			result.setStream(stream);

			return result;

		} catch (IOException ioe) {
			throw new StorageException("Content could not be retrieved : "
					+ ioe.getMessage());
		} catch (AmazonServiceException ase) {
			System.out.println("Amazon Service Error:");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
			throw new StorageException("Content could not be retrieved : "
					+ ase.getMessage());
		} catch (AmazonClientException ace) {
			System.out.println("Amazon Client Error:");
			System.out.println("Error Message: " + ace.getMessage());
			throw new StorageException("Content could not be retrieved : "
					+ ace.getMessage());
		} catch (Exception ex) {
			System.out.println(ex.toString());
			throw new StorageException("Content could not be retrieved : "
					+ ex.getMessage());
		}
	}

	public boolean exists(String dataId) {
		debug("exists dataId = "+dataId);
		try {
			printFiles();
			s3client.getObjectMetadata(new GetObjectMetadataRequest(bucket,
					dataId));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// =========== private ===========

	private List<String> getFileList() {
		String folderName = folder;
		if (folder.length() > 0 && !folder.endsWith("/"))
			folderName = folder + "/";

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(bucket).withPrefix(folderName);
		ObjectListing objectListing;
		List<String> files = new ArrayList<String>();
		String fileName;

		do {
			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : objectListing
					.getObjectSummaries()) {
				// System.out.println(" - " + objectSummary.getKey() + "  " +
				// "(size = " + objectSummary.getSize() +
				// ")");

				fileName = objectSummary.getKey().replace(folderName, "");
				if (fileName.trim().length() > 0)
					files.add(fileName);
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());

		return files;
	}

	private void printFiles() {
		System.out.println("printFiles ...");
		List<String> files = getFileList();
		for (String file : files)
			System.out.println(file);
		System.out.println("printFiles done.");
	}

	private void debug(String msg) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("{}", msg);
		}
		System.out.println("[AWSStorageService] "+msg);
	}

	@Override
	public String getStorageId() {
		return STORAGE_ID;
	}
}
