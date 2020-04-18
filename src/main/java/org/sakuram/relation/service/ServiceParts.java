package org.sakuram.relation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.sakuram.relation.bean.AttributeValue;
import org.sakuram.relation.bean.DomainValue;
import org.sakuram.relation.bean.Person;
import org.sakuram.relation.bean.Relation;
import org.sakuram.relation.repository.DomainValueRepository;
import org.sakuram.relation.repository.RelationRepository;
import org.sakuram.relation.util.AppException;
import org.sakuram.relation.util.Constants;
import org.sakuram.relation.util.DomainValueFlags;
import org.sakuram.relation.valueobject.PersonVO;
import org.sakuram.relation.valueobject.RelationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceParts {

	@Autowired
	RelationRepository relationRepository;
	@Autowired
	DomainValueRepository domainValueRepository;
	
	public PersonVO addToPersonVOMap(Map<Long, PersonVO> personVOMap, Person person) {
    	PersonVO personVO;
    	
		personVO = new PersonVO();
		personVOMap.put(person.getId(), personVO);
		
		personVO.setId(String.valueOf(person.getId()));
		personVO.setSize(5.0);
		personVO.setColor(Constants.DEFAULT_COLOR);
		
		for (AttributeValue attributeValue : person.getAttributeValueList()) {
    		if (attributeValue.getAttribute().getId() == Constants.PERSON_ATTRIBUTE_DV_ID_LABEL &&
    				(attributeValue.getStartDate() == null || attributeValue.getStartDate().toLocalDate().isBefore(LocalDate.now())) &&
    				(attributeValue.getEndDate() == null || attributeValue.getEndDate().toLocalDate().isAfter(LocalDate.now())) &&
    				attributeValue.getOverwrittenBy() == null) {
    			personVO.setLabel(attributeValue.getAttributeValue());
    			// TODO: What if domainValueFlags.getAttributeDomain() not empty
    			break;
    		}
		}
		
		return personVO;
	}
	
	public RelatedPerson1VO addToRelationVOList(List<RelationVO> relationVOList, Relation relation, Person startPerson) {
		RelationVO relationVO;
    	DomainValueFlags domainValueFlags;
    	DomainValue attributeDv;
    	String otherPersonId;
    	RelatedPerson1VO relatedPerson1VO;
    	long relationAttributeDVIdOtherForStart;
    	
		relationVO = new RelationVO();
		relationVOList.add(relationVO);
    	domainValueFlags = new DomainValueFlags();
    	relatedPerson1VO = new RelatedPerson1VO();
		
		relationVO.setId(String.valueOf(relation.getId()));
		relationVO.setSource(String.valueOf(relation.getPerson1().getId()));
		relationVO.setTarget(String.valueOf(relation.getPerson2().getId()));
		relationVO.setSize(0.5);
		if (relation.getPerson1().equals(startPerson)) {
			relatedPerson1VO.person = relation.getPerson2();
			otherPersonId = String.valueOf(relation.getPerson2().getId());
			relationAttributeDVIdOtherForStart = Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1;
		}
		else if (relation.getPerson2().equals(startPerson)){
			relatedPerson1VO.person = relation.getPerson1();
			otherPersonId = String.valueOf(relation.getPerson1().getId());
			relationAttributeDVIdOtherForStart = Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2;
		}
		else {
			otherPersonId = null;
			relationAttributeDVIdOtherForStart = -1;
		}
		for (AttributeValue attributeValue : relation.getAttributeValueList()) {
    		if (isCurrentValidAttributeValue(attributeValue)) {
        		domainValueFlags.setDomainValue(attributeValue.getAttribute());
        		if (domainValueFlags.getAttributeDomain().equals("")) {
        			relationVO.buildLabel(attributeValue.getAttribute().getId(), attributeValue.getAttributeValue());
        		}
        		else {
            		attributeDv = domainValueRepository.findById(Long.valueOf(attributeValue.getAttributeValue()))
            				.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + attributeValue.getAttributeValue(), null));
        			relationVO.buildLabel(attributeValue.getAttribute().getId(), attributeDv.getValue());
        		}
    			if (otherPersonId != null && attributeValue.getAttribute().getId() == relationAttributeDVIdOtherForStart) {
    				relatedPerson1VO.relationDvId = attributeValue.getAttributeValue();
    			}
    		}
		}
		relationVO.setLabel(relationVO.getNormalisedLabel());
		return relatedPerson1VO;
	}
	
    public boolean isCurrentValidAttributeValue(AttributeValue attributeValue) {
		if ((attributeValue.getStartDate() == null || attributeValue.getStartDate().toLocalDate().isBefore(LocalDate.now())) &&
				(attributeValue.getEndDate() == null || attributeValue.getEndDate().toLocalDate().isAfter(LocalDate.now())) &&
				attributeValue.getOverwrittenBy() == null) {
			return true;
		}
		else {
			return false;
		}
    }
    
    protected class RelatedPerson1VO {
    	Person person;
    	Relation relation;
    	String relationDvId;
    }

}
