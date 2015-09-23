package org.apache.chemistry.opencmis.couchbase;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class CmisObject {

	static private final String CMIS_FOLDER = "cmis:folder";

	private String id;
	private String type;
	private String name;
	private String createdBy;
	private String lastModifiedBy;
	private String contentType;
	private String fileName = null;
	private GregorianCalendar creationDate;
	private GregorianCalendar lastModificationDate;
	private String parentId = null;
	private String path = null;
	private List<String> children = new ArrayList<String>();;

	public CmisObject(String objectId) {
		this.id = objectId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getLastModifiedBy() {
		return this.lastModifiedBy;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setFileName(String filename) {
		this.fileName = filename;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setCreationDate(GregorianCalendar cal) {
		this.creationDate = cal;
	}

	public GregorianCalendar getCreationDate() {
		return this.creationDate;
	}

	public void setLastModificationDate(GregorianCalendar cal) {
		this.lastModificationDate = cal;
	}

	public GregorianCalendar getLastModificationDate() {
		return this.lastModificationDate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getParentId() {
		return this.parentId;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	public String getFullPath() {
		String path = getPath();
		if (path == null)
			return null;
		if (isRoot())
			return path;
		return path + getName();
	}

	public void addChildren(String childrenId) {
		if (children == null) {
			children = new ArrayList<String>();
		}
		children.add(childrenId);
	}

	public boolean removeChildren(String childrenId) {
		if (children == null)
			return false;
		return children.remove(childrenId);
	}

	public List<String> getChildren() {
		return this.children;
	}

	public boolean isDirectory() {
		if (CMIS_FOLDER.equals(getType())) {
			return true;
		}
		return false;
	}

	/**
	 * Must interrogate the File System to determine is the document or the
	 * folder exists.
	 */
	public boolean exists() {
		return true;
	}

	public boolean isRoot() {
		return CouchbaseRepository.ROOT_ID.equals(this.getId());
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("id:" + this.getId() + "\n");
		buf.append("type:" + this.getType() + "\n");
		buf.append("name:" + this.getName() + "\n");
		buf.append("createdBy:" + this.getCreatedBy() + "\n");
		buf.append("contentType:" + this.getContentType() + "\n");
		buf.append("fileName:" + this.getFileName() + "\n");
		buf.append("creationDate:" + this.getCreationDate() + "\n");
		buf.append("lastModificationDate:" + this.getLastModificationDate()
				+ "\n");
		buf.append("parentId:" + this.getParentId() + "\n");
		buf.append("path:" + this.getPath() + "\n");

		return buf.toString();
	}
}
