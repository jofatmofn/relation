package org.sakuram.relation.util;

import org.sakuram.relation.bean.DomainValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class DomainValueFlags {
	private String attributeDomain, repetitionType, validationJsRegEx, languageCode;
	private Boolean isRelationParentChild, isRelationSpouse, isInputAsAttribute, isInputMandatory, isTranslatable;
	
	public DomainValueFlags(DomainValue domainValue) {
		setDomainValue(domainValue);
	}
	
	public void setDomainValue(DomainValue domainValue) {
    	String flagsArr[];
    	
		attributeDomain = null;
		repetitionType = null;
		validationJsRegEx = null;
		languageCode = null;
		isRelationParentChild = null;
		isRelationSpouse = null;
		isInputAsAttribute = null;
		isInputMandatory = null;
		isTranslatable = null;
		
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
		} else if (domainValue.getCategory().equals(Constants.CATEGORY_PERSON_ATTRIBUTE) || domainValue.getCategory().equals(Constants.CATEGORY_RELATION_ATTRIBUTE)) {
			isTranslatable = false;
			if (flagsArr.length > Constants.FLAG_POSITION_INPUT_AS_ATTRIBUTE) {
				isInputAsAttribute = Boolean.valueOf(flagsArr[Constants.FLAG_POSITION_INPUT_AS_ATTRIBUTE]);    				
			}
			if (flagsArr.length > Constants.FLAG_POSITION_REPETITION) {
				repetitionType = flagsArr[Constants.FLAG_POSITION_REPETITION];
			}
			if (flagsArr.length > Constants.FLAG_POSITION_DOMAIN) {
				attributeDomain = flagsArr[Constants.FLAG_POSITION_DOMAIN];
			}
			if (flagsArr.length > Constants.FLAG_POSITION_INPUT_MANDATORY) {
				isInputMandatory = Boolean.valueOf(flagsArr[Constants.FLAG_POSITION_INPUT_MANDATORY]);
			}
			if (flagsArr.length > Constants.FLAG_POSITION_VALIDATION_JS_REG_EX) {
				validationJsRegEx = flagsArr[Constants.FLAG_POSITION_VALIDATION_JS_REG_EX];
				if (validationJsRegEx.equals(Constants.TRANSLATABLE_REGEX)) {
					isTranslatable = true;
				}
			}
		} else if (domainValue.getCategory().equals(Constants.CATEGORY_LANGUAGE)) {
			if (flagsArr.length > Constants.FLAG_POSITION_ISO_LANGUAGE_CODE) {
				languageCode = flagsArr[Constants.FLAG_POSITION_ISO_LANGUAGE_CODE];
			}
		}
		
	}
	
}
