package org.sakuram.relation.valueobject;

import java.util.HashMap;
import java.util.Map;

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

	public String getNormalisedLabel(boolean toIncludeRelationId) {
		if (getLabel() != null) {
			return label;
		}
		label = Constants.RELATION_LABEL_TEMPLATE;
		if (attributeMap.containsKey(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2) && attributeMap.containsKey(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1)) {
			label = label.replaceAll("@@" + Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2 + "@@", (toSwap ? attributeMap.get(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1) : attributeMap.get(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2)))
				.replaceAll("@@" + Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1 + "@@", (toSwap ? attributeMap.get(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2) : attributeMap.get(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1)));
		}
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

	public void setAttribute(long attributeDvId, String attributeValue) {
		attributeMap.put(attributeDvId, attributeValue);
	}

	@JsonIgnore public String getPerson1ForPerson2DvId() {
		if (toSwap) {
			return Constants.RELATION_NAME_TO_ID_MAP.get(attributeMap.get(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1));
		}
		else {
			return Constants.RELATION_NAME_TO_ID_MAP.get(attributeMap.get(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2));
		}
	}

	@JsonIgnore public String getPerson2ForPerson1DvId() {
		if (toSwap) {
			return Constants.RELATION_NAME_TO_ID_MAP.get(attributeMap.get(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2));
		}
		else {
			return Constants.RELATION_NAME_TO_ID_MAP.get(attributeMap.get(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1));
		}
	}

	public boolean isToSwap() {
		return toSwap;
	}

	public void setToSwap(boolean toSwap) {
		this.toSwap = toSwap;
	}
}
