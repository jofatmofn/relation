package org.sakuram.relation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sakuram.relation.bean.AttributeValue;
import org.sakuram.relation.bean.DomainValue;
import org.sakuram.relation.bean.Person;
import org.sakuram.relation.bean.Relation;
import org.sakuram.relation.repository.DomainValueRepository;
import org.sakuram.relation.repository.RelationRepository;
import org.sakuram.relation.util.AppException;
import org.sakuram.relation.util.Constants;
import org.sakuram.relation.valueobject.PersonVO;
import org.sakuram.relation.valueobject.RelationVO;
import org.sakuram.relation.valueobject.GraphVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceParts {

	@Autowired
	RelationRepository relationRepository;
	@Autowired
	DomainValueRepository domainValueRepository;
	
	public GraphVO buildGraph(Set<Person> relatedPersonSet, Person startPerson) {
    	GraphVO retrieveRelationsResponseVO;
    	List<PersonVO> personVOList;
    	PersonVO personVO;
    	short childCount;
    	List<Relation> relationList;
    	List<RelationVO> relationVOList;
    	RelationVO relationVO;
    	String otherPersonId;
    	DomainValue attributeDv;
    	long relationAttributeDVIdOtherForStart;
    	
    	retrieveRelationsResponseVO = new GraphVO();
    	personVOList = new ArrayList<PersonVO>();
    	retrieveRelationsResponseVO.setNodes(personVOList);
    	relationVOList = new ArrayList<RelationVO>();
    	retrieveRelationsResponseVO.setEdges(relationVOList);
    	relationList = new ArrayList<Relation>();
    	
    	for (Person person : relatedPersonSet) {
    		personVO = new PersonVO();
    		personVOList.add(personVO);
    		
    		personVO.setId(String.valueOf(person.getId()));
    		personVO.setSize(5.0);
    		personVO.setColor(Constants.DEFAULT_COLOR);
    		if (person.equals(startPerson)) {
	    		personVO.setX(265);
	    		personVO.setY(260);
    		}
    		else {
    			personVO.setX(Math.random());
    			personVO.setY(Math.random());
    		}
    		for (AttributeValue attributeValue : person.getAttributeValueList()) {
        		if (attributeValue.getAttribute().getId() == Constants.PERSON_ATTRIBUTE_DV_ID_LABEL &&
        				(attributeValue.getStartDate() == null || attributeValue.getStartDate().toLocalDate().isBefore(LocalDate.now())) &&
        				(attributeValue.getEndDate() == null || attributeValue.getEndDate().toLocalDate().isAfter(LocalDate.now())) &&
        				attributeValue.getOverwrittenBy() == null) {
        			personVO.setLabel(attributeValue.getAttributeValue());
        			break;
        		}
    		}
    	}
    	
    	childCount = 0;
    	relationList = relationRepository.findByPerson1InAndPerson2In(relatedPersonSet, relatedPersonSet);
    	for (Relation relation : relationList) {
    		relationVO = new RelationVO();
    		relationVOList.add(relationVO);
    		
    		relationVO.setId(String.valueOf(relation.getId()));
    		relationVO.setSource(String.valueOf(relation.getPerson1().getId()));
    		relationVO.setTarget(String.valueOf(relation.getPerson2().getId()));
    		relationVO.setSize(0.5);
    		if (relation.getPerson1().equals(startPerson)) {
    			otherPersonId = String.valueOf(relation.getPerson2().getId());
    			relationAttributeDVIdOtherForStart = Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1;
    		}
    		else if (relation.getPerson2().equals(startPerson)){
    			otherPersonId = String.valueOf(relation.getPerson1().getId());
    			relationAttributeDVIdOtherForStart = Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2;
    		}
    		else {
    			otherPersonId = null;
    			relationAttributeDVIdOtherForStart = -1;
    		}
    		for (AttributeValue attributeValue : relation.getAttributeValueList()) {
        		if ((attributeValue.getAttribute().getId() == Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2 || attributeValue.getAttribute().getId() == Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1) &&
        				(attributeValue.getStartDate() == null || attributeValue.getStartDate().toLocalDate().isBefore(LocalDate.now())) &&
        				(attributeValue.getEndDate() == null || attributeValue.getEndDate().toLocalDate().isAfter(LocalDate.now())) &&
        				attributeValue.getOverwrittenBy() == null) {
            		attributeDv = domainValueRepository.findById(Long.valueOf(attributeValue.getAttributeValue()))
            				.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + attributeValue.getAttributeValue(), null));
        			relationVO.setLabel(attributeValue.getAttribute().getId(), attributeDv.getValue());
        			
        			if (otherPersonId != null && attributeValue.getAttribute().getId() == relationAttributeDVIdOtherForStart) {
            			for (PersonVO pVO : personVOList) {
            				if (pVO.getId().equals(otherPersonId)) {
		        				switch(attributeValue.getAttributeValue()) {
		        				case Constants.RELATION_NAME_FATHER:
		        					pVO.setX(265);
		        					pVO.setY(160);
		        		    		break;
		        				case Constants.RELATION_NAME_MOTHER:
		        					pVO.setX(530);
		        					pVO.setY(160);
		        		    		break;
		        				case Constants.RELATION_NAME_HUSBAND:
		        				case Constants.RELATION_NAME_WIFE:
		        					pVO.setX(530);
		        					pVO.setY(260);
		        		    		break;
		        				case Constants.RELATION_NAME_SON:
		        				case Constants.RELATION_NAME_DAUGHTER:
		        			    	childCount++;
		        			    	pVO.setX(260 + childCount * 50);
		        			    	pVO.setY(360);
		        		    		break;
		        				}
		        				break;
            				}
            			}
        			}
        		}
    		}
    	}
    	
    	return retrieveRelationsResponseVO;
	}
}
