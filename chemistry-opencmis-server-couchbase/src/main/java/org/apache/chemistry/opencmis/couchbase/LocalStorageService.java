package org.apache.chemistry.opencmis.couchbase;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PartialContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalStorageService implements StorageService{

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalStorageService.class);

	private static final int BUFFER_SIZE = 64 * 1024;
	private static final String STORAGE_ID = "local";
	
	final File root;

	public LocalStorageService(String rootpath) {
		root = new File(rootpath);
	}

	public void close() {
	}

	public void writeContent(String dataId, ContentStream contentStream)
			throws StorageException {
		debug("writeContent dataId:"+dataId);
		if (root == null)
			throw new StorageException("Root folder does not exist");
		File newFile = new File(root, dataId);
		if (newFile.exists()) {
			throw new StorageException("Document already exists!");
		}

		// create the file
		try {
			newFile.createNewFile();
		} catch (IOException e) {
			throw new StorageException("Could not create file: "
					+ e.getMessage());
		}

		// write content, if available
		if (contentStream != null && contentStream.getStream() != null) {
			writeContent(newFile, contentStream.getStream());
		}

	}

	/**
	 * Delete a content. Since folder are not materialized, only document are
	 * deleted from the storage system.
	 * 
	 * @param dataId
	 *            the content identifier.
	 * @return
	 */
	public boolean deleteContent(String dataId) {
		try {
			File file = getFile(dataId); 

			if (file == null || !file.isFile()) {
				throw new CmisStreamNotSupportedException("Not a file!");
			}

			if (file.length() == 0) {
				throw new CmisConstraintException("Document has no content!");
			}
			
			return file.delete();
		} catch (StorageException e) {
			throw new CmisObjectNotFoundException(
					"The object cannot be deleted");
		}
	}

	/**
	 * Writes the content to disc.
	 */
	private void writeContent(File newFile, InputStream stream) {
		OutputStream out = null;
		try {
			out = new FileOutputStream(newFile);
			IOUtils.copy(stream, out, BUFFER_SIZE);
		} catch (IOException e) {
			throw new CmisStorageException("Could not write content: "
					+ e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(stream);
		}
	}

	public org.apache.chemistry.opencmis.commons.data.ContentStream getContent(
			String dataId, BigInteger offset, BigInteger length, String filename) {
		debug("getContent not yet implemented");
		try {
			File file = getFile(dataId);
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
		} catch (Exception e) {
			throw new CmisObjectNotFoundException(e.getMessage(), e);
		}

	}

	public boolean exists(String dataId) {
		try {
			File file = getFile(dataId);
			if (file == null)
				return false;
			return file.exists();
		} catch (StorageException e) {
			e.printStackTrace();
			return false;
		}
	}

	private File getFile(String dataId) throws StorageException {
		if (dataId == null || dataId.length() == 0) {
			throw new CmisInvalidArgumentException("Id is not valid!");
		}

		if (root == null)
			throw new StorageException("Root folder does not exist");

		return new File(root, dataId);
	}

	private void debug(String msg) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("{}", msg);
		}
	}

	@Override
	public String getStorageId() {
		return STORAGE_ID;
	}
}
