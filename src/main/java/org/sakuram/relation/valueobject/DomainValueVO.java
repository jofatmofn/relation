package org.sakuram.relation.valueobject;

public class DomainValueVO {
	private int id;	
	private String category;	
	private String value;	
	private boolean isRelationParentChild;	
	private boolean isRelationSpouse;
	private boolean isInputAsAttribute;
	private String repetitionType;
	private String attributeDomain;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isRelationParentChild() {
		return isRelationParentChild;
	}

	public void setRelationParentChild(boolean isRelationParentChild) {
		this.isRelationParentChild = isRelationParentChild;
	}

	public boolean isRelationSpouse() {
		return isRelationSpouse;
	}

	public void setRelationSpouse(boolean isRelationSpouse) {
		this.isRelationSpouse = isRelationSpouse;
	}

	public boolean isInputAsAttribute() {
		return isInputAsAttribute;
	}

	public void setInputAsAttribute(boolean isInputAsAttribute) {
		this.isInputAsAttribute = isInputAsAttribute;
	}

	public String getRepetitionType() {
		return repetitionType;
	}

	public void setRepetitionType(String repetitionType) {
		this.repetitionType = repetitionType;
	}

	public String getAttributeDomain() {
		return attributeDomain;
	}

	public void setAttributeDomain(String attributeDomain) {
		this.attributeDomain = attributeDomain;
	}
	
}
