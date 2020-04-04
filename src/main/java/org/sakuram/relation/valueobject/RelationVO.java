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

	public String getNormalisedLabel() {
		// Beware: Because of the ids 34, 35, 61, 62, the pattern \d\d is used below
		return (label == null ? null : label.replaceAll("@@\\d\\d@@", "").replaceAll("\\(\\)", ""));
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void buildLabel(long attributeDvId, String attributeValue) {
		if (label == null) {
			label = Constants.RELATION_LABEL_TEMPLATE;
		}
		label = label.replaceAll("@@" + attributeDvId + "@@", attributeValue);
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}
	
}
