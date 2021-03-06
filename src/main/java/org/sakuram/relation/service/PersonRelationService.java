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

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.sakuram.relation.bean.AttributeValue;
import org.sakuram.relation.bean.DomainValue;
import org.sakuram.relation.bean.Person;
import org.sakuram.relation.bean.Relation;
import org.sakuram.relation.bean.Tenant;
import org.sakuram.relation.repository.AttributeValueRepository;
import org.sakuram.relation.repository.DomainValueRepository;
import org.sakuram.relation.repository.PersonRepository;
import org.sakuram.relation.repository.RelationRepository;
import org.sakuram.relation.repository.TenantRepository;
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
import org.sakuram.relation.valueobject.RetrievePersonAttributesResponseVO;
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
	TenantRepository tenantRepository;
	
	@Autowired
	ServiceParts serviceParts;
	
	int maxLevel;
	
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
    	startPerson = personRepository.findByIdAndTenant(retrieveRelationsRequestVO.getStartPersonId(), SecurityContext.getCurrentTenant())
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
    	
    	end1Person = personRepository.findByIdAndTenant(retrieveRelationsBetweenRequestVO.getEnd1PersonId(), SecurityContext.getCurrentTenant())
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
    	startPerson = personRepository.findByIdAndTenant(retrieveRelationsRequestVO.getStartPersonId(), SecurityContext.getCurrentTenant())
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
	
	public List<List<Object>> exportTree(RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
		List<List<Object>> treeCsvContents;
		GraphVO treeGraphVO;
		Map<String, PersonVO> personsMap;
		List<Object> treeCsvRow;
		
		treeCsvContents = new ArrayList<List<Object>>();
		retrieveRelationsRequestVO.setMaxDepth(Constants.EXPORT_TREE_MAX_DEPTH);
		treeGraphVO = retrieveTree(retrieveRelationsRequestVO);
		
		personsMap = new HashMap<String, PersonVO>();
		for(PersonVO node : treeGraphVO.getNodes()) {
			personsMap.put(node.getId(), node);
		}

		/* for(RelationVO edge : treeGraphVO.getEdges()) {
			System.out.println(personsMap.get(edge.getSource()).getFirstName() + ":" + personsMap.get(edge.getTarget()).getFirstName() + ":" + edge.getLabel());
		} */
	
		maxLevel = 0;
		exportWriteTree(String.valueOf(retrieveRelationsRequestVO.getStartPersonId()), 0, personsMap, treeGraphVO.getEdges(), treeCsvContents);
		
		treeCsvRow = new ArrayList<Object>((maxLevel + 1) * 2);
		treeCsvContents.add(0, treeCsvRow);
		for(int ind2 = 0; ind2 <= maxLevel; ind2++) {
			treeCsvRow.add("Level " + (ind2 + 1));
			treeCsvRow.add("Level " + (ind2 + 1) + " Spouse");
		}
		
		return treeCsvContents;
	}
	
	private void exportWriteTree(String personId, int level, Map<String, PersonVO> personsMap, List<RelationVO> relationsList, List<List<Object>> treeCsvContents) {
		List<String> spousesList;
		boolean isFirstSpouse;
		
		if (level > maxLevel) {
			maxLevel = level;
		}
		// Person
		exportWriteRow(personId, level * 2, false, personsMap, treeCsvContents);
		
		// Spouse
		spousesList = getSpouses(personId, relationsList);
		isFirstSpouse = true;
		if (spousesList.size() > 0) {
			for(String spouseId : spousesList) {
				exportWriteRow(spouseId, level * 2 + 1, isFirstSpouse, personsMap, treeCsvContents);
				isFirstSpouse = false;
				
				// Kids
				for(String kidId : getKids(personId, spouseId, relationsList)) {
					exportWriteTree(kidId, level + 1, personsMap, relationsList, treeCsvContents);
				}
			}
		} else {
			// Kids
			for(String kidId : getKids(personId, null, relationsList)) {
				exportWriteTree(kidId, level + 1, personsMap, relationsList, treeCsvContents);
			}
			
		}
	}
	
	private void exportWriteRow(String personId, int index, boolean toReuseLastRow, Map<String, PersonVO> personsMap, List<List<Object>> treeCsvContents) {
		List<Object> treeCsvRow;
		
		if (toReuseLastRow) {
			treeCsvRow = treeCsvContents.get(treeCsvContents.size() - 1);
		} else {
			treeCsvRow = new ArrayList<Object>(index + 1);
			treeCsvContents.add(treeCsvRow);
		}
		if(personsMap.containsKey(personId)) {
			UtilFuncs.listSet(treeCsvRow, index, personsMap.get(personId).getLabel(), null);
		} else {
			UtilFuncs.listSet(treeCsvRow, index, personId, null);
		}
	}
	
	private List<String> getSpouses(String personId, List<RelationVO> relationsList) {
		List<String> spousesList;
		String spouseId, person2ForPerson1RelId;
		float sequenceNo, randSequenceNo;
		
		spousesList = new ArrayList<String>();
		randSequenceNo = 1;
		for(RelationVO relationVO : relationsList) {
			person2ForPerson1RelId = Constants.RELATION_NAME_TO_ID_MAP.get(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1));
			if (person2ForPerson1RelId == Constants.RELATION_NAME_HUSBAND || person2ForPerson1RelId == Constants.RELATION_NAME_WIFE) {
				if (relationVO.getSource().equals(personId)) {
					sequenceNo = relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1).equals("") ? randSequenceNo++ : Float.valueOf(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1));
					spouseId = relationVO.getTarget();
				} else if (relationVO.getTarget().equals(personId)) {
					sequenceNo = relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2).equals("") ? randSequenceNo++ : Float.valueOf(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2));
					spouseId = relationVO.getSource();
				} else {
					continue;
				}
				UtilFuncs.listSet(spousesList, sequenceNo, spouseId, null);
			}
		}
		return spousesList.size() == 0 ? spousesList : spousesList.subList(1, spousesList.size());
	}
	
	private List<String> getKids(String parent1Id, String parent2Id, List<RelationVO> relationsList) {
		List<String> parent1SatisfiedKidsList, parent2SatisfiedKidsList, kidsList;
		String kidId, person2ForPerson1RelId, person1ForPerson2RelId;
		float sequenceNo, randSequenceNo;
		
		parent1SatisfiedKidsList = new ArrayList<String>();
		parent2SatisfiedKidsList = new ArrayList<String>();
		kidsList = new ArrayList<String>();
		randSequenceNo = 1;
		for(RelationVO relationVO : relationsList) {
			person2ForPerson1RelId = Constants.RELATION_NAME_TO_ID_MAP.get(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1));
			person1ForPerson2RelId = Constants.RELATION_NAME_TO_ID_MAP.get(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2));
			if(relationVO.getSource().equals(parent1Id) && (person2ForPerson1RelId == Constants.RELATION_NAME_SON || person2ForPerson1RelId == Constants.RELATION_NAME_DAUGHTER) ||
					relationVO.getTarget().equals(parent1Id) && (person1ForPerson2RelId == Constants.RELATION_NAME_SON || person1ForPerson2RelId == Constants.RELATION_NAME_DAUGHTER)) {
				kidId = relationVO.getSource().equals(parent1Id) ? relationVO.getTarget() : relationVO.getSource();
				if (parent2SatisfiedKidsList.contains(kidId) || parent2Id == null) {
					if (relationVO.getSource().equals(parent1Id)) {
						sequenceNo = relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1).equals("") ? randSequenceNo++ : Float.valueOf(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1));
					} else if (relationVO.getTarget().equals(parent1Id)) {
						sequenceNo = relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2).equals("") ? randSequenceNo++ : Float.valueOf(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2));
					} else {
						continue;
					}
					UtilFuncs.listSet(kidsList, sequenceNo, kidId, null);
				} else {
					parent1SatisfiedKidsList.add(kidId);
				}
			} else if(relationVO.getSource().equals(parent2Id) && (person2ForPerson1RelId == Constants.RELATION_NAME_SON || person2ForPerson1RelId == Constants.RELATION_NAME_DAUGHTER) ||
					relationVO.getTarget().equals(parent2Id) && (person1ForPerson2RelId == Constants.RELATION_NAME_SON || person1ForPerson2RelId == Constants.RELATION_NAME_DAUGHTER)) {
				kidId = relationVO.getSource().equals(parent2Id) ? relationVO.getTarget() : relationVO.getSource();
				if (parent1SatisfiedKidsList.contains(kidId)) {
					if (relationVO.getSource().equals(parent2Id)) {
						sequenceNo = relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1).equals("") ? randSequenceNo++ : Float.valueOf(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1));
					} else if (relationVO.getTarget().equals(parent2Id)) {
						sequenceNo = relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2).equals("") ? randSequenceNo++ : Float.valueOf(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2));
					} else {
						continue;
					}
					UtilFuncs.listSet(kidsList, sequenceNo, kidId, null);
				} else {
					parent2SatisfiedKidsList.add(kidId);
				}
			}
		}
		return kidsList.size() == 0 ? kidsList : kidsList.subList(1, kidsList.size());
	}
	
	public GraphVO retrieveParceners(RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
		Person startPerson;
		List<RelatedPerson1VO> relatedPerson1VOList;
		RetrieveRelationsRequestVO retrieveRelationsRequestVO2;
		short depth;
		
    	startPerson = personRepository.findByIdAndTenant(retrieveRelationsRequestVO.getStartPersonId(), SecurityContext.getCurrentTenant())
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
	
	public RetrieveAppStartValuesResponseVO retrieveAppStartValues(Long tenantId) {
		RetrieveAppStartValuesResponseVO retrieveAppStartValuesResponseVO;
		Tenant tenant;
		
		retrieveAppStartValuesResponseVO = new RetrieveAppStartValuesResponseVO();
		retrieveAppStartValuesResponseVO.setDomainValueVOList(retrieveDomainValues());
		if (tenantId != null) {
    		tenant = tenantRepository.findById(tenantId)
    				.orElseThrow(() -> new AppException("Invalid Tenant Id " + tenantId, null));
    		retrieveAppStartValuesResponseVO.setInUseProject(tenant.getProjectId());
		}

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
    		domainValueVO.setValidationJsRegEx(domainValueFlags.getValidationJsRegEx());
    	}
    	
    	return domainValueVOList;
    }
    
    public RetrievePersonAttributesResponseVO retrievePersonAttributes(long entityId) {
    	Person person;
    	List<AttributeValue> attributeValueList;
    	RetrievePersonAttributesResponseVO retrievePersonAttributesResponseVO;
    	
    	retrievePersonAttributesResponseVO = new RetrievePersonAttributesResponseVO();
		person = personRepository.findByIdAndTenant(entityId, SecurityContext.getCurrentTenant())
				.orElseThrow(() -> new AppException("Invalid Person Id " + entityId, null));
		retrievePersonAttributesResponseVO.setPhoto(person.getPhoto());
		attributeValueList = person.getAttributeValueList();
		retrievePersonAttributesResponseVO.setAttributeValueVOList(attributeValuesEntityToVo(attributeValueList));
		return retrievePersonAttributesResponseVO;
    }
    
    public RetrieveRelationAttributesResponseVO retrieveRelationAttributes(long entityId) {
    	Relation relation;
    	List<AttributeValue> attributeValueList;
    	RetrieveRelationAttributesResponseVO retrieveRelationAttributesResponseVO;
    	
		relation = relationRepository.findByIdAndTenant(entityId, SecurityContext.getCurrentTenant())
				.orElseThrow(() -> new AppException("Invalid Relation Id " + entityId, null));
		attributeValueList = relation.getAttributeValueList();
		
    	retrieveRelationAttributesResponseVO = new RetrieveRelationAttributesResponseVO();
    	retrieveRelationAttributesResponseVO.setPerson1Id(relation.getPerson1().getId());
    	// Following two for loops can be replaced with attributeValueRepository.findByPersonAndAttribute(relation.getPersonX(), genderAttributeDv)
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
    	SaveAttributesResponseVO saveAttributesResponseVO;
    	
    	if (saveAttributesRequestVO.getEntityId() == Constants.NEW_ENTITY_ID) {
    		person = new Person();
    		person = personRepository.save(person);
    	}
    	else {
    		person = personRepository.findByIdAndTenant(saveAttributesRequestVO.getEntityId(), SecurityContext.getCurrentTenant())
    				.orElseThrow(() -> new AppException("Invalid Person Id " + saveAttributesRequestVO.getEntityId(), null));
    	}
    	person.setPhoto(saveAttributesRequestVO.getPhoto());
    	
		saveAttributesResponseVO = new SaveAttributesResponseVO();
		saveAttributesResponseVO.setEntityId(person.getId());
		saveAttributesResponseVO.setInsertedAttributeValueIdList(saveAttributeValue(saveAttributesRequestVO.getAttributeValueVOList(), person, null));
    	return saveAttributesResponseVO;
    }
    
    public SaveAttributesResponseVO saveRelationAttributes(SaveAttributesRequestVO saveAttributesRequestVO) {
    	Relation relation = null;
    	SaveAttributesResponseVO saveAttributesResponseVO;
    	
		relation = relationRepository.findByIdAndTenant(saveAttributesRequestVO.getEntityId(), SecurityContext.getCurrentTenant())
				.orElseThrow(() -> new AppException("Invalid Relation Id " + saveAttributesRequestVO.getEntityId(), null));
    	
		saveAttributesResponseVO = new SaveAttributesResponseVO();
		saveAttributesResponseVO.setEntityId(saveAttributesRequestVO.getEntityId());
		saveAttributesResponseVO.setInsertedAttributeValueIdList(saveAttributeValue(saveAttributesRequestVO.getAttributeValueVOList(), null, relation));
    	return saveAttributesResponseVO;
    }

    private List<Long> saveAttributeValue(List<AttributeValueVO> attributeValueVOList, Person person, Relation relation) {
    	AttributeValue attributeValue, insertedAttributeValue;
    	List<Long> incomingAttributeValueWithIdList, insertedAttributeValueIdList;
    	List<AttributeValue> toDeleteAttributeValueList;
    	DomainValueFlags domainValueFlags;
    	
		toDeleteAttributeValueList = (person != null ? person.getAttributeValueList() : relation.getAttributeValueList());
		LogManager.getLogger().debug("1. Before update, no. of attributes in DB: " + (toDeleteAttributeValueList == null ? 0 : toDeleteAttributeValueList.size()));
    	incomingAttributeValueWithIdList = new ArrayList<Long>();
    	insertedAttributeValueIdList = new ArrayList<Long>();
    	for(AttributeValueVO attributeValueVO : attributeValueVOList) {
    		if (attributeValueVO.getId() == null) {
    			throw new AppException("System error: Attribute with null id", null);
    		}
    		else if (attributeValueVO.getId() < 1) {
    			insertedAttributeValue = insertAttributeValue(attributeValueVO, person, relation);
    			insertedAttributeValueIdList.add(insertedAttributeValue.getId());
    		}
    		else {
    			incomingAttributeValueWithIdList.add(attributeValueVO.getId());
    			attributeValue = attributeValueRepository.findByIdAndTenant(attributeValueVO.getId(), SecurityContext.getCurrentTenant())
    					.orElseThrow(() -> new AppException("Invalid Attribute Value Id " + attributeValueVO.getId(), null));
    			if (attributeValueVO.getAttributeDvId() != attributeValue.getAttribute().getId()) {
    				throw new AppException("Invalid input from client.", null);
    			}
    			if (!Objects.equals(attributeValueVO.getAttributeValue(), attributeValue.getAttributeValue()) ||
    					!Objects.equals(attributeValueVO.isValueAccurate(), attributeValue.isValueAccurate()) ||
    					!UtilFuncs.dateEquals(attributeValueVO.getStartDate(), attributeValue.getStartDate()) ||
    					!UtilFuncs.dateEquals(attributeValueVO.getEndDate(), attributeValue.getEndDate())) {
    				insertedAttributeValue = insertAttributeValue(attributeValueVO, person, relation);
    				attributeValue.setOverwrittenBy(insertedAttributeValue);
    				attributeValueRepository.save(attributeValue);
    			}
    		}
    	}
    	
    	LogManager.getLogger().debug("2. Before update, no. of attributes in DB: " + (toDeleteAttributeValueList == null ? 0 : toDeleteAttributeValueList.size()));
    	domainValueFlags = new DomainValueFlags();
		if (toDeleteAttributeValueList != null) {
	    	for(AttributeValue toDeleteAttributeValue : toDeleteAttributeValueList) {
	    		domainValueFlags.setDomainValue(toDeleteAttributeValue.getAttribute());
	    		if (domainValueFlags.isInputAsAttribute() && !incomingAttributeValueWithIdList.contains(toDeleteAttributeValue.getId())) {
	    			toDeleteAttributeValue.setDeleter(SecurityContext.getCurrentUser());
	    			toDeleteAttributeValue.setDeletedAt(new Timestamp(System.currentTimeMillis()));
					attributeValueRepository.save(toDeleteAttributeValue);
	    		}
	    	}
		}
		
		return insertedAttributeValueIdList;
    }

    private AttributeValue insertAttributeValue(AttributeValueVO attributeValueVO, Person person, Relation relation) {
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
		attributeValue.setCreator(SecurityContext.getCurrentUser());
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
    	
    	parentsCriteria = null;
    	spousesCriteria = null;
    	querySB = new StringBuilder();
    	querySB.append("SELECT * FROM person p LEFT OUTER JOIN tenant t ON p.tenant_fk = t.id WHERE p.overwritten_by_fk IS NULL AND p.deleter_fk IS NULL");
    	// TODO: AOP to take of the following if block
    	if (SecurityContext.getCurrentTenantId() != null) {
    		querySB.append(" AND p.tenant_fk = ");
    		querySB.append(SecurityContext.getCurrentTenantId());
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
    
    public List<String> retrieveGendersOfPersons(List<Long> personsList) {
    	List<String> gendersOfPersonsList;
    	Person person;
    	AttributeValue genderAv;
    	DomainValue attributeDv;
    	
    	gendersOfPersonsList = new ArrayList<String>(personsList.size());
    	for (long personId : personsList) {
        	person = personRepository.findByIdAndTenant(personId, SecurityContext.getCurrentTenant())
    				.orElseThrow(() -> new AppException("Invalid Person Id " + personId, null));
			attributeDv = domainValueRepository.findById(Constants.PERSON_ATTRIBUTE_DV_ID_GENDER)
					.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + Constants.PERSON_ATTRIBUTE_DV_ID_GENDER, null));
			genderAv = attributeValueRepository.findByPersonAndAttribute(person, attributeDv)
					.orElseThrow(() -> new AppException("Invalid gender for " + personId, null));
			gendersOfPersonsList.add(genderAv.getAttributeValue());
    	}
    	return gendersOfPersonsList;
    }
    
    public RelationVO saveRelation(RelatedPersonsVO saveRelationRequestVO) {
    	// Person 1 is expected to be one of Father, Mother, Husband
    	Person person1, person2;
    	Relation relation;
    	AttributeValue attributeValue1, attributeValue2, genderAv;
    	DomainValue attributeDv;
    	List<RelationVO> relationVOList;
    	
    	if (relationRepository.findRelationGivenPersons(saveRelationRequestVO.getPerson1Id(), saveRelationRequestVO.getPerson2Id(), SecurityContext.getCurrentTenantId()) != null) {
    		throw new AppException(saveRelationRequestVO.getPerson1Id() + " and " + saveRelationRequestVO.getPerson2Id() + " are already related.", null);
    	}
    	person1 = personRepository.findByIdAndTenant(saveRelationRequestVO.getPerson1Id(), SecurityContext.getCurrentTenant())
				.orElseThrow(() -> new AppException("Invalid Person Id " + saveRelationRequestVO.getPerson1Id(), null));
    	person2 = personRepository.findByIdAndTenant(saveRelationRequestVO.getPerson2Id(), SecurityContext.getCurrentTenant())
				.orElseThrow(() -> new AppException("Invalid Person Id " + saveRelationRequestVO.getPerson2Id(), null));

    	relation = new Relation(person1, person2);
    	relation = relationRepository.save(relation);

    	attributeValue1 = new AttributeValue();
		attributeDv = domainValueRepository.findById(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2)
				.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2, null));
		attributeValue1.setAttribute(attributeDv);
		attributeValue1.setAttributeValue(saveRelationRequestVO.getPerson1ForPerson2());
		attributeValue1.setRelation(relation);
		attributeValue1.setCreator(SecurityContext.getCurrentUser());
		attributeValueRepository.save(attributeValue1);
		
		attributeValue2 = new AttributeValue();
		attributeDv = domainValueRepository.findById(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1)
				.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1, null));
		attributeValue2.setAttribute(attributeDv);
		if (saveRelationRequestVO.getPerson1ForPerson2().equals(Constants.RELATION_NAME_MOTHER) || saveRelationRequestVO.getPerson1ForPerson2().equals(Constants.RELATION_NAME_FATHER)) {
			attributeDv = domainValueRepository.findById(Constants.PERSON_ATTRIBUTE_DV_ID_GENDER)
					.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + Constants.PERSON_ATTRIBUTE_DV_ID_GENDER, null));
			genderAv = attributeValueRepository.findByPersonAndAttribute(person2, attributeDv)
					.orElseThrow(() -> new AppException("Invalid gender for " + saveRelationRequestVO.getPerson2Id(), null));
			if (genderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE)) {
				attributeValue2.setAttributeValue(Constants.RELATION_NAME_SON);
			} else if (genderAv.getAttributeValue().equals(Constants.GENDER_NAME_FEMALE)) {
				attributeValue2.setAttributeValue(Constants.RELATION_NAME_DAUGHTER);
			} else {
				throw new AppException("Incomplete support for Gender " + genderAv.getAttributeValue(), null);
			}
		} else if (saveRelationRequestVO.getPerson1ForPerson2().equals(Constants.RELATION_NAME_HUSBAND)) {
			attributeValue2.setAttributeValue(Constants.RELATION_NAME_WIFE);
		}
		attributeValue2.setRelation(relation);
		attributeValue2.setCreator(SecurityContext.getCurrentUser());
		attributeValueRepository.save(attributeValue2);
		
		relation.setAttributeValueList(new ArrayList<AttributeValue>(Arrays.asList(attributeValue1, attributeValue2)));
    	relationVOList = new ArrayList<RelationVO>();
		serviceParts.addToRelationVOList(relationVOList, relation, null, false);
    	return relationVOList.get(0);
    }
    
    public void deleteRelation(long relationId) {
    	Relation relation;
    	Timestamp deletedAt;
    	
		relation = relationRepository.findByIdAndTenant(relationId, SecurityContext.getCurrentTenant())
				.orElseThrow(() -> new AppException("Invalid Relation Id " + relationId, null));
    	
    	deletedAt = new Timestamp(System.currentTimeMillis());
		for (AttributeValue attributeValue : relation.getAttributeValueList()) {
			attributeValue.setDeleter(SecurityContext.getCurrentUser());
			attributeValue.setDeletedAt(deletedAt);
		}
    	relation.setDeleter(SecurityContext.getCurrentUser());
    	relation.setDeletedAt(deletedAt);
    	relationRepository.save(relation);
    }
        
    public void deletePerson(long personId) {
    	Person person;
    	List<Relation> relationList;
    	Timestamp deletedAt;
    	
		person = personRepository.findByIdAndTenant(personId, SecurityContext.getCurrentTenant())
				.orElseThrow(() -> new AppException("Invalid Person Id " + personId, null));
    	
    	deletedAt = new Timestamp(System.currentTimeMillis());
    	
    	relationList = relationRepository.findByPerson1(person);
    	relationList.addAll(relationRepository.findByPerson2(person));
    	for (Relation relation : relationList) {
    		for (AttributeValue attributeValue : relation.getAttributeValueList()) {
    			attributeValue.setDeleter(SecurityContext.getCurrentUser());
    			attributeValue.setDeletedAt(deletedAt);
    			attributeValueRepository.save(attributeValue);
    		}
    		relation.setDeleter(SecurityContext.getCurrentUser());
    		relation.setDeletedAt(deletedAt);
        	relationRepository.save(relation);
		}
    	
		for (AttributeValue attributeValue : person.getAttributeValueList()) {
			attributeValue.setDeleter(SecurityContext.getCurrentUser());
			attributeValue.setDeletedAt(deletedAt);
			attributeValueRepository.save(attributeValue);
		}
    	person.setDeleter(SecurityContext.getCurrentUser());
    	person.setDeletedAt(deletedAt);
    	personRepository.save(person);
    }

    public void importPrData(Iterable<CSVRecord> csvRecords) {
    	/* A cell content (Person details) is either skipped (if person id) or INSERTed.
    	 * For relationship to be INSERTed, no prior relationship should exist between the two persons already.
    	 * There is no DELETE or MODIFY of Person and Relation.
    	 * Special first name DITTO with gender to handle multiple spouses Vs. Empty cell to mean details are not known
    	 * Different sequence within each parent not supported
    	 */
    	int recordSize, level;
    	Person mainPerson, spousePerson;
    	Relation relation;
    	AttributeValue attributeValue, mainPersonGenderAv, spousePersonGenderAv;
    	DomainValue firstNamePersAttributeDv, genderPersAttributeDv, labelPersAttributeDv, person1ForPerson2RelAttributeDv, person2ForPerson1RelAttributeDv, sequenceOfPerson2ForPerson1RelAttributeDv;
    	List<Person> malePersonList, femalePersonList;
    	ParsedCellContentVO parsedCellContentVO;
    	Integer withinSpouseSequenceNo, withinParentSequenceNo;
    	
		firstNamePersAttributeDv = domainValueRepository.findById(Constants.PERSON_ATTRIBUTE_DV_ID_FIRST_NAME)
				.orElseThrow(() -> new AppException("Attribute Dv Id missing: " + Constants.PERSON_ATTRIBUTE_DV_ID_FIRST_NAME, null));
		genderPersAttributeDv = domainValueRepository.findById(Constants.PERSON_ATTRIBUTE_DV_ID_GENDER)
				.orElseThrow(() -> new AppException("Attribute Dv Id missing: " + Constants.PERSON_ATTRIBUTE_DV_ID_GENDER, null));
		labelPersAttributeDv = domainValueRepository.findById(Constants.PERSON_ATTRIBUTE_DV_ID_LABEL)
				.orElseThrow(() -> new AppException("Attribute Dv Id missing: " + Constants.PERSON_ATTRIBUTE_DV_ID_LABEL, null));
		person1ForPerson2RelAttributeDv =  domainValueRepository.findById(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2)
				.orElseThrow(() -> new AppException("Attribute Dv Id missing: " + Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2, null));
		person2ForPerson1RelAttributeDv =  domainValueRepository.findById(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1)
				.orElseThrow(() -> new AppException("Attribute Dv Id missing: " + Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1, null));
		sequenceOfPerson2ForPerson1RelAttributeDv =  domainValueRepository.findById(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1)
				.orElseThrow(() -> new AppException("Attribute Dv Id missing: " + Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1, null));

		malePersonList = new ArrayList<Person>();
		femalePersonList = new ArrayList<Person>();

    	for (CSVRecord csvRecord : csvRecords) {
			mainPerson = null;
			spousePerson = null;
	    	mainPersonGenderAv = null;
	    	spousePersonGenderAv = null;
	    	withinParentSequenceNo = null;
	    	withinSpouseSequenceNo = null;
    		recordSize = csvRecord.size();
    		
    		if (recordSize == 0) {
    			continue;
    		}
    		if (csvRecord.get(recordSize - 1).equals("")) {
    			throw new AppException("It's likely that there are empty cells towards the end of the record. Use a text editor and remove redundant delimiters.", null);
    		}
    		
    		if (recordSize % 2 != 0) {
    			// Spouse not given
    			level = recordSize / 2;
    	    	parsedCellContentVO = cellContentsToPerson(csvRecord.get(recordSize - 1), level, malePersonList, femalePersonList, firstNamePersAttributeDv, genderPersAttributeDv, labelPersAttributeDv);
    	    	mainPerson = parsedCellContentVO.person;
    	    	withinParentSequenceNo = parsedCellContentVO.sequenceNo;
    	    	mainPersonGenderAv = attributeValueRepository.findByPersonAndAttribute(mainPerson, genderPersAttributeDv)
    	    			.orElseThrow(() -> new AppException("Gender missing", null));
    		} else {
    			level = recordSize / 2 - 1;
    			if (csvRecord.get(recordSize - 2).equals("")) {
    				// Person not given
    				parsedCellContentVO = cellContentsToPerson(csvRecord.get(recordSize - 1), level, malePersonList, femalePersonList, firstNamePersAttributeDv, genderPersAttributeDv, labelPersAttributeDv);
        	    	spousePerson = parsedCellContentVO.person;
        	    	spousePersonGenderAv = attributeValueRepository.findByPersonAndAttribute(spousePerson, genderPersAttributeDv)
        	    			.orElseThrow(() -> new AppException("Gender missing", null));
    			} else {
    				// Both are given
    				parsedCellContentVO = cellContentsToPerson(csvRecord.get(recordSize - 2), level, malePersonList, femalePersonList, firstNamePersAttributeDv, genderPersAttributeDv, labelPersAttributeDv);
        	    	mainPerson = parsedCellContentVO.person;
        	    	withinParentSequenceNo = parsedCellContentVO.sequenceNo;
        	    	mainPersonGenderAv = attributeValueRepository.findByPersonAndAttribute(mainPerson, genderPersAttributeDv)
        	    			.orElseThrow(() -> new AppException("Gender missing", null));
        	    	
        	    	parsedCellContentVO = cellContentsToPerson(csvRecord.get(recordSize - 1), level, malePersonList, femalePersonList, firstNamePersAttributeDv, genderPersAttributeDv, labelPersAttributeDv);
        	    	spousePerson = parsedCellContentVO.person;
        	    	withinSpouseSequenceNo = parsedCellContentVO.sequenceNo;
        	    	
        	    	if (relationRepository.findRelationGivenPersons(mainPerson.getId(), spousePerson.getId(), SecurityContext.getCurrentTenantId()) != null) {
        	    		// mainPerson and spousePerson are already related
        	    	} else {
	        			if (mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE)) {
	        				relation = new Relation(mainPerson, spousePerson);
	        			} else if (mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_FEMALE)) {
	        				relation = new Relation(spousePerson, mainPerson);
	        			} else {
	        				throw new AppException("Incomplete support for Gender " + mainPersonGenderAv.getAttributeValue(), null);
	        			}
	        	    	relation = relationRepository.save(relation);
	        	    	
	    				attributeValue = new AttributeValue(person1ForPerson2RelAttributeDv, Constants.RELATION_NAME_HUSBAND, null, relation);
	    	    		attributeValueRepository.save(attributeValue);
	    	    		
	    				attributeValue = new AttributeValue(person2ForPerson1RelAttributeDv, Constants.RELATION_NAME_WIFE, null, relation);
	    	    		attributeValueRepository.save(attributeValue);
	    	    		
	    	    		if (withinSpouseSequenceNo != null) {
		    				attributeValue = new AttributeValue(sequenceOfPerson2ForPerson1RelAttributeDv, withinSpouseSequenceNo.toString(), null, relation);
		    	    		attributeValueRepository.save(attributeValue);
	    	    		}
        	    	}
    			}
    		}
    		if (mainPerson != null &&
    				mainPersonGenderAv != null && mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE) ||
    				spousePerson != null &&
    				spousePersonGenderAv != null && spousePersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_FEMALE)) {
    			UtilFuncs.listSet(malePersonList, level, mainPerson, null);
    			UtilFuncs.listSet(femalePersonList, level, spousePerson, null);
    		} else if (mainPerson != null &&
    				mainPersonGenderAv != null && mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_FEMALE) ||
    				spousePerson != null &&
    				spousePersonGenderAv != null && spousePersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE)) {
    			UtilFuncs.listSet(malePersonList, level, spousePerson, null);
    			UtilFuncs.listSet(femalePersonList, level, mainPerson, null);
			} else {
				throw new AppException("Incomplete support for Gender", null);
			}
    		
    		if (mainPerson != null) {
        		establishParent(mainPerson, mainPersonGenderAv, withinParentSequenceNo, level, Constants.RELATION_NAME_FATHER, malePersonList, person1ForPerson2RelAttributeDv, person2ForPerson1RelAttributeDv, sequenceOfPerson2ForPerson1RelAttributeDv);
        		establishParent(mainPerson, mainPersonGenderAv, withinParentSequenceNo, level, Constants.RELATION_NAME_MOTHER, femalePersonList, person1ForPerson2RelAttributeDv, person2ForPerson1RelAttributeDv, sequenceOfPerson2ForPerson1RelAttributeDv);
    		}
    	}
    }
    
    private ParsedCellContentVO cellContentsToPerson(String cellContents, int level, List<Person> malePersonList, List<Person> femalePersonList, DomainValue firstNamePersAttributeDv, DomainValue genderPersAttributeDv, DomainValue labelPersAttributeDv) {
    	long personId;
    	Person person;
    	String[] personAttributeValuesArr;
    	AttributeValue attributeValue;
    	ParsedCellContentVO parsedCellContentVO;
    	Integer sequenceNo;
    	boolean isMale;
    	
    	sequenceNo = null;
    	try {
    		personId = Long.parseLong(cellContents);
    		person = personRepository.findByIdAndTenant(personId, SecurityContext.getCurrentTenant())
    				.orElseThrow(() -> new AppException("Invalid Person Id " + personId, null));
    	} catch(NumberFormatException e) {
    		personAttributeValuesArr = cellContents.split("#", -1);
    		
    		if (personAttributeValuesArr.length != 4) {
    			throw new AppException("Person Details missing four components: " + cellContents, null);
    		}
    		
    		if (!personAttributeValuesArr[0].equals("")) {
    	    	try {
    	    		sequenceNo = Integer.parseInt(personAttributeValuesArr[0]);
    	    	} catch(NumberFormatException nfe) {
        			throw new AppException("Invalid Sequence No.: " + personAttributeValuesArr[0], null);
    	    	}
    		}
    		
    		if (personAttributeValuesArr[1].equals("")) {
    			throw new AppException("First name cannot be empty. Person Details: " + cellContents, null);
    		}
    		if (personAttributeValuesArr[2].equals("M")) {
    			isMale = true;
    		} else if (personAttributeValuesArr[2].equals("F")) {
    			isMale = false;
			} else {
				throw new AppException("Unsupported Gender " + personAttributeValuesArr[2], null);
			}
    		
    		if (personAttributeValuesArr[1].equalsIgnoreCase("DITTO")) {
    			if (isMale) {
    				person = malePersonList.get(level);
    			} else {
    				person = femalePersonList.get(level);
    			}
    		} else {
        		person = new Person();
        		person = personRepository.save(person);
        		
	    		attributeValue = new AttributeValue(firstNamePersAttributeDv, personAttributeValuesArr[1], person, null);
	    		attributeValueRepository.save(attributeValue);
	    		
	        	attributeValue = new AttributeValue(genderPersAttributeDv, isMale? Constants.GENDER_NAME_MALE : Constants.GENDER_NAME_FEMALE, person, null);
	    		attributeValueRepository.save(attributeValue);
	    		
	    		if (personAttributeValuesArr[3].equals("")) {
	    			personAttributeValuesArr[3] = personAttributeValuesArr[1];
	    		}
	    		attributeValue = new AttributeValue(labelPersAttributeDv, personAttributeValuesArr[3], person, null);
	    		attributeValueRepository.save(attributeValue);
    		}
    	}
    	
    	parsedCellContentVO = new ParsedCellContentVO();
    	parsedCellContentVO.person = person;
    	parsedCellContentVO.sequenceNo = sequenceNo;
    	return parsedCellContentVO;
    }
    
    private void establishParent(Person mainPerson, AttributeValue mainPersonGenderAv, Integer sequenceNo, int level, String parentRelationName, List<Person> personList, DomainValue person1ForPerson2RelAttributeDv, DomainValue person2ForPerson1RelAttributeDv, DomainValue sequenceOfPerson2ForPerson1RelAttributeDv) {
    	AttributeValue attributeValue;
    	Relation relation;

    	if (level > 0 && personList.get(level - 1) != null &&
    			relationRepository.findRelationGivenPersons(mainPerson.getId(), personList.get(level - 1).getId(), SecurityContext.getCurrentTenantId()) == null) {
	    	relation = new Relation(personList.get(level - 1), mainPerson);
	    	relation = relationRepository.save(relation);
	    	
			attributeValue = new AttributeValue(person1ForPerson2RelAttributeDv, parentRelationName, null, relation);
    		attributeValueRepository.save(attributeValue);
    		
			if (mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE)) {
				attributeValue = new AttributeValue(person2ForPerson1RelAttributeDv, Constants.RELATION_NAME_SON, null, relation);
			} else if (mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_FEMALE)) {
				attributeValue = new AttributeValue(person2ForPerson1RelAttributeDv, Constants.RELATION_NAME_DAUGHTER, null, relation);
			} else {
				throw new AppException("Incomplete support for Gender " + mainPersonGenderAv.getAttributeValue(), null);
			}
    		attributeValueRepository.save(attributeValue);
    		
    		if (sequenceNo != null) {
				attributeValue = new AttributeValue(sequenceOfPerson2ForPerson1RelAttributeDv, sequenceNo.toString(), null, relation);
	    		attributeValueRepository.save(attributeValue);
    		}
    	}
    		
    }

    protected class RelatedPerson2VO {
    	Person person;
    	int level;
    }
    
    protected class RelatedPerson3VO  implements Comparable<RelatedPerson3VO> {
    	long personId;
    	float seqNo;

    	public RelatedPerson3VO(long personId, float seqNo) {
    		this.personId = personId;
    		this.seqNo = seqNo;
    	}
    	
    	public int compareTo(RelatedPerson3VO relatedPerson3VO) {
    		return (this.seqNo < relatedPerson3VO.seqNo ? -1 : this.seqNo == relatedPerson3VO.seqNo ? 0 : 1);
    	}
    }
    
    protected class TreeIntermediateOut1VO {
    	String personId;
    	int level;
    	List<TreeIntermediateOut2VO> directRelativesList;
    }
    
    protected class TreeIntermediateOut2VO {
    	String spouseId;
    	List<String> kidsList;
    }
    
    protected class ParsedCellContentVO {
    	Person person;
    	Integer sequenceNo;
    }
}
