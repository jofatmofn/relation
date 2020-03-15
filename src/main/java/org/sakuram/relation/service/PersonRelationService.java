package org.sakuram.relation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
import org.sakuram.relation.valueobject.RetrieveRelationsRequestVO;
import org.sakuram.relation.valueobject.GraphVO;
import org.sakuram.relation.valueobject.SaveAttributesRequestVO;
import org.sakuram.relation.valueobject.RelatedPersonsVO;
import org.sakuram.relation.valueobject.RetrieveAppStartValuesResponseVO;
import org.sakuram.relation.valueobject.RetrieveRelationAttributesResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Autowired
	ServiceParts serviceParts;
	
	@Value("${relation.application.readonly}")
	boolean isAppReadOnly;
	
	public GraphVO retrieveRelations(RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	Person startPerson;
    	List<Relation> participatingRelationList;
    	Set<Person> relatedPersonSet;
    	
    	relatedPersonSet = new HashSet<Person>();
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
    	
    	return serviceParts.buildGraph(relatedPersonSet, startPerson);
    }
	
	public RetrieveAppStartValuesResponseVO retrieveAppStartValues() {
		RetrieveAppStartValuesResponseVO retrieveAppStartValuesResponseVO;
		retrieveAppStartValuesResponseVO = new RetrieveAppStartValuesResponseVO();
		retrieveAppStartValuesResponseVO.setDomainValueVOList(retrieveDomainValues());
		retrieveAppStartValuesResponseVO.setAppReadOnly(isAppReadOnly);
		return retrieveAppStartValuesResponseVO;
	}
	
    private List<DomainValueVO> retrieveDomainValues() {
    	DomainValueVO domainValueVO;
    	List<DomainValueVO> domainValueVOList;
    	List<DomainValue> domainValueList;
    	DomainValueFlags domainValueFlags;
    	
    	domainValueFlags = new DomainValueFlags();
    	domainValueList = domainValueRepository.findAllByOrderByCategoryAscValueAsc();
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
    
    public List<AttributeValueVO> retrievePersonAttributes(long entityId) {
    	Person person;
    	List<AttributeValue> attributeValueList;
    	
		person = personRepository.findById(entityId)
				.orElseThrow(() -> new AppException("Invalid Person " + entityId, null));
		attributeValueList = person.getAttributeValueList();
		
		return attributeValuesEntityToVo(attributeValueList);
    }
    
    public RetrieveRelationAttributesResponseVO retrieveRelationAttributes(long entityId) {
    	Relation relation;
    	List<AttributeValue> attributeValueList;
    	RetrieveRelationAttributesResponseVO retrieveRelationAttributesResponseVO;
    	
		relation = relationRepository.findById(entityId)
				.orElseThrow(() -> new AppException("Invalid Relation " + entityId, null));
		attributeValueList = relation.getAttributeValueList();
		
    	retrieveRelationAttributesResponseVO = new RetrieveRelationAttributesResponseVO();
    	for(AttributeValue attributeValue : relation.getPerson1().getAttributeValueList()) {
    		if (attributeValue.getAttribute().getId() == Constants.PERSON_ATTRIBUTE_DV_ID_GENDER && isCurrentValidAttributeValue(attributeValue)) {
    	    	retrieveRelationAttributesResponseVO.setPerson1GenderDVId(Long.valueOf(attributeValue.getAttributeValue()));
    	    	break;
    		}
    	}
    	for(AttributeValue attributeValue : relation.getPerson2().getAttributeValueList()) {
    		if (attributeValue.getAttribute().getId() == Constants.PERSON_ATTRIBUTE_DV_ID_GENDER && isCurrentValidAttributeValue(attributeValue)) {
    	    	retrieveRelationAttributesResponseVO.setPerson2GenderDVId(Long.valueOf(attributeValue.getAttributeValue()));
    	    	break;
    		}
    	}
    	retrieveRelationAttributesResponseVO.setAttributeValueVOList(attributeValuesEntityToVo(attributeValueList));
    	return retrieveRelationAttributesResponseVO;
    }
    
    private List<AttributeValueVO> attributeValuesEntityToVo(List<AttributeValue> attributeValueList) {
    	List<AttributeValueVO> attributeValueVOList;
    	AttributeValueVO attributeValueVO;
    	DomainValueFlags domainValueFlags;
    	
    	domainValueFlags = new DomainValueFlags();
    	
    	attributeValueVOList = new ArrayList<AttributeValueVO>();
    	for(AttributeValue attributeValue : attributeValueList) {
    		
    		domainValueFlags.setDomainValue(attributeValue.getAttribute());
    		if (isCurrentValidAttributeValue(attributeValue) && domainValueFlags.isInputAsAttribute()) {
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
    
    private boolean isCurrentValidAttributeValue(AttributeValue attributeValue) {
		if ((attributeValue.getStartDate() == null || attributeValue.getStartDate().toLocalDate().isBefore(LocalDate.now())) &&
				(attributeValue.getEndDate() == null || attributeValue.getEndDate().toLocalDate().isAfter(LocalDate.now())) &&
				attributeValue.getOverwrittenBy() == null) {
			return true;
		}
		else {
			return false;
		}
    }
    
    public long savePersonAttributes(SaveAttributesRequestVO saveAttributesRequestVO) {
    	Person person, creator;
    	
    	creator = personRepository.findById(saveAttributesRequestVO.getCreatorId())
				.orElseThrow(() -> new AppException("Invalid Person Id " + saveAttributesRequestVO.getCreatorId(), null));
    	
    	if (saveAttributesRequestVO.getEntityId() == Constants.NEW_ENTITY_ID) {
    		person = new Person();
    		person.setCreator(creator);
    		person = personRepository.save(person);
    	}
    	else {
    		person = personRepository.findById(saveAttributesRequestVO.getEntityId())
    				.orElseThrow(() -> new AppException("Invalid Person " + saveAttributesRequestVO.getEntityId(), null));
    	}
    	
    	saveAttributeValue(saveAttributesRequestVO.getAttributeValueVOList(), person, null, creator);
    	
		return person.getId();
    }
    
    public void saveRelationAttributes(SaveAttributesRequestVO saveAttributesRequestVO) {
    	Relation relation = null;
    	Person creator;
    	
    	creator = personRepository.findById(saveAttributesRequestVO.getCreatorId())
				.orElseThrow(() -> new AppException("Invalid Person Id " + saveAttributesRequestVO.getCreatorId(), null));
    	
		relation = relationRepository.findById(saveAttributesRequestVO.getEntityId())
				.orElseThrow(() -> new AppException("Invalid Relation " + saveAttributesRequestVO.getEntityId(), null));
    	
    	saveAttributeValue(saveAttributesRequestVO.getAttributeValueVOList(), null, relation, creator);
    }

    private void saveAttributeValue(List<AttributeValueVO> attributeValueVOList, Person person, Relation relation, Person creator) {
    	AttributeValue attributeValue, insertedAttributeValue, deletedAttributeValue;
    	List<Long> incomingAttributeValueWithIdList, insertedAttributeValueIdList;
    	List<AttributeValue> toDeleteAttributeValueList;
    	DomainValueFlags domainValueFlags;
    	
    	incomingAttributeValueWithIdList = new ArrayList<Long>();
    	insertedAttributeValueIdList = new ArrayList<Long>();
    	for(AttributeValueVO attributeValueVO : attributeValueVOList) {
    		if (attributeValueVO.getId() == null) {
    			insertedAttributeValue = insertAttributeValue(attributeValueVO, person, relation, creator);
    			insertedAttributeValueIdList.add(insertedAttributeValue.getId());
    		}
    		else {
    			incomingAttributeValueWithIdList.add(attributeValueVO.getId());
    			attributeValue = attributeValueRepository.findById(attributeValueVO.getId())
    					.orElseThrow(() -> new AppException("Invalid Attribute Value Id " + attributeValueVO.getId(), null));
    			if (attributeValueVO.getAttributeDvId() != attributeValue.getAttribute().getId()) {
    				throw new AppException("Invalid input from client.", null);
    			}
    			if (!Objects.equals(attributeValueVO.getAttributeValue(), attributeValue.getAttributeValue()) ||
    					!Objects.equals(attributeValueVO.isValueAccurate(), attributeValue.isValueAccurate()) ||
    					!Objects.equals(attributeValueVO.getStartDate(), attributeValue.getStartDate()) || 
    					!Objects.equals(attributeValueVO.getEndDate(), attributeValue.getEndDate())) {
    				insertedAttributeValue = insertAttributeValue(attributeValueVO, person, relation, creator);
        			insertedAttributeValueIdList.add(insertedAttributeValue.getId());
    				attributeValue.setOverwrittenBy(insertedAttributeValue);
    				attributeValueRepository.save(attributeValue);
    			}
    		}
    	}
    	
    	domainValueFlags = new DomainValueFlags();
		deletedAttributeValue = attributeValueRepository.findById(Constants.DELETED_ATTRIBUTE_VALUE_ID)
				.orElseThrow(() -> new AppException("Invalid Attribute Value Id " + Constants.DELETED_ATTRIBUTE_VALUE_ID, null));
		toDeleteAttributeValueList = (person != null ? person.getAttributeValueList() : relation.getAttributeValueList());
		if (toDeleteAttributeValueList != null) {
	    	for(AttributeValue toDeleteAttributeValue : toDeleteAttributeValueList) {
	    		domainValueFlags.setDomainValue(toDeleteAttributeValue.getAttribute());
	    		if (domainValueFlags.isInputAsAttribute() && !incomingAttributeValueWithIdList.contains(toDeleteAttributeValue.getId()) && toDeleteAttributeValue.getOverwrittenBy() == null) {
	    			toDeleteAttributeValue.setOverwrittenBy(deletedAttributeValue);
					attributeValueRepository.save(toDeleteAttributeValue);
	    		}
	    	}
		}
    }

    private AttributeValue insertAttributeValue(AttributeValueVO attributeValueVO, Person person, Relation relation, Person creator) {
    	AttributeValue attributeValue;
    	DomainValue attributeDv;
    	
		attributeValue = new AttributeValue();
		attributeDv = domainValueRepository.findById(attributeValueVO.getAttributeDvId())
				.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + attributeValueVO.getAttributeDvId(), null));
		attributeValue.setAttribute(attributeDv);
		attributeValue.setAttributeValue(attributeValueVO.getAttributeValue());
		attributeValue.setPerson(person);
		attributeValue.setRelation(relation);
		attributeValue.setValueAccurate(attributeValueVO.isValueAccurate());
		attributeValue.setStartDate(attributeValueVO.getStartDate());
		attributeValue.setEndDate(attributeValueVO.getEndDate());
		attributeValue.setCreator(creator);
		return attributeValueRepository.save(attributeValue);
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
    		querySB.append(" AND LOWER(attribute_value) LIKE '%");	// Beware: PostgreSQL specific syntax
    		querySB.append(attributeValueVO.getAttributeValue().toLowerCase());
    		querySB.append("%') ");
    	}
    	
    	personList = personRepository.executeDynamicQuery(querySB.toString());
    	return (personList.size() > 0 ? personList.get(0).getId() : Constants.NEW_ENTITY_ID);
    }
    
    public long saveRelation(RelatedPersonsVO saveRelationRequestVO) {
    	Person person1, person2, creator;
    	Relation relation;
    	HashSet<Person> personSet;
    	
    	person1 = personRepository.findById(saveRelationRequestVO.getPerson1Id())
				.orElseThrow(() -> new AppException("Invalid Person Id " + saveRelationRequestVO.getPerson1Id(), null));
    	person2 = personRepository.findById(saveRelationRequestVO.getPerson2Id())
				.orElseThrow(() -> new AppException("Invalid Person Id " + saveRelationRequestVO.getPerson2Id(), null));
    	creator = personRepository.findById(saveRelationRequestVO.getCreatorId())
				.orElseThrow(() -> new AppException("Invalid Person Id " + saveRelationRequestVO.getCreatorId(), null));
    	// TODO: To check reverse also; Create a new repository method
    	personSet = new HashSet<Person>(Arrays.asList(new Person[]{person1, person2}));
    	if (relationRepository.findByPerson1InAndPerson2In(personSet, personSet).size() > 0) {
    		throw new AppException(saveRelationRequestVO.getPerson1Id() + " and " + saveRelationRequestVO.getPerson2Id() + " are already related.", null);
    	}
    	relation = new Relation();
    	relation.setPerson1(person1);
    	relation.setPerson2(person2);
    	relation.setCreator(creator);
    	relation = relationRepository.save(relation);
    	return relation.getId();
    }
}
