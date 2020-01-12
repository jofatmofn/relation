package org.sakuram.relation.valueobject;

import java.sql.Date;

public class AttributeValueVO {
	private long id;
	private long attributeDvId;
	private String attributeName;
	private String attributeValue;
	private boolean isValueAccurate;
	private Date startDate;
	private Date endDate;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getAttributeDvId() {
		return attributeDvId;
	}

	public void setAttributeDvId(long attributeDvId) {
		this.attributeDvId = attributeDvId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	public boolean isValueAccurate() {
		return isValueAccurate;
	}

	public void setValueAccurate(boolean isValueAccurate) {
		this.isValueAccurate = isValueAccurate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}
