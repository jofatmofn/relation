package org.sakuram.relation.valueobject;

import java.util.List;

public class RetrieveRelationAttributesResponseVO {
	Long person1GenderDVId;
	Long person2GenderDVId;
	List<AttributeValueVO> attributeValueVOList;
	Long person1Id;
	
	public Long getPerson1GenderDVId() {
		return person1GenderDVId;
	}
	
	public void setPerson1GenderDVId(Long person1GenderDVId) {
		this.person1GenderDVId = person1GenderDVId;
	}
	
	public Long getPerson2GenderDVId() {
		return person2GenderDVId;
	}
	
	public void setPerson2GenderDVId(Long person2GenderDVId) {
		this.person2GenderDVId = person2GenderDVId;
	}
	
	public List<AttributeValueVO> getAttributeValueVOList() {
		return attributeValueVOList;
	}
	
	public void setAttributeValueVOList(List<AttributeValueVO> attributeValueVOList) {
		this.attributeValueVOList = attributeValueVOList;
	}

	public Long getPerson1Id() {
		return person1Id;
	}

	public void setPerson1Id(Long person1Id) {
		this.person1Id = person1Id;
	}
}
