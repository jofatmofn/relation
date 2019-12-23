package org.sakuram.relation.valueobject;

public class RelationVO {

	private String id;
	private String source;
	private String target;
	private String label;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setAttribute(String attributeName, String attributeValue) {
		switch(attributeName) {
			case "person1ForPerson2":
				if (getLabel() == null || getLabel().equals("")) {
					setLabel(attributeValue);
				}
				else {
					setLabel(attributeValue + "-" + getLabel());
				}
				break;
			case "person2ForPerson1":
				if (getLabel() == null || getLabel().equals("")) {
					setLabel(attributeValue);
				}
				else {
					setLabel(getLabel() + "-" + attributeValue);
				}
				break;
			default:
				System.out.println("Attribute " + attributeName + " ignored.");
		}
	}
	
}
