package org.sakuram.relation.valueobject;

public class RelatedPersonsVO {
	private long person1Id;
	private long person2Id;
	private String person1ForPerson2;
	private long creatorId;
	private String excludeRelationIdCsv;
	
	public long getPerson1Id() {
		return person1Id;
	}
	
	public void setPerson1Id(long person1Id) {
		this.person1Id = person1Id;
	}
	
	public long getPerson2Id() {
		return person2Id;
	}
	
	public void setPerson2Id(long person2Id) {
		this.person2Id = person2Id;
	}
	
	public String getPerson1ForPerson2() {
		return person1ForPerson2;
	}

	public void setPerson1ForPerson2(String person1ForPerson2) {
		this.person1ForPerson2 = person1ForPerson2;
	}

	public long getCreatorId() {
		return creatorId;
	}
	
	public void setCreatorId(long creatorId) {
		this.creatorId = creatorId;
	}

	public String getExcludeRelationIdCsv() {
		return excludeRelationIdCsv;
	}

	public void setExcludeRelationIdCsv(String excludeRelationIdCsv) {
		this.excludeRelationIdCsv = excludeRelationIdCsv;
	}

}
