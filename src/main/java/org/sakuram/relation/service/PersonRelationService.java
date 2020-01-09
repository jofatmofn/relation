package org.sakuram.relation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakuram.relation.bean.AttributeValue;
import org.sakuram.relation.bean.DomainValue;
import org.sakuram.relation.bean.Person;
import org.sakuram.relation.bean.Relation;
import org.sakuram.relation.repository.AttributeValueRepository;
import org.sakuram.relation.repository.DomainValueRepository;
import org.sakuram.relation.repository.PersonRepository;
import org.sakuram.relation.repository.RelationRepository;
import org.sakuram.relation.util.AppException;
import org.sakuram.relation.util.Constants;
import org.sakuram.relation.util.DomainValueFlags;
import org.sakuram.relation.valueobject.AttributeValueVO;
import org.sakuram.relation.valueobject.DomainValueVO;
import org.sakuram.relation.valueobject.PersonVO;
import org.sakuram.relation.valueobject.RelationVO;
import org.sakuram.relation.valueobject.RetrieveRelationsRequestVO;
import org.sakuram.relation.valueobject.RetrieveRelationsResponseVO;
import org.sakuram.relation.valueobject.SaveAttributesRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PersonRelationService {
	
	@Autowired
	PersonRepository personRepository;
	@Autowired
	RelationRepository relationRepository;
	@Autowired
	DomainValueRepository domainValueRepository;
	@Autowired
	AttributeValueRepository attributeValueRepository;
	
	public RetrieveRelationsResponseVO retrieveRelations(RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	RetrieveRelationsResponseVO retrieveRelationsResponseVO;
    	Person startPerson;
    	List<PersonVO> personVOList;
    	PersonVO personVO;
    	List<Relation> relationList, participatingRelationList;
    	List<RelationVO> relationVOList;
    	RelationVO relationVO;
    	Set<Person> relatedPersonSet;
    	
    	retrieveRelationsResponseVO = new RetrieveRelationsResponseVO();
    	personVOList = new ArrayList<PersonVO>();
    	retrieveRelationsResponseVO.setNodes(personVOList);
    	relationVOList = new ArrayList<RelationVO>();
    	retrieveRelationsResponseVO.setEdges(relationVOList);
    	relatedPersonSet = new HashSet<Person>();
    	relationList = new ArrayList<Relation>();
    	
    	startPerson = personRepository.findById(retrieveRelationsRequestVO.getStartPersonId())
				.orElseThrow(() -> new AppException("Invalid Person " + retrieveRelationsRequestVO.getStartPersonId(), null));
		relatedPersonSet.add(startPerson);
    	participatingRelationList = relationRepository.findByPerson1(startPerson);
    	for (Relation relation : participatingRelationList) {
    		relatedPersonSet.add(relation.getPerson2());
    	}
    	participatingRelationList = relationRepository.findByPerson2(startPerson);
    	for (Relation relation : participatingRelationList) {
    		relatedPersonSet.add(relation.getPerson1());
    	}
    	for (Person person : relatedPersonSet) {
    		personVO = new PersonVO();
    		personVOList.add(personVO);
    		
    		personVO.setId(String.valueOf(person.getId()));
    		personVO.setSize(5.0);
    		personVO.setColor("rgb(1,179,255)");
    		personVO.setX(Math.random());
    		personVO.setY(Math.random());
    		for (AttributeValue attributeValue : person.getAttributeValueList()) {
        		if (attributeValue.getAttribute().getId() == Constants.PERSON_ATTRIBUTE_DV_ID_LABEL &&
        				(attributeValue.getStartDate() == null || attributeValue.getStartDate().toLocalDate().isBefore(LocalDate.now())) &&
        				(attributeValue.getEndDate() == null || attributeValue.getEndDate().toLocalDate().isAfter(LocalDate.now()))) {
        			personVO.setLabel(attributeValue.getAttributeValue());
        			break;
        		}
    		}
    	}
    	
    	relationList = relationRepository.findByPerson1InAndPerson2In(relatedPersonSet, relatedPersonSet);
    	for (Relation relation : relationList) {
    		relationVO = new RelationVO();
    		relationVOList.add(relationVO);
    		
    		relationVO.setId(String.valueOf(relation.getId()));
    		relationVO.setSource(String.valueOf(relation.getPerson1().getId()));
    		relationVO.setTarget(String.valueOf(relation.getPerson2().getId()));
    		relationVO.setSize(0.5);
    		for (AttributeValue attributeValue : relation.getAttributeValueList()) {
        		if ((attributeValue.getAttribute().getId() == Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2 || attributeValue.getAttribute().getId() == Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1) &&
        				(attributeValue.getStartDate() == null || attributeValue.getStartDate().toLocalDate().isBefore(LocalDate.now())) &&
        				(attributeValue.getEndDate() == null || attributeValue.getEndDate().toLocalDate().isAfter(LocalDate.now()))) {
        			relationVO.setLabel(attributeValue.getAttribute().getId(), attributeValue.getAttributeValue());
        		}
    		}
    	}
    	
    	return retrieveRelationsResponseVO;
    }
	
    public List<DomainValueVO> retrieveDomainValues() {
    	DomainValueVO domainValueVO;
    	List<DomainValueVO> domainValueVOList;
    	List<DomainValue> domainValueList;
    	DomainValueFlags domainValueFlags;
    	
    	domainValueFlags = new DomainValueFlags();
    	domainValueList = domainValueRepository.findAll();
    	domainValueVOList = new ArrayList<DomainValueVO>(domainValueList.size());
    	
    	for (DomainValue domainValue : domainValueList) {
    		domainValueVO = new DomainValueVO();
    		domainValueVOList.add(domainValueVO);
    		
    		domainValueVO.setId(domainValue.getId());
    		domainValueVO.setCategory(domainValue.getCategory());
    		domainValueVO.setValue(domainValue.getValue());
    		
    		domainValueFlags.setDomainValue(domainValue);
    		domainValueVO.setRelationParentChild(domainValueFlags.isRelationParentChild());
    		domainValueVO.setRelationSpouse(domainValueFlags.isRelationSpouse());
    		domainValueVO.setInputAsAttribute(domainValueFlags.isInputAsAttribute());
    		domainValueVO.setRepetitionType(domainValueFlags.getRepetitionType());
    		domainValueVO.setAttributeDomain(domainValueFlags.getAttributeDomain());
    		domainValueVO.setInputMandatory(domainValueFlags.isInputMandatory());
    	}
    	
    	return domainValueVOList;
    }
    
    public List<AttributeValueVO> retrieveAttributes(byte entityType, long entityId) {
    	Person person;
    	Relation relation;
    	List<AttributeValue> attributeValueList;
    	List<AttributeValueVO> attributeValueVOList;
    	AttributeValueVO attributeValueVO;
    	DomainValueFlags domainValueFlags;
    	
    	domainValueFlags = new DomainValueFlags();
    	if (entityType == Constants.ENTITY_TYPE_PERSON) {
    		person = personRepository.findById(entityId)
    				.orElseThrow(() -> new AppException("Invalid Person " + entityId, null));
    		attributeValueList = person.getAttributeValueList();
    	}
    	else if (entityType == Constants.ENTITY_TYPE_RELATION) {
    		relation = relationRepository.findById(entityId)
    				.orElseThrow(() -> new AppException("Invalid Relation " + entityId, null));
    		attributeValueList = relation.getAttributeValueList();
    	}
    	else {
    		throw new AppException("Unknow entity type " + entityType, null);
    	}
    	
    	attributeValueVOList = new ArrayList<AttributeValueVO>();
    	for(AttributeValue attributeValue : attributeValueList) {
    		
    		domainValueFlags.setDomainValue(attributeValue.getAttribute());
    		if ((attributeValue.getStartDate() == null || attributeValue.getStartDate().toLocalDate().isBefore(LocalDate.now())) &&
    				(attributeValue.getEndDate() == null || attributeValue.getEndDate().toLocalDate().isAfter(LocalDate.now())) &&
    				domainValueFlags.isInputAsAttribute()) {
        		attributeValueVO = new AttributeValueVO();
        		attributeValueVOList.add(attributeValueVO);
        		attributeValueVO.setId(attributeValue.getId());
        		attributeValueVO.setAttributeDvId(attributeValue.getAttribute().getId());
        		attributeValueVO.setAttributeName(attributeValue.getAttribute().getValue());
        		attributeValueVO.setAttributeValue(attributeValue.getAttributeValue());
        		attributeValueVO.setValueAccurate(attributeValue.isValueAccurate());
        		attributeValueVO.setStartDate(attributeValue.getStartDate());
        		attributeValueVO.setEndDate(attributeValue.getEndDate());
    		}
    	}
    	return attributeValueVOList;
    }
    
    public void savePersonAttributes(SaveAttributesRequestVO saveAttributesRequestVO) {
    	Person person, creator;
    	List<AttributeValue> attributeValueList;
    	
    	if (saveAttributesRequestVO.getEntityId() == Constants.NEW_ENTITY_ID) {
        	creator = personRepository.findById(6L)
    				.orElseThrow(() -> new AppException("Invalid Person Id " + 6L, null));  // TODO: After integration with login, this should be user's person id
    		person = new Person();
    		person.setCreator(creator);
    	}
    	else {
    		person = personRepository.findById(saveAttributesRequestVO.getEntityId())
    				.orElseThrow(() -> new AppException("Invalid Person " + saveAttributesRequestVO.getEntityId(), null));
    		deleteExistingInputAttributes(person.getAttributeValueList());
    		// TODO: Delete only the modified values. Else the creator id and timestamp will be lost.
    	}
    	
    	attributeValueList = attributeValueVOToEntity(saveAttributesRequestVO.getAttributeValueVOList(), person, null);
    	
		person.setAttributeValueList(attributeValueList);
		personRepository.save(person);
    }
    
    public void saveRelationAttributes(SaveAttributesRequestVO saveAttributesRequestVO) {
    	Relation relation = null;
    	List<AttributeValue> attributeValueList;
    	
		relation = relationRepository.findById(saveAttributesRequestVO.getEntityId())
				.orElseThrow(() -> new AppException("Invalid Relation " + saveAttributesRequestVO.getEntityId(), null));
		deleteExistingInputAttributes(relation.getAttributeValueList());
		// TODO: Delete only the modified values. Else the creator id and timestamp will be lost.
    	
    	attributeValueList = attributeValueVOToEntity(saveAttributesRequestVO.getAttributeValueVOList(), null, relation);
    	
		relation.setAttributeValueList(attributeValueList);
		relationRepository.save(relation);
    }

    private void deleteExistingInputAttributes(List<AttributeValue> attributeValueList) {
    	DomainValueFlags domainValueFlags;
    	
    	domainValueFlags = new DomainValueFlags();
    	for(AttributeValue attributeValue : attributeValueList) {
    		domainValueFlags.setDomainValue(attributeValue.getAttribute());
    		if (domainValueFlags.isInputAsAttribute()) {
    			attributeValueRepository.delete(attributeValue);
    		}
    	}
    }
    
    private List<AttributeValue> attributeValueVOToEntity(List<AttributeValueVO> attributeValueVOList, Person person, Relation relation) {
    	List<AttributeValue> attributeValueList;
    	AttributeValue attributeValue;
    	DomainValue attributeDv;
    	Person creator;
    	
    	creator = personRepository.findById(6L)
				.orElseThrow(() -> new AppException("Invalid Person Id " + 6L, null));  // TODO: After integration with login, this should be user's person id
    	attributeValueList = new ArrayList<AttributeValue>();
    	for(AttributeValueVO attributeValueVO : attributeValueVOList) {
    		attributeValue = new AttributeValue();
    		attributeValueList.add(attributeValue);
    		
    		attributeDv = domainValueRepository.findById(attributeValueVO.getAttributeDvId())
    				.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + attributeValueVO.getAttributeDvId(), null));
    		attributeValue.setAttribute(attributeDv);
    		attributeValue.setAttributeValue(attributeValueVO.getAttributeValue());
    		attributeValue.setPerson(person);
    		attributeValue.setRelation(relation);
    		attributeValue.setValueAccurate(attributeValueVO.isValueAccurate());
    		attributeValue.setStartDate(attributeValueVO.getStartDate());
    		attributeValue.setEndDate(attributeValueVO.getEndDate());
    		attributeValue.setCreatorId(creator);
    	}
    	return attributeValueList;
    }
    
    public long searchPerson(List<AttributeValueVO> attributeValueVOList) {
    	StringBuilder querySB;
    	boolean firstTime;
    	List<Person> personList;
    	
    	firstTime = true;
    	querySB = new StringBuilder();
    	querySB.append("SELECT * FROM person p ");
    	for(AttributeValueVO attributeValueVO : attributeValueVOList) {
    		querySB.append((firstTime ? "WHERE " : "AND "));
    		firstTime = false;
    		querySB.append("EXISTS (SELECT 1 FROM attribute_value WHERE person_fk = p.id AND attribute_fk = ");
    		querySB.append(attributeValueVO.getAttributeDvId());
    		querySB.append(" AND LOWER(attribute_value) = '");	// Beware: PostgreSQL specific syntax
    		querySB.append(attributeValueVO.getAttributeValue().toLowerCase());
    		querySB.append("') ");
    	}
    	
    	personList = personRepository.executeDynamicQuery(querySB.toString());
    	return (personList.size() > 0 ? personList.get(0).getId() : Constants.NEW_ENTITY_ID);
    }
}
