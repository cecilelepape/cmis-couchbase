package org.apache.chemistry.opencmis.couchbase;

import java.math.BigInteger;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

public interface StorageService {

	public String getStorageId();
	
	public void close();
	
	/**
	 * Write a content.
	 * 
	 * @param dataId
	 *            the content identifier.
	 * @param contentStream the stream of the content
	 * @return
	 */
	public void writeContent(String dataId, ContentStream contentStream)
			throws StorageException;
	
	
	/**
	 * Delete a content. Since folder are not materialized, only document are
	 * deleted from the storage system.
	 * 
	 * @param dataId
	 *            the content identifier.
	 * @return
	 */
	public boolean deleteContent(String dataId);

	public org.apache.chemistry.opencmis.commons.data.ContentStream getContent(
			String dataId, BigInteger offset, BigInteger length, String filename) throws StorageException;

	/**
	 * Tests if a content is already stored.
	 * @param dataId the content identifier
	 * @return true if the content exists
	 */
	public boolean exists(String dataId);

}
