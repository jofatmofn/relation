package org.sakuram.relation.service;

import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.sakuram.relation.bean.AppUser;
import org.sakuram.relation.bean.AttributeValue;
import org.sakuram.relation.bean.DomainValue;
import org.sakuram.relation.bean.Person;
import org.sakuram.relation.bean.Relation;
import org.sakuram.relation.repository.AppUserRepository;
import org.sakuram.relation.repository.AttributeValueRepository;
import org.sakuram.relation.repository.DomainValueRepository;
import org.sakuram.relation.repository.PersonRepository;
import org.sakuram.relation.repository.RelationRepository;
import org.sakuram.relation.service.ServiceParts.RelatedPerson1VO;
import org.sakuram.relation.util.AppException;
import org.sakuram.relation.util.Constants;
import org.sakuram.relation.util.DomainValueFlags;
import org.sakuram.relation.util.SecurityContext;
import org.sakuram.relation.util.UtilFuncs;
import org.sakuram.relation.valueobject.AttributeValueVO;
import org.sakuram.relation.valueobject.DomainValueVO;
import org.sakuram.relation.valueobject.RetrieveRelationsRequestVO;
import org.sakuram.relation.valueobject.GraphVO;
import org.sakuram.relation.valueobject.PersonVO;
import org.sakuram.relation.valueobject.SaveAttributesRequestVO;
import org.sakuram.relation.valueobject.SaveAttributesResponseVO;
import org.sakuram.relation.valueobject.SearchResultsVO;
import org.sakuram.relation.valueobject.RelatedPersonsVO;
import org.sakuram.relation.valueobject.RelationVO;
import org.sakuram.relation.valueobject.RetrieveAppStartValuesResponseVO;
import org.sakuram.relation.valueobject.RetrieveRelationAttributesResponseVO;
import org.sakuram.relation.valueobject.RetrieveRelationsBetweenRequestVO;
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
	@Autowired
	AppUserRepository appUserRepository;
	
	@Autowired
	ServiceParts serviceParts;
	
	public GraphVO retrieveRelations(RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	Person startPerson;
    	List<Relation> participatingRelationList;
    	Set<Person> relatedPersonSet;
    	int ind;
    	List<RelatedPerson3VO> fatherRelatedPerson3VOList, motherRelatedPerson3VOList, spouseRelatedPerson3VOList, childRelatedPerson3VOList;
    	
    	GraphVO retrieveRelationsResponseVO;
    	Map<Long, PersonVO> personVOMap;
    	List<Relation> relationList;
    	List<RelationVO> relationVOList;
    	RelatedPerson1VO relatedPerson1VO;
    	PersonVO personVO;
    	    	
    	relatedPersonSet = new HashSet<Person>();
    	startPerson = personRepository.findById(retrieveRelationsRequestVO.getStartPersonId())
				.orElseThrow(() -> new AppException("Invalid Person Id " + retrieveRelationsRequestVO.getStartPersonId(), null));
		relatedPersonSet.add(startPerson);
    	participatingRelationList = relationRepository.findByPerson1(startPerson);
    	for (Relation relation : participatingRelationList) {
    		relatedPersonSet.add(relation.getPerson2());
    	}
    	participatingRelationList = relationRepository.findByPerson2(startPerson);
    	for (Relation relation : participatingRelationList) {
    		relatedPersonSet.add(relation.getPerson1());
    	}
    	
    	retrieveRelationsResponseVO = new GraphVO();
    	personVOMap = new HashMap<Long, PersonVO>();
    	relationVOList = new ArrayList<RelationVO>();
    	retrieveRelationsResponseVO.setEdges(relationVOList);
    	relationList = new ArrayList<Relation>();
    	
    	for (Person person : relatedPersonSet) {
    		personVO = serviceParts.addToPersonVOMap(personVOMap, person);
    		if (person.equals(startPerson)) {
        		personVO.setX(10);
        		personVO.setY(60);
    		}
    	}
    	
    	fatherRelatedPerson3VOList = new ArrayList<RelatedPerson3VO>();
    	motherRelatedPerson3VOList = new ArrayList<RelatedPerson3VO>();
    	spouseRelatedPerson3VOList = new ArrayList<RelatedPerson3VO>();
    	childRelatedPerson3VOList = new ArrayList<RelatedPerson3VO>();
    	relationList = relationRepository.findByPerson1InAndPerson2In(relatedPersonSet, relatedPersonSet);
    	for (Relation relation : relationList) {
    		relatedPerson1VO = serviceParts.addToRelationVOList(relationVOList, relation, startPerson, false);
    		if (relatedPerson1VO.person != null) {	// Ignore Husband-Wife relation between parents
	    		personVO = personVOMap.get(relatedPerson1VO.person.getId());
	    		
	    		if (relatedPerson1VO.relationDvId == null) {
	        		personVO.setX(Math.random() * 100);
	        		personVO.setY(Math.random() * 100);
	    		}
	    		else {
					switch(relatedPerson1VO.relationDvId) {
					case Constants.RELATION_NAME_FATHER:
						fatherRelatedPerson3VOList.add(new RelatedPerson3VO(relatedPerson1VO.person.getId(), relatedPerson1VO.seqNo));
						personVO.setX(25);
			    		break;
					case Constants.RELATION_NAME_MOTHER:
						motherRelatedPerson3VOList.add(new RelatedPerson3VO(relatedPerson1VO.person.getId(), relatedPerson1VO.seqNo));
						personVO.setX(85);
			    		break;
					case Constants.RELATION_NAME_HUSBAND:
					case Constants.RELATION_NAME_WIFE:
						spouseRelatedPerson3VOList.add(new RelatedPerson3VO(relatedPerson1VO.person.getId(), relatedPerson1VO.seqNo));
						personVO.setX(100);
			    		break;
					case Constants.RELATION_NAME_SON:
					case Constants.RELATION_NAME_DAUGHTER:
						childRelatedPerson3VOList.add(new RelatedPerson3VO(relatedPerson1VO.person.getId(), relatedPerson1VO.seqNo));
				    	personVO.setY(120);
			    		break;
					}
	    		}
    		}
    	}

    	Collections.sort(fatherRelatedPerson3VOList);
    	ind = 0;
    	for (RelatedPerson3VO relatedPerson3VO : fatherRelatedPerson3VOList) {
    		ind++;
    		personVOMap.get(relatedPerson3VO.personId).setY(ind * 10);
    	}
    	Collections.sort(motherRelatedPerson3VOList);
    	ind = 0;
    	for (RelatedPerson3VO relatedPerson3VO : motherRelatedPerson3VOList) {
    		ind++;
    		personVOMap.get(relatedPerson3VO.personId).setY(ind * 10);
    	}
    	Collections.sort(spouseRelatedPerson3VOList);
    	ind = -1;
    	for (RelatedPerson3VO relatedPerson3VO : spouseRelatedPerson3VOList) {
    		ind++;
    		personVOMap.get(relatedPerson3VO.personId).setY(60 + ind * 10);
    	}
    	Collections.sort(childRelatedPerson3VOList);
    	ind = 0;
    	for (RelatedPerson3VO relatedPerson3VO : childRelatedPerson3VOList) {
    		ind++;
    		personVOMap.get(relatedPerson3VO.personId).setX(ind * 10);
    	}
    	
    	retrieveRelationsResponseVO.setNodes(new ArrayList<PersonVO>(personVOMap.values()));
    	return retrieveRelationsResponseVO;
    }
	
	public List<RelationVO> retrieveRelationsBetween(RetrieveRelationsBetweenRequestVO retrieveRelationsBetweenRequestVO) {
    	Person end1Person;
    	List<Relation> relationList;
    	List<RelationVO> relationVOList;
    	
    	end1Person = personRepository.findById(retrieveRelationsBetweenRequestVO.getEnd1PersonId())
				.orElseThrow(() -> new AppException("Invalid Person Id " + retrieveRelationsBetweenRequestVO.getEnd1PersonId(), null));
    	relationList = relationRepository.findByPerson1(end1Person);
    	relationList.addAll(relationRepository.findByPerson2(end1Person));
    	
    	relationVOList = new ArrayList<RelationVO>();
    	for (Relation relation : relationList) {
    		if (relation.getPerson1().getId() == retrieveRelationsBetweenRequestVO.getEnd1PersonId() && retrieveRelationsBetweenRequestVO.getEnd2PersonIdsList().contains(relation.getPerson2().getId()) ||
    				relation.getPerson2().getId() == retrieveRelationsBetweenRequestVO.getEnd1PersonId() && retrieveRelationsBetweenRequestVO.getEnd2PersonIdsList().contains(relation.getPerson1().getId())) {
    			serviceParts.addToRelationVOList(relationVOList, relation, null, false);
    		}
    	}
    	return relationVOList;
    }
	
	public GraphVO retrieveTree(RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	Person startPerson, currentPerson;
    	Set<Long> relatedPersonIdSet, relatedRelationIdSet;
    	List<RelatedPerson2VO> relatedPerson2VOList;
    	RelatedPerson2VO relatedPerson2VO;
    	int level, sequence, readInd, currentLevel, ind;
    	double lastY;
    	List<Integer> seqAtLevel;
    	List<String> requiredRelationsList;
    	List<Person> excludeSpouseList;
    	
    	GraphVO retrieveRelationsResponseVO;
    	Map<Long, PersonVO> personVOMap;
    	PersonVO relatedPersonVO, currentPersonVO;
    	List<PersonVO> personVOList;
    	List<RelationVO> relationVOList;
    	
    	retrieveRelationsResponseVO = new GraphVO();
    	personVOMap = new HashMap<Long, PersonVO>();
    	relationVOList = new ArrayList<RelationVO>();
    	retrieveRelationsResponseVO.setEdges(relationVOList);
    	relatedPersonIdSet = new HashSet<Long>();
    	relatedRelationIdSet = new HashSet<Long>();
    	excludeSpouseList = new ArrayList<Person>();
		relatedPerson2VOList = new ArrayList<RelatedPerson2VO>();
    	startPerson = personRepository.findById(retrieveRelationsRequestVO.getStartPersonId())
				.orElseThrow(() -> new AppException("Invalid Person Id " + retrieveRelationsRequestVO.getStartPersonId(), null));
		relatedPersonIdSet.add(startPerson.getId());
		currentPersonVO = serviceParts.addToPersonVOMap(personVOMap, startPerson);
		currentPersonVO.setX(1);
		currentPersonVO.setY(1);
		
		requiredRelationsList = Objects.equals(retrieveRelationsRequestVO.getIsSonOnly(), true) ? Arrays.asList(Constants.RELATION_NAME_SON) : Arrays.asList(Constants.RELATION_NAME_HUSBAND, Constants.RELATION_NAME_WIFE, Constants.RELATION_NAME_SON, Constants.RELATION_NAME_DAUGHTER);
		relatedPerson2VO =  new RelatedPerson2VO();
		relatedPerson2VOList.add(relatedPerson2VO);
		relatedPerson2VO.person = startPerson;
		relatedPerson2VO.level = 0;
		seqAtLevel = new ArrayList<Integer>();
		seqAtLevel.add(0);
		readInd = 0;
		while (true) {
			currentPerson = relatedPerson2VOList.get(readInd).person;
			currentLevel = relatedPerson2VOList.get(readInd).level;
			LogManager.getLogger().debug("Current person: " + currentPerson.getId() + " at level " + currentLevel);
			if (currentLevel == retrieveRelationsRequestVO.getMaxDepth()) {
				break;
			}
			if (currentLevel == 0 && !currentPerson.equals(startPerson)) {
				excludeSpouseList.add(currentPerson);
			}
			else {
			currentPersonVO = personVOMap.get(currentPerson.getId());
	    	for (RelatedPerson1VO relatedPerson1VO : retrieveRelatives(currentPerson, requiredRelationsList)) {
				if (relatedPerson1VO.relationDvId.equals(Constants.RELATION_NAME_HUSBAND) ||
						relatedPerson1VO.relationDvId.equals(Constants.RELATION_NAME_WIFE) ||
						currentLevel < retrieveRelationsRequestVO.getMaxDepth() - 1) {
				if (relatedPersonIdSet.add(relatedPerson1VO.person.getId())) {
					relatedPerson2VO =  new RelatedPerson2VO();
					relatedPerson2VOList.add(relatedPerson2VO);
					relatedPerson2VO.person = relatedPerson1VO.person;
		    		relatedPersonVO = serviceParts.addToPersonVOMap(personVOMap, relatedPerson1VO.person);
		    		if (relatedPerson1VO.relationDvId.equals(Constants.RELATION_NAME_HUSBAND) || relatedPerson1VO.relationDvId.equals(Constants.RELATION_NAME_WIFE)) {
		    			relatedPerson2VO.level = currentLevel;
						relatedPersonVO.setX(currentPersonVO.getX() + 10);
						relatedPersonVO.setY(currentPersonVO.getY());
		    		}
		    		else if (currentLevel < retrieveRelationsRequestVO.getMaxDepth() - 1) {
		    			level = currentLevel + 1;
		    			relatedPerson2VO.level = level;
		    			if (seqAtLevel.size() > level) {
		    				sequence = seqAtLevel.get(level);
			    			sequence++;
		    			}
		    			else {
		    				seqAtLevel.add(0);
		    				sequence = 0;
		    			}
		    			seqAtLevel.set(level, sequence);
						relatedPersonVO.setX(sequence * 20 + 1);
						relatedPersonVO.setY(level * 10 + 1);
		    		}
		    		LogManager.getLogger().debug("Added person: " + relatedPerson2VO.person.getId() + " at level " + relatedPerson2VO.level);

				}
				else LogManager.getLogger().debug("Skipped (due to duplicate) person: " + relatedPerson1VO.person.getId());
				if (relatedRelationIdSet.add(relatedPerson1VO.relation.getId())) {
					serviceParts.addToRelationVOList(relationVOList, relatedPerson1VO.relation, currentPerson, false);
				}
				}
				else LogManager.getLogger().debug("Skipped (due to higher depth) person: " + relatedPerson1VO.person.getId());
			}
			}
	    	readInd++;
	    	if (readInd == relatedPerson2VOList.size()) {
	    		break;
	    	}
		}
		
		for (Person spouse : excludeSpouseList) {
	    	for (RelatedPerson1VO relatedPerson1VO : retrieveRelatives(spouse, requiredRelationsList)) {
				if (relatedPersonIdSet.contains(relatedPerson1VO.person.getId()) && relatedRelationIdSet.add(relatedPerson1VO.relation.getId())) {
					serviceParts.addToRelationVOList(relationVOList, relatedPerson1VO.relation, spouse, false);
				}
	    	}
		}
    	
		/* Compact unutilised space reserved for spouse */
		/* TODO: There should be a better (performance) way of doing this */
		personVOList = new ArrayList<PersonVO>(personVOMap.values());
		Collections.sort(personVOList);
		lastY = -1;
		sequence = 0;
		for (ind = 0; ind < personVOList.size(); ind++) {
			currentPersonVO = personVOList.get(ind);
			if (currentPersonVO.getY() == lastY) {
				sequence++;
			}
			else {
				lastY = currentPersonVO.getY();
				sequence = 0;
			}
			currentPersonVO.setX(sequence * 20 + 1);
		}
    	retrieveRelationsResponseVO.setNodes(personVOList);
    	return retrieveRelationsResponseVO;
    }
	
	public GraphVO retrieveParceners(RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
		Person startPerson;
		List<RelatedPerson1VO> relatedPerson1VOList;
		RetrieveRelationsRequestVO retrieveRelationsRequestVO2;
		short depth;
		
    	startPerson = personRepository.findById(retrieveRelationsRequestVO.getStartPersonId())
				.orElseThrow(() -> new AppException("Invalid Person Id " + retrieveRelationsRequestVO.getStartPersonId(), null));
    	depth = 1;
    	while((relatedPerson1VOList = retrieveRelatives(startPerson, Arrays.asList(Constants.RELATION_NAME_FATHER))).size() == 1) {
    		startPerson = relatedPerson1VOList.get(0).person;
    		depth++;
    	}
    	retrieveRelationsRequestVO2 = new RetrieveRelationsRequestVO();
    	retrieveRelationsRequestVO2.setStartPersonId(startPerson.getId());
    	retrieveRelationsRequestVO2.setMaxDepth(depth);
    	retrieveRelationsRequestVO2.setIsSonOnly(true);
		return retrieveTree(retrieveRelationsRequestVO2);
	}
	
	public RetrieveAppStartValuesResponseVO retrieveAppStartValues() {
		RetrieveAppStartValuesResponseVO retrieveAppStartValuesResponseVO;
		retrieveAppStartValuesResponseVO = new RetrieveAppStartValuesResponseVO();
		retrieveAppStartValuesResponseVO.setDomainValueVOList(retrieveDomainValues());
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
				.orElseThrow(() -> new AppException("Invalid Person Id " + entityId, null));
		attributeValueList = person.getAttributeValueList();
		
		return attributeValuesEntityToVo(attributeValueList);
    }
    
    public RetrieveRelationAttributesResponseVO retrieveRelationAttributes(long entityId) {
    	Relation relation;
    	List<AttributeValue> attributeValueList;
    	RetrieveRelationAttributesResponseVO retrieveRelationAttributesResponseVO;
    	
		relation = relationRepository.findById(entityId)
				.orElseThrow(() -> new AppException("Invalid Relation Id " + entityId, null));
		attributeValueList = relation.getAttributeValueList();
		
    	retrieveRelationAttributesResponseVO = new RetrieveRelationAttributesResponseVO();
    	for(AttributeValue attributeValue : relation.getPerson1().getAttributeValueList()) {
    		if (attributeValue.getAttribute().getId() == Constants.PERSON_ATTRIBUTE_DV_ID_GENDER && serviceParts.isCurrentValidAttributeValue(attributeValue)) {
    	    	retrieveRelationAttributesResponseVO.setPerson1GenderDVId(Long.valueOf(attributeValue.getAttributeValue()));
    	    	break;
    		}
    	}
    	for(AttributeValue attributeValue : relation.getPerson2().getAttributeValueList()) {
    		if (attributeValue.getAttribute().getId() == Constants.PERSON_ATTRIBUTE_DV_ID_GENDER && serviceParts.isCurrentValidAttributeValue(attributeValue)) {
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
    		if (domainValueFlags.isInputAsAttribute()) {
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
    
    public SaveAttributesResponseVO savePersonAttributes(SaveAttributesRequestVO saveAttributesRequestVO) {
    	Person person;
    	AppUser appUser;
    	SaveAttributesResponseVO saveAttributesResponseVO;
    	
    	appUser = appUserRepository.findById(SecurityContext.getCurrentUser())
    			.orElseThrow(() -> new AppException("Invalid User Id " + SecurityContext.getCurrentUser(), null));
    	
    	if (saveAttributesRequestVO.getEntityId() == Constants.NEW_ENTITY_ID) {
    		person = new Person();
    		person.setCreator(appUser);
    		person = personRepository.save(person);
    	}
    	else {
    		person = personRepository.findById(saveAttributesRequestVO.getEntityId())
    				.orElseThrow(() -> new AppException("Invalid Person Id " + saveAttributesRequestVO.getEntityId(), null));
    	}
    	
		saveAttributesResponseVO = new SaveAttributesResponseVO();
		saveAttributesResponseVO.setEntityId(person.getId());
		saveAttributesResponseVO.setInsertedAttributeValueIdList(saveAttributeValue(saveAttributesRequestVO.getAttributeValueVOList(), person, null, appUser));
    	return saveAttributesResponseVO;
    }
    
    public SaveAttributesResponseVO saveRelationAttributes(SaveAttributesRequestVO saveAttributesRequestVO) {
    	Relation relation = null;
    	AppUser appUser;
    	SaveAttributesResponseVO saveAttributesResponseVO;
    	
    	appUser = appUserRepository.findById(SecurityContext.getCurrentUser())
    			.orElseThrow(() -> new AppException("Invalid User Id " + SecurityContext.getCurrentUser(), null));
    	
		relation = relationRepository.findById(saveAttributesRequestVO.getEntityId())
				.orElseThrow(() -> new AppException("Invalid Relation Id " + saveAttributesRequestVO.getEntityId(), null));
    	
		saveAttributesResponseVO = new SaveAttributesResponseVO();
		saveAttributesResponseVO.setEntityId(saveAttributesRequestVO.getEntityId());
		saveAttributesResponseVO.setInsertedAttributeValueIdList(saveAttributeValue(saveAttributesRequestVO.getAttributeValueVOList(), null, relation, appUser));
    	return saveAttributesResponseVO;
    }

    private List<Long> saveAttributeValue(List<AttributeValueVO> attributeValueVOList, Person person, Relation relation, AppUser appUser) {
    	AttributeValue attributeValue, insertedAttributeValue;
    	List<Long> incomingAttributeValueWithIdList, insertedAttributeValueIdList;
    	List<AttributeValue> toDeleteAttributeValueList;
    	DomainValueFlags domainValueFlags;
    	
    	incomingAttributeValueWithIdList = new ArrayList<Long>();
    	insertedAttributeValueIdList = new ArrayList<Long>();
    	for(AttributeValueVO attributeValueVO : attributeValueVOList) {
    		if (attributeValueVO.getId() == null) {
    			throw new AppException("System error: Attribute with null id", null);
    		}
    		else if (attributeValueVO.getId() < 1) {
    			insertedAttributeValue = insertAttributeValue(attributeValueVO, person, relation, appUser);
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
    					!UtilFuncs.dateEquals(attributeValueVO.getStartDate(), attributeValue.getStartDate()) ||
    					!UtilFuncs.dateEquals(attributeValueVO.getEndDate(), attributeValue.getEndDate())) {
    				insertedAttributeValue = insertAttributeValue(attributeValueVO, person, relation, appUser);
    				attributeValue.setOverwrittenBy(insertedAttributeValue);
    				attributeValueRepository.save(attributeValue);
    			}
    		}
    	}
    	
    	domainValueFlags = new DomainValueFlags();
		toDeleteAttributeValueList = (person != null ? person.getAttributeValueList() : relation.getAttributeValueList());
		if (toDeleteAttributeValueList != null) {
	    	for(AttributeValue toDeleteAttributeValue : toDeleteAttributeValueList) {
	    		domainValueFlags.setDomainValue(toDeleteAttributeValue.getAttribute());
	    		if (domainValueFlags.isInputAsAttribute() && !incomingAttributeValueWithIdList.contains(toDeleteAttributeValue.getId())) {
	    			toDeleteAttributeValue.setDeleter(appUser);
	    			toDeleteAttributeValue.setDeletedAt(new Timestamp(System.currentTimeMillis()));
					attributeValueRepository.save(toDeleteAttributeValue);
	    		}
	    	}
		}
		
		return insertedAttributeValueIdList;
    }

    private AttributeValue insertAttributeValue(AttributeValueVO attributeValueVO, Person person, Relation relation, AppUser creator) {
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
    
    public SearchResultsVO searchPerson(List<AttributeValueVO> attributeValueVOList) {
    	StringBuilder querySB;
    	List<Person> personList;
    	long searchResultsCount;
    	int searchResultAttributesListSize;
    	SearchResultsVO searchResultsVO;
    	Map<Long, Integer> attributeVsColumnMap;
    	List<List<String>> searchResultsList, searchResultsPostXtraFilterList;
    	List<String> personAttributesList;
    	DomainValueFlags domainValueFlags;
    	String attrVal, parentNamesSsv, spouseNamesSsv, parentsCriteria, spousesCriteria;
    	DomainValue attributeDv;
    	Long tenantId;
    	
    	parentsCriteria = null;
    	spousesCriteria = null;
    	querySB = new StringBuilder();
    	querySB.append("SELECT * FROM person p LEFT OUTER JOIN tenant t ON p.tenant_fk = t.id WHERE p.overwritten_by_fk IS NULL AND p.deleter_fk IS NULL");
    	tenantId = SecurityContext.getCurrentTenant();
    	if (tenantId != null) {
    		querySB.append(" AND p.tenant_fk = ");
    		querySB.append(tenantId);
    	}
    	for(AttributeValueVO attributeValueVO : attributeValueVOList) {
    		if (attributeValueVO.getAttributeDvId() > 0) {
	    		querySB.append(" AND ");
	    		querySB.append("EXISTS (SELECT 1 FROM attribute_value av WHERE av.overwritten_by_fk IS NULL AND av.deleter_fk IS NULL AND av.person_fk = p.id AND av.attribute_fk = ");
	    		querySB.append(attributeValueVO.getAttributeDvId());
	    		querySB.append(" AND LOWER(av.attribute_value) LIKE '%");	// Beware: PostgreSQL specific syntax
	    		querySB.append(attributeValueVO.getAttributeValue().toLowerCase());
	    		querySB.append("%')");
    		}
    		else if (attributeValueVO.getAttributeDvId() == -1) {
	    		querySB.append(" AND p.id = ");
	    		querySB.append(attributeValueVO.getAttributeValue());
    		}
    		else if (attributeValueVO.getAttributeDvId() == -2) {
    			parentsCriteria = attributeValueVO.getAttributeValue().toLowerCase();
    		}
    		else if (attributeValueVO.getAttributeDvId() == -3) {
    			spousesCriteria = attributeValueVO.getAttributeValue().toLowerCase();
    		}
    	}
		querySB.append(" ORDER BY p.id;");
    	
    	personList = personRepository.executeDynamicQuery(querySB.toString());
    	
    	searchResultsVO = new SearchResultsVO();
    	if (personList.size() == 0) {
    		return searchResultsVO;
    	}
    	
    	domainValueFlags = new DomainValueFlags();
    	searchResultAttributesListSize = 0;
    	attributeVsColumnMap = new HashMap<Long, Integer>();
    	searchResultsList = new ArrayList<List<String>>(personList.size());
    	personAttributesList = new ArrayList<String>(); // For Header
    	searchResultsList.add(personAttributesList);
    	personAttributesList.add("Id");
    	for(Person person : personList) {
    		personAttributesList = new ArrayList<String>();
    		searchResultsList.add(personAttributesList);
    		personAttributesList.add(String.valueOf(person.getId()));
    		for (AttributeValue attributeValue : person.getAttributeValueList()) {
    			if(serviceParts.isCurrentValidAttributeValue(attributeValue)) {
    				domainValueFlags.setDomainValue(attributeValue.getAttribute());
    				if (domainValueFlags.getAttributeDomain().equals("")) {
    					attrVal = attributeValue.getAttributeValue();
    				}
    				else {
    					attributeDv = domainValueRepository.findById(Long.valueOf(attributeValue.getAttributeValue()))
    							.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + attributeValue.getAttributeValue(), null));
    					attrVal = attributeDv.getValue();
    				}
	    			if (attributeVsColumnMap.containsKey(attributeValue.getAttribute().getId())) {
	    				UtilFuncs.listSet(personAttributesList, attributeVsColumnMap.get(attributeValue.getAttribute().getId()), attrVal, "");
	    			}
	    			else {
	    				attributeVsColumnMap.put(attributeValue.getAttribute().getId(), attributeVsColumnMap.size() + 1);
	    				UtilFuncs.listSet(searchResultsList.get(0), attributeVsColumnMap.size(), attributeValue.getAttribute().getValue(), "");
	    				UtilFuncs.listSet(personAttributesList, attributeVsColumnMap.size(), attrVal, "");
	    			}
    			}
    		}
    	}
		// Add parents & spouses; Also apply search criteria based on parents & spouses
    	searchResultsPostXtraFilterList = new ArrayList<List<String>>(searchResultsList.size());
		searchResultAttributesListSize = searchResultsList.get(0).size();
		UtilFuncs.listSet(searchResultsList.get(0), searchResultAttributesListSize, "Parents", "");
		UtilFuncs.listSet(searchResultsList.get(0), searchResultAttributesListSize + 1, "Spouses", "");
    	searchResultsCount = 0;
    	for (int ind = 1; ind < searchResultsList.size(); ind++) {
			parentNamesSsv = "";
			spouseNamesSsv = "";
    		for (Map.Entry<Person, AttributeValue> relativeAttributeEntry : retrieveRelativesAndAttributes(personList.get(ind - 1), Arrays.asList(Constants.RELATION_NAME_FATHER, Constants.RELATION_NAME_MOTHER), Arrays.asList(Constants.PERSON_ATTRIBUTE_DV_ID_LABEL))) {
    			parentNamesSsv += "/" + relativeAttributeEntry.getValue().getAttributeValue();
    		}
    		if (parentsCriteria != null && (parentNamesSsv.equals("") || !parentNamesSsv.toLowerCase().contains(parentsCriteria))) {
    			continue;
    		}
    		for (Map.Entry<Person, AttributeValue> relativeAttributeEntry : retrieveRelativesAndAttributes(personList.get(ind - 1), Arrays.asList(Constants.RELATION_NAME_HUSBAND, Constants.RELATION_NAME_WIFE), Arrays.asList(Constants.PERSON_ATTRIBUTE_DV_ID_LABEL))) {
    			spouseNamesSsv += "/" + relativeAttributeEntry.getValue().getAttributeValue();
    		}
    		if (spousesCriteria != null && (spouseNamesSsv.equals("") || !spouseNamesSsv.toLowerCase().contains(spousesCriteria))) {
    			continue;
    		}
			UtilFuncs.listSet(searchResultsList.get(ind), searchResultAttributesListSize, parentNamesSsv, "");
			UtilFuncs.listSet(searchResultsList.get(ind), searchResultAttributesListSize + 1, spouseNamesSsv, "");
			searchResultsPostXtraFilterList.add(searchResultsList.get(ind));
    		searchResultsCount++;
    		if (searchResultsCount == Constants.SEARCH_RESULTS_MAX_COUNT) {
    			if (ind == searchResultsList.size() - 1) {
    		    	searchResultsVO.setMorePresentInDb(false);
    			}
    			else {
    		    	searchResultsVO.setMorePresentInDb(true);
    			}
    			break;
    		}
    	}
    	if (searchResultsCount > 0) {
    		searchResultsPostXtraFilterList.add(0, searchResultsList.get(0));
    		searchResultsVO.setResultsList(searchResultsPostXtraFilterList);
    	}
    	return searchResultsVO;
    }

    private List<Map.Entry<Person, AttributeValue>> retrieveRelativesAndAttributes(Person forPerson, List<String> requiredRelationTypesList, List<Long> requiredAttributeTypesList) {
    	List<Map.Entry<Person, AttributeValue>> personAttributeValueList;
    	
    	personAttributeValueList = new ArrayList<Map.Entry<Person, AttributeValue>>();
    	for (RelatedPerson1VO relatedPerson1VO : retrieveRelatives(forPerson, requiredRelationTypesList)) {
    		for (AttributeValue attributeValue : relatedPerson1VO.person.getAttributeValueList()) {
    			if (requiredAttributeTypesList.contains(attributeValue.getAttribute().getId()) &&
    					serviceParts.isCurrentValidAttributeValue(attributeValue)) {
    				personAttributeValueList.add(new AbstractMap.SimpleEntry<Person, AttributeValue>(relatedPerson1VO.person, attributeValue));
    			}
    		}
    	}
    	return personAttributeValueList;
    }
    
    private List<RelatedPerson1VO> retrieveRelatives(Person forPerson, List<String> requiredRelationTypesList) {
    	List<RelatedPerson1VO> relatedPerson1VOList;
    	List<Relation> relationList;
    	RelatedPerson1VO relatedPerson1VO;
    	
    	relatedPerson1VOList = new ArrayList<RelatedPerson1VO>();
    	relationList = relationRepository.findByPerson1(forPerson);
    	relationList.addAll(relationRepository.findByPerson2(forPerson));
    	for (Relation relation : relationList) {
    		relatedPerson1VO = getOtherPerson(relation, forPerson);
			if (requiredRelationTypesList.contains(relatedPerson1VO.relationDvId)) {
				relatedPerson1VO.relation = relation;
   				relatedPerson1VOList.add(relatedPerson1VO);
    		}
    	}
    	return relatedPerson1VOList;
    }
    
    private RelatedPerson1VO getOtherPerson(Relation relation, Person forPerson) {
    	long reqdAttributeDvId;
    	RelatedPerson1VO relatedPerson1VO;
    	
    	relatedPerson1VO = serviceParts.new RelatedPerson1VO();
    	if (relation.getPerson1().equals(forPerson)) {
    		relatedPerson1VO.person = relation.getPerson2();
    		reqdAttributeDvId = Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1;
    	}
    	else {
    		relatedPerson1VO.person = relation.getPerson1();
    		reqdAttributeDvId = Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2;
    	}
		for (AttributeValue attributeValue : relation.getAttributeValueList()) {
			if (attributeValue.getAttribute().getId() == reqdAttributeDvId &&
					serviceParts.isCurrentValidAttributeValue(attributeValue)) {
				relatedPerson1VO.relationDvId = attributeValue.getAttributeValue();
				break;
			}
		}
		return relatedPerson1VO;
    }
    
    public long saveRelation(RelatedPersonsVO saveRelationRequestVO) {
    	Person person1, person2;
    	AppUser creator;
    	Relation relation;
    	HashSet<Person> personSet;
    	
    	person1 = personRepository.findById(saveRelationRequestVO.getPerson1Id())
				.orElseThrow(() -> new AppException("Invalid Person Id " + saveRelationRequestVO.getPerson1Id(), null));
    	person2 = personRepository.findById(saveRelationRequestVO.getPerson2Id())
				.orElseThrow(() -> new AppException("Invalid Person Id " + saveRelationRequestVO.getPerson2Id(), null));
    	creator = appUserRepository.findById(SecurityContext.getCurrentUser())
    			.orElseThrow(() -> new AppException("Invalid User Id " + SecurityContext.getCurrentUser(), null));
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
    
    public void deleteRelation(long relationId) {
    	Relation relation;
    	AppUser deleter;
    	Timestamp deletedAt;
    	
		relation = relationRepository.findById(relationId)
				.orElseThrow(() -> new AppException("Invalid Relation Id " + relationId, null));
    	
    	deleter = appUserRepository.findById(SecurityContext.getCurrentUser())
				.orElseThrow(() -> new AppException("Invalid User Id " + SecurityContext.getCurrentUser(), null));
    	
    	deletedAt = new Timestamp(System.currentTimeMillis());
		for (AttributeValue attributeValue : relation.getAttributeValueList()) {
			attributeValue.setDeleter(deleter);
			attributeValue.setDeletedAt(deletedAt);
		}
    	relation.setDeleter(deleter);
    	relation.setDeletedAt(deletedAt);
    	relationRepository.save(relation);
    }
        
    public void deletePerson(long personId) {
    	Person person;
    	AppUser deleter;
    	List<Relation> relationList;
    	Timestamp deletedAt;
    	
		person = personRepository.findById(personId)
				.orElseThrow(() -> new AppException("Invalid Person Id " + personId, null));
    	
    	deleter = appUserRepository.findById(SecurityContext.getCurrentUser())
    			.orElseThrow(() -> new AppException("Invalid User Id " + SecurityContext.getCurrentUser(), null));
    	deletedAt = new Timestamp(System.currentTimeMillis());
    	
    	relationList = relationRepository.findByPerson1(person);
    	relationList.addAll(relationRepository.findByPerson2(person));
    	for (Relation relation : relationList) {
    		for (AttributeValue attributeValue : relation.getAttributeValueList()) {
    			attributeValue.setDeleter(deleter);
    			attributeValue.setDeletedAt(deletedAt);
    			attributeValueRepository.save(attributeValue);
    		}
    		relation.setDeleter(deleter);
    		relation.setDeletedAt(deletedAt);
        	relationRepository.save(relation);
		}
    	
		for (AttributeValue attributeValue : person.getAttributeValueList()) {
			attributeValue.setDeleter(deleter);
			attributeValue.setDeletedAt(deletedAt);
			attributeValueRepository.save(attributeValue);
		}
    	person.setDeleter(deleter);
    	person.setDeletedAt(deletedAt);
    	personRepository.save(person);
    }
        
    protected class RelatedPerson2VO {
    	Person person;
    	int level;
    }
    
    protected class RelatedPerson3VO  implements Comparable<RelatedPerson3VO> {
    	long personId;
    	int seqNo;

    	public RelatedPerson3VO(long personId, int seqNo) {
    		this.personId = personId;
    		this.seqNo = seqNo;
    	}
    	
    	public int compareTo(RelatedPerson3VO relatedPerson3VO) {
    		return (this.seqNo < relatedPerson3VO.seqNo ? -1 : this.seqNo == relatedPerson3VO.seqNo ? 0 : 1);
    	}
    }
    
}
