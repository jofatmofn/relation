package org.sakuram.relation.valueobject;

import java.util.List;

public class SaveAttributesRequestVO {
	private long entityId;
	private List<AttributeValueVO> attributeValueVOList;

	public long getEntityId() {
		return entityId;
	}

	public void setEntityId(long entityId) {
		this.entityId = entityId;
	}

	public List<AttributeValueVO> getAttributeValueVOList() {
		return attributeValueVOList;
	}

	public void setAttributeValueVOList(List<AttributeValueVO> attributeValueVOList) {
		this.attributeValueVOList = attributeValueVOList;
	}
}
