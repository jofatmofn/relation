package org.sakuram.relation.valueobject;

public class DomainValueVO {
	private long id;	
	private String category;	
	private String value;	
	private boolean isRelationParentChild;	
	private boolean isRelationSpouse;
	private boolean isInputAsAttribute;
	private String repetitionType;
	private String attributeDomain;
	private boolean isInputMandatory;

	public long getId() {
		return id;
	}

	public void setId(long id) {
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

	public boolean isInputMandatory() {
		return isInputMandatory;
	}

	public void setInputMandatory(boolean isInputMandatory) {
		this.isInputMandatory = isInputMandatory;
	}
	
}
