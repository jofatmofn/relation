package org.sakuram.relation.valueobject;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sakuram.relation.util.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class RelationVO {

	private String id;
	private String source;
	private String target;
	private String label;
	private double size;
	private String type;
	@JsonIgnore private Map<Long, String> attributeMap;
	@JsonIgnore private boolean toSwap;

	public RelationVO() {
		attributeMap = new HashMap<Long, String>();
	}
	
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
		if (toSwap) {
			this.target = source;
		} else {
			this.source = source;
		}
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		if (toSwap) {
			this.source = target;
		} else {
			this.target = target;
		}
	}

	public String getLabel() {
		return label;
	}

	public String getNormalisedLabel(boolean toIncludeRelationId) {
		if (getLabel() != null) {
			return label;
		}
		label = Constants.RELATION_LABEL_TEMPLATE;
		for (Map.Entry<Long, String> attributeEntry : attributeMap.entrySet()) {
			label = label.replaceAll("@@" + attributeEntry.getKey() + "@@", attributeEntry.getValue());
		}
		// Beware: Because of the ids 34, 35, 36, 61, 62, the pattern \d\d is used below
		return (toIncludeRelationId ? "<" + id + ">" : "") + label.replaceAll("@@\\d\\d@@", "").replaceAll("\\(\\)", "");
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAttribute(long attributeDvId) {
		if(attributeMap.containsKey(attributeDvId)) {
			return attributeMap.get(attributeDvId);
		} else {
			return "";
		}
	}

	public void setAttribute(long attributeDvId, String attributeValue) {
		if (toSwap) {
			if (attributeDvId == Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2) {
				attributeMap.put(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1, attributeValue);
			} else if (attributeDvId == Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1) {
				attributeMap.put(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2, attributeValue);
			} else if (attributeDvId == Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2) {
				attributeMap.put(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1, attributeValue);
			} else if (attributeDvId == Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1) {
				attributeMap.put(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2, attributeValue);
			} else {
				attributeMap.put(attributeDvId, attributeValue);
			}
		} else {
			attributeMap.put(attributeDvId, attributeValue);
		}
	}

	public boolean isToSwap() {
		return toSwap;
	}

	public void setToSwap(boolean toSwap) {
		this.toSwap = toSwap;
	}
	
	public String toString() {
		StringBuffer sb;
		sb = new StringBuffer(1000);
		sb.append("Source: ");
		sb.append(source);
		sb.append("\n");
		sb.append("Target: ");
		sb.append(target);
		sb.append("\n");
		sb.append("Label: ");
		sb.append(label);
		sb.append("\n");
		for (Entry<Long, String> attributeEntry : attributeMap.entrySet()) {
			sb.append(attributeEntry.getKey());
			sb.append(": ");
			sb.append(attributeEntry.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}
}
