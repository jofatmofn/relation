package org.sakuram.relation.util;

import org.sakuram.relation.bean.DomainValue;

public class DomainValueFlags {
	private String attributeDomain, repetitionType, validationJsRegEx;
	private boolean isRelationParentChild, isRelationSpouse, isInputAsAttribute, isInputMandatory;
	
	public void setDomainValue(DomainValue domainValue) {
    	String flagsArr[];
    	
		attributeDomain = "";
		if (domainValue.getFlagsCsv() != null && !domainValue.getFlagsCsv().equals("")) {
			flagsArr = domainValue.getFlagsCsv().split(Constants.CSV_SEPARATOR);
		}
		else {
			flagsArr = new String[0];
		}
		if (domainValue.getCategory().equals(Constants.CATEGORY_RELATION_NAME) || domainValue.getCategory().equals(Constants.CATEGORY_RELATION_SUB_TYPE)) {
			if (flagsArr.length > Constants.FLAG_POSITION_RELATION_TYPE && flagsArr[Constants.FLAG_POSITION_RELATION_TYPE].equals(Constants.FLAG_RELATION_TYPE_PARENT_CHILD)) {
				isRelationParentChild = true;
			}
			else {
				isRelationParentChild = false;
			}
			if (flagsArr.length > Constants.FLAG_POSITION_RELATION_TYPE && flagsArr[Constants.FLAG_POSITION_RELATION_TYPE].equals(Constants.FLAG_RELATION_TYPE_SPOUSE)) {
				isRelationSpouse = true;
			}
			else {
				isRelationSpouse = false;
			}
		}
		if (domainValue.getCategory().equals(Constants.CATEGORY_PERSON_ATTRIBUTE) || domainValue.getCategory().equals(Constants.CATEGORY_RELATION_ATTRIBUTE)) {
			if (flagsArr.length > Constants.FLAG_POSITION_INPUT_AS_ATTRIBUTE) {
				isInputAsAttribute = new Boolean(flagsArr[Constants.FLAG_POSITION_INPUT_AS_ATTRIBUTE]);    				
			}
			if (flagsArr.length > Constants.FLAG_POSITION_REPETITION) {
				repetitionType = flagsArr[Constants.FLAG_POSITION_REPETITION];
			}
			if (flagsArr.length > Constants.FLAG_POSITION_DOMAIN) {
				attributeDomain = flagsArr[Constants.FLAG_POSITION_DOMAIN];
			}
			if (flagsArr.length > Constants.FLAG_POSITION_INPUT_MANDATORY) {
				isInputMandatory = new Boolean(flagsArr[Constants.FLAG_POSITION_INPUT_MANDATORY]);
			}
			if (flagsArr.length > Constants.FLAG_POSITION_VALIDATION_JS_REG_EX) {
				validationJsRegEx = flagsArr[Constants.FLAG_POSITION_VALIDATION_JS_REG_EX];
			}
		}
		
	}
	
	public boolean isRelationParentChild() {
		return isRelationParentChild;
	}
	
	public boolean isRelationSpouse() {
		return isRelationSpouse;
	}

	public boolean isInputAsAttribute() {
		return isInputAsAttribute;
	}
	
	public String getRepetitionType() {
		return repetitionType;
	}
	
	public String getAttributeDomain() {
		return attributeDomain;
	}
	
	public boolean isInputMandatory() {
		return isInputMandatory;
	}
	
	public String getValidationJsRegEx() {
		return validationJsRegEx;
	}
	
}
