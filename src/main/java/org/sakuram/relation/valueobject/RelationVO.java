package org.sakuram.relation.valueobject;

import org.sakuram.relation.util.Constants;

public class RelationVO {

	private String id;
	private String source;
	private String target;
	private String label;
	private double size;

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

	public void setLabel(long attributeDvId, String attributeValue) {
		if (attributeDvId == Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2) {
			if (label == null || label.equals("")) {
				label = attributeValue;
			}
			else {
				label = attributeValue + "-" + label;
			}
		}
		else if (attributeDvId == Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1) {
			if (label == null || label.equals("")) {
				label = attributeValue;
			}
			else {
				label = label + "-" + attributeValue;
			}
		}
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}
	
}
