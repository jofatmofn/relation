package org.sakuram.relation.service;

import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.javatuples.Pair;
import org.sakuram.relation.bean.AttributeValue;
import org.sakuram.relation.bean.DomainValue;
import org.sakuram.relation.bean.Person;
import org.sakuram.relation.bean.Relation;
import org.sakuram.relation.bean.Tenant;
import org.sakuram.relation.bean.Translation;
import org.sakuram.relation.repository.AttributeValueRepository;
import org.sakuram.relation.repository.DomainValueRepository;
import org.sakuram.relation.repository.PersonRepository;
import org.sakuram.relation.repository.RelationRepository;
import org.sakuram.relation.repository.TenantRepository;
import org.sakuram.relation.repository.TranslationRepository;
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
import org.sakuram.relation.valueobject.PersonSearchCriteriaVO;
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
	TranslationRepository translationRepository;
	
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
    	int readInd, level, currentLevel;
    	double sequence;
    	List<Double> seqAtLevel;
    	List<Person> excludeSpouseList;
    	boolean isFirstKid;
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
		currentPersonVO.setX(0);
		currentPersonVO.setY(0);
		if (retrieveRelationsRequestVO.getRequiredRelationsList() == null || retrieveRelationsRequestVO.getRequiredRelationsList().isEmpty()) {
			retrieveRelationsRequestVO.setRequiredRelationsList(Arrays.asList(Constants.RELATION_NAME_HUSBAND, Constants.RELATION_NAME_WIFE, Constants.RELATION_NAME_SON, Constants.RELATION_NAME_DAUGHTER));
		}
		
		relatedPerson2VO =  new RelatedPerson2VO();
		relatedPerson2VOList.add(relatedPerson2VO);
		relatedPerson2VO.person = startPerson;
		relatedPerson2VO.level = 0;
		seqAtLevel = new ArrayList<Double>();
		seqAtLevel.add(0D);
		readInd = 0;
		/*		relatedPerson2VOList - Built (appended) as it is traversed (Concurrent modification). Will not be in any order w.r.t X and Y.
		 * 		current - Item from relatedPerson2VOList, currently being processed
		 * 		list of relatedPerson1VO - Relatives of current person (in the order of relation type and sequence no.
		 * 		relatedPerson2VO - New person (relatedPerson1VO) added to relatedPerson2VOList
		 * 		relatedPersonVO - New person (relatedPerson1VO) added to personVOMap
		 */
		while (true) {
			currentPerson = relatedPerson2VOList.get(readInd).person;
			currentLevel = relatedPerson2VOList.get(readInd).level;
			LogManager.getLogger().debug("Current person: " + currentPerson.getId() + " at level " + currentLevel);
			if (currentLevel == retrieveRelationsRequestVO.getMaxDepth()) {
				break;
			}
			if (relatedPerson2VOList.get(readInd).isSpouse) {
				excludeSpouseList.add(currentPerson);
			}
			else {
				currentPersonVO = personVOMap.get(currentPerson.getId());
				isFirstKid = true;
		    	for (RelatedPerson1VO relatedPerson1VO : retrieveRelatives(currentPerson, retrieveRelationsRequestVO.getRequiredRelationsList())) {
					if (relatedPerson1VO.relationDvId.equals(Constants.RELATION_NAME_HUSBAND) ||
							relatedPerson1VO.relationDvId.equals(Constants.RELATION_NAME_WIFE) ||
							currentLevel < retrieveRelationsRequestVO.getMaxDepth() - 1) {
						if (relatedPersonIdSet.add(relatedPerson1VO.person.getId())) {
							relatedPerson2VO =  new RelatedPerson2VO();
							relatedPerson2VOList.add(relatedPerson2VO);
							relatedPerson2VO.person = relatedPerson1VO.person;
				    		relatedPersonVO = serviceParts.addToPersonVOMap(personVOMap, relatedPerson1VO.person);
							LogManager.getLogger().debug("Added person: " + relatedPersonVO.getLabel());
				    		if (relatedPerson1VO.relationDvId.equals(Constants.RELATION_NAME_HUSBAND) || relatedPerson1VO.relationDvId.equals(Constants.RELATION_NAME_WIFE)) {
				    			relatedPerson2VO.level = currentLevel;
				    			relatedPerson2VO.isSpouse = true;
			    				sequence = currentPersonVO.getX() + relatedPerson1VO.seqNo;
								LogManager.getLogger().debug("currentPersonVO.getX(): " + currentPersonVO.getX() + ". relatedPerson1VO.seqNo: " + relatedPerson1VO.seqNo);
								LogManager.getLogger().debug("relatedPerson2VO.level: " + relatedPerson2VO.level + ". Initial sequence: " + sequence);
			    				for (int ind = currentLevel; ind > -1; ind--) {
			    					shiftX(personVOMap, sequence, ind, 1);
			    				}
								relatedPersonVO.setX(sequence);
								relatedPersonVO.setY(currentPersonVO.getY());
								if (sequence > seqAtLevel.get(currentLevel)) {
					    			seqAtLevel.set(currentLevel, sequence);
								}
				    		}
				    		else if (currentLevel < retrieveRelationsRequestVO.getMaxDepth() - 1) {
				    			level = currentLevel + 1;
				    			relatedPerson2VO.level = level;
				    			relatedPerson2VO.isSpouse = false;
				    			if (seqAtLevel.size() > level) {
				    				sequence = seqAtLevel.get(level);
					    			sequence++;
				    			}
				    			else {
				    				seqAtLevel.add(0D);
				    				sequence = 0D;
				    			}
				    			if (isFirstKid) {
				    				if (currentPersonVO.getX() >= sequence) {	// Position of child
				    					sequence = currentPersonVO.getX();
				    				} else {	// Alter the position of parent
				    					shiftX(personVOMap, currentPersonVO.getX(), currentLevel, (float) (sequence - currentPersonVO.getX()));
										LogManager.getLogger().debug("Parent Shift");
										LogManager.getLogger().debug("currentLevel" + ": " + currentLevel + ". From " + seqAtLevel.get(currentLevel) + " to " + (seqAtLevel.get(currentLevel) + sequence - currentPersonVO.getX()));
						    			seqAtLevel.set(currentLevel, seqAtLevel.get(currentLevel) + sequence - currentPersonVO.getX());
				    				}
				    				isFirstKid = false;
				    			}
				    			// UtilFuncs.listSet(relatedPerson2VOList, sequence, relatedPerson2VO, null);
								LogManager.getLogger().debug("sequence: " + sequence);
				    			seqAtLevel.set(level, sequence);
								relatedPersonVO.setX(sequence);
								relatedPersonVO.setY(level);
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
	    	for (RelatedPerson1VO relatedPerson1VO : retrieveRelatives(spouse, retrieveRelationsRequestVO.getRequiredRelationsList())) {
				if (relatedPersonIdSet.contains(relatedPerson1VO.person.getId()) && relatedRelationIdSet.add(relatedPerson1VO.relation.getId())) {
					serviceParts.addToRelationVOList(relationVOList, relatedPerson1VO.relation, spouse, false);
				}
	    	}
		}
    	
		personVOList = new ArrayList<PersonVO>(personVOMap.values());
		for (PersonVO personVO : personVOList) {
			LogManager.getLogger().debug(personVO.getId() + "/"  + personVO.getFirstName() + "/" + personVO.getY() + "/" + personVO.getX());
		}
    	retrieveRelationsResponseVO.setNodes(personVOList);
    	return retrieveRelationsResponseVO;
    }
	
	private void shiftX(Map<Long, PersonVO> personVOMap, double fromX, double criteriaY, double shiftBy) {
		
		for (PersonVO personVO : personVOMap.values()) {
			if (personVO.getY() == criteriaY && personVO.getX() >= fromX) {
				LogManager.getLogger().debug("Shift: " + personVO.getY() + ", " + personVO.getX() + ", " + shiftBy);
				personVO.setX(personVO.getX() + shiftBy);
			}
		}
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

		maxLevel = 0;
		exportWriteTree(String.valueOf(retrieveRelationsRequestVO.getStartPersonId()), 0, personsMap, treeGraphVO.getEdges(), treeCsvContents);
		
		treeCsvRow = new ArrayList<Object>((maxLevel + 1) * 2);
		treeCsvContents.add(0, treeCsvRow);
		for(int ind2 = 0; ind2 <= maxLevel; ind2++) {	// TODO: Multi-language support for the following column titles
			treeCsvRow.add("Level " + (ind2 + 1));
			treeCsvRow.add("Level " + (ind2 + 1) + " Spouse");
		}
		
		LogManager.getLogger().debug("Exported rows: " + treeCsvContents.size());
		return treeCsvContents;
	}
	
	public GraphVO displayTree(RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
		List<List<Object>> treeCsvContents;
		GraphVO treeGraphVO;
		Map<String, PersonVO> personsMap;
		
		treeCsvContents = new ArrayList<List<Object>>();
		retrieveRelationsRequestVO.setMaxDepth(Constants.EXPORT_TREE_MAX_DEPTH);
		treeGraphVO = retrieveTree(retrieveRelationsRequestVO);
		
		// Rest of the logic is a round-about way of setting X and Y :(
		personsMap = new HashMap<String, PersonVO>();
		for(PersonVO node : treeGraphVO.getNodes()) {
			personsMap.put(node.getId(), node);
		}

		maxLevel = 0;
		exportWriteTree(String.valueOf(retrieveRelationsRequestVO.getStartPersonId()), 0, personsMap, treeGraphVO.getEdges(), treeCsvContents);
		
		return treeGraphVO;
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
		PersonVO personVO;
		
		if (toReuseLastRow) {
			treeCsvRow = treeCsvContents.get(treeCsvContents.size() - 1);
		} else {
			treeCsvRow = new ArrayList<Object>(index + 1);
			treeCsvContents.add(treeCsvRow);
		}
		if(personsMap.containsKey(personId)) {
			personVO = personsMap.get(personId);
			UtilFuncs.listSet(treeCsvRow, index, personVO.getLabel(), null);
			personVO.setY(treeCsvContents.indexOf(treeCsvRow));
			personVO.setX(index);
			LogManager.getLogger().debug(personVO.getId() + ":"  + personVO.getFirstName() + ":" + personVO.getY() + ":" + personVO.getX());
		} else {
			throw new AppException("Application in inconsistent state?! PersonId: " + personId, null);
			// UtilFuncs.listSet(treeCsvRow, index, personId, null);
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
		LogManager.getLogger().debug(personId + "'s spouses: " +  spousesList.size());
		return spousesList.size() == 0 ? spousesList : spousesList.subList(1, spousesList.size());
	}
	
	private List<String> getKids(String parent1Id, String parent2Id, List<RelationVO> relationsList) {
		Map<String, Float> parent1SatisfiedKidsMap;
		List<String> parent2SatisfiedKidsList;
		List<Pair<String, Float>> kidsList;
		String kidId, person2ForPerson1RelId, person1ForPerson2RelId;
		float sequenceNo, randSequenceNo;
		
		parent1SatisfiedKidsMap = new HashMap<String, Float>();
		parent2SatisfiedKidsList = new ArrayList<String>();
		kidsList = new ArrayList<Pair<String, Float>>();
		randSequenceNo = 1;
		for(RelationVO relationVO : relationsList) {
			person2ForPerson1RelId = Constants.RELATION_NAME_TO_ID_MAP.get(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1));
			person1ForPerson2RelId = Constants.RELATION_NAME_TO_ID_MAP.get(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2));
			if(relationVO.getSource().equals(parent1Id) && (person2ForPerson1RelId == Constants.RELATION_NAME_SON || person2ForPerson1RelId == Constants.RELATION_NAME_DAUGHTER) ||
					relationVO.getTarget().equals(parent1Id) && (person1ForPerson2RelId == Constants.RELATION_NAME_SON || person1ForPerson2RelId == Constants.RELATION_NAME_DAUGHTER)) {
				kidId = relationVO.getSource().equals(parent1Id) ? relationVO.getTarget() : relationVO.getSource();
				if (relationVO.getSource().equals(parent1Id)) {
					sequenceNo = relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1).equals("") ? randSequenceNo++ : Float.valueOf(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1));
				} else if (relationVO.getTarget().equals(parent1Id)) {
					sequenceNo = relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2).equals("") ? randSequenceNo++ : Float.valueOf(relationVO.getAttribute(Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2));
				} else {
					continue;
				}
				if (parent2SatisfiedKidsList.contains(kidId) || parent2Id == null) {
					kidsList.add(Pair.with(kidId, sequenceNo));
				} else {
					parent1SatisfiedKidsMap.put(kidId, sequenceNo);
				}
			} else if(relationVO.getSource().equals(parent2Id) && (person2ForPerson1RelId == Constants.RELATION_NAME_SON || person2ForPerson1RelId == Constants.RELATION_NAME_DAUGHTER) ||
					relationVO.getTarget().equals(parent2Id) && (person1ForPerson2RelId == Constants.RELATION_NAME_SON || person1ForPerson2RelId == Constants.RELATION_NAME_DAUGHTER)) {
				kidId = relationVO.getSource().equals(parent2Id) ? relationVO.getTarget() : relationVO.getSource();
				if (parent1SatisfiedKidsMap.containsKey(kidId)) {
					sequenceNo = parent1SatisfiedKidsMap.get(kidId);
					kidsList.add(Pair.with(kidId, sequenceNo));
				} else {
					parent2SatisfiedKidsList.add(kidId);
				}
			}
		}
		Collections.sort(kidsList, (m1, m2) -> m1.getValue1().compareTo(m2.getValue1()));
		LogManager.getLogger().debug(parent1Id + "-" + parent2Id + "'s kids: " +  kidsList.size());
		return kidsList.stream().map(Pair<String, Float>::getValue0).collect(Collectors.toCollection(ArrayList::new));
	}
	
	public GraphVO retrieveRoots(RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	retrieveRelationsRequestVO.setMaxDepth(Constants.EXPORT_TREE_MAX_DEPTH);
    	retrieveRelationsRequestVO.setRequiredRelationsList(Arrays.asList(Constants.RELATION_NAME_FATHER, Constants.RELATION_NAME_MOTHER));
		return retrieveTree(retrieveRelationsRequestVO);
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
    	retrieveRelationsRequestVO2.setRequiredRelationsList(Arrays.asList(Constants.RELATION_NAME_SON));
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
    		domainValueVO.setValue(domainValue.getDvValue());
    		
    		domainValueFlags.setDomainValue(domainValue);
    		domainValueVO.setIsRelationParentChild(domainValueFlags.getIsRelationParentChild());
    		domainValueVO.setIsRelationSpouse(domainValueFlags.getIsRelationSpouse());
    		domainValueVO.setIsInputAsAttribute(domainValueFlags.getIsInputAsAttribute());
    		domainValueVO.setRepetitionType(domainValueFlags.getRepetitionType());
    		domainValueVO.setAttributeDomain(domainValueFlags.getAttributeDomain());
    		domainValueVO.setIsInputMandatory(domainValueFlags.getIsInputMandatory());
    		domainValueVO.setValidationJsRegEx(domainValueFlags.getValidationJsRegEx());
    		domainValueVO.setLanguageCode(domainValueFlags.getLanguageCode());
    	}
    	
    	return domainValueVOList;
    }
    
    public RetrievePersonAttributesResponseVO retrievePersonAttributes(long entityId) {
    	Person person;
    	List<AttributeValue> attributeValueList;
    	RetrievePersonAttributesResponseVO retrievePersonAttributesResponseVO;
		String gender, firstName, personLabel, label;
		DomainValue attributeValueDv;
    	
    	retrievePersonAttributesResponseVO = new RetrievePersonAttributesResponseVO();
		person = personRepository.findByIdAndTenant(entityId, SecurityContext.getCurrentTenant())
				.orElseThrow(() -> new AppException("Invalid Person Id " + entityId, null));
		retrievePersonAttributesResponseVO.setPhoto(person.getPhoto());
		attributeValueList = person.getAttributeValueList();
		retrievePersonAttributesResponseVO.setAttributeValueVOList(attributeValuesEntityToVo(attributeValueList));
		
		// Determine Label
		gender = null;
		firstName = null;
		personLabel = null;
		for (AttributeValueVO aVVo : retrievePersonAttributesResponseVO.getAttributeValueVOList()) {
			if(aVVo.getAttributeDvId() == Constants.PERSON_ATTRIBUTE_DV_ID_GENDER) {
				attributeValueDv = domainValueRepository.findById(Long.valueOf(aVVo.getAttributeValue()))
        				.orElseThrow(() -> new AppException("Invalid Attribute Value Dv Id " + aVVo.getAttributeValue(), null));
				gender = attributeValueDv.getDvValue().substring(0,1);	// TODO: Incorrect logic (In some languages, duplicates can be there; In some languages, a single character could be made up of multiple unicodes)
			} else if(aVVo.getAttributeDvId() == Constants.PERSON_ATTRIBUTE_DV_ID_FIRST_NAME) {
				firstName = aVVo.getAvValue();
			} else if(aVVo.getAttributeDvId() == Constants.PERSON_ATTRIBUTE_DV_ID_LABEL) {
				personLabel = aVVo.getAvValue();
			}
		}
		// The following expression is to be maintained in sync with that in PersonVO.determineLabel
		label =  "(" + entityId + "/" + (gender == null ? "" : gender) + ")" + (firstName == null ? "" : firstName) +
				(firstName == null || personLabel  == null ? "" : "/") + (personLabel == null ? "" : personLabel);
		retrievePersonAttributesResponseVO.setLabel(label);
		
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
    		if (domainValueFlags.getIsInputAsAttribute()) {
        		attributeValueVO = new AttributeValueVO();
        		attributeValueVOList.add(attributeValueVO);
        		attributeValueVO.setId(attributeValue.getId());
        		attributeValueVO.setAttributeDvId(attributeValue.getAttribute().getId());
        		attributeValueVO.setAttributeName(attributeValue.getAttribute().getDvValue());
        		attributeValueVO.setAttributeValue(attributeValue.getAttributeValue());
        		attributeValueVO.setTranslatedValue(attributeValue.getTranslation() == null ? null : attributeValue.getTranslation().getValue());
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
    	if (saveAttributesRequestVO.getPhoto() != null) {	// When no fresh upload (browse & open), don't update existing photo with null
    		person.setPhoto(saveAttributesRequestVO.getPhoto());
    	}
    	
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
    	AttributeValue attributeValue, insertedAttributeValue, preModifyAttributeValue;
    	List<Long> incomingAttributeValueWithIdList, insertedAttributeValueIdList;
    	List<AttributeValue> toDeleteAttributeValueList;
    	DomainValueFlags domainValueFlags;
    	Translation translation;
    	String translatedValue;
    	DomainValue attributeDv;
    	
		toDeleteAttributeValueList = (person != null ? person.getAttributeValueList() : relation.getAttributeValueList());
		LogManager.getLogger().debug("1. Before update, no. of attributes in DB: " + (toDeleteAttributeValueList == null ? 0 : toDeleteAttributeValueList.size()));
    	incomingAttributeValueWithIdList = new ArrayList<Long>();
    	insertedAttributeValueIdList = new ArrayList<Long>();
    	domainValueFlags = new DomainValueFlags();
    	
    	for(AttributeValueVO attributeValueVO : attributeValueVOList) {
    		if (attributeValueVO.getId() == null) {
    			throw new AppException("System error: Attribute with null id", null);
    		}
    		attributeDv = domainValueRepository.findById(attributeValueVO.getAttributeDvId())
    				.orElseThrow(() -> new AppException("Invalid Attribute " + attributeValueVO.getAttributeDvId(), null));
    		domainValueFlags.setDomainValue(attributeDv);
    		if (attributeValueVO.getId() < 1) {	// Insert New AV
    			insertedAttributeValue = insertAttributeValue(attributeValueVO, person, relation);
    			insertedAttributeValueIdList.add(insertedAttributeValue.getId());
				if (attributeValueVO.getTranslatedValue() != null) {	// Insert New Translation
					translation = new Translation(insertedAttributeValue, null, attributeValueVO.getTranslatedValue());
					translationRepository.save(translation);
				}
    		}
    		else {	// Modify Existing AV
    			incomingAttributeValueWithIdList.add(attributeValueVO.getId());
    			attributeValue = attributeValueRepository.findByIdAndTenant(attributeValueVO.getId(), SecurityContext.getCurrentTenant())
    					.orElseThrow(() -> new AppException("Invalid Attribute Value Id " + attributeValueVO.getId(), null));
        		if (attributeValueVO.getAttributeValue() == null || attributeValueVO.getAttributeValue().equals("") ||
        				domainValueFlags.getIsTranslatable() && !SecurityContext.getCurrentLanguageDvId().equals(Constants.DEFAULT_LANGUAGE_DV_ID) &&
        				(attributeValueVO.getTranslatedValue() == null || attributeValueVO.getTranslatedValue().equals(""))) {
        			throw new AppException("Attribute value (and its translation, if applicable) cannot be null", null);
        		}
    			if (attributeValueVO.getAttributeDvId() != attributeValue.getAttribute().getId()) {
    				throw new AppException("Invalid input from client.", null);
    			}
    			
    			if (!Objects.equals(attributeValueVO.getAttributeValue(), attributeValue.getAttributeValue()) ||
    					!Objects.equals(attributeValueVO.isValueAccurate(), attributeValue.isValueAccurate()) ||
    					!UtilFuncs.dateEquals(attributeValueVO.getStartDate(), attributeValue.getStartDate()) ||
    					!UtilFuncs.dateEquals(attributeValueVO.getEndDate(), attributeValue.getEndDate())) {	// Modify Default-Lang
    				preModifyAttributeValue = new AttributeValue(attributeValue);
    				preModifyAttributeValue.setOverwrittenBy(attributeValue);
    				attributeValueRepository.save(preModifyAttributeValue);
    				attributeValue.setAttributeValue(attributeValueVO.getAttributeValue());
    				attributeValue.setValueAccurate(attributeValueVO.isValueAccurate());
    				attributeValue.setStartDate(attributeValueVO.getStartDate());
    				attributeValue.setEndDate(attributeValueVO.getEndDate());
    				attributeValueRepository.save(attributeValue);
    			}
    			translatedValue = attributeValue.getTranslation() == null ? null : attributeValue.getTranslation().getValue();
    			if (!Objects.equals(attributeValueVO.getTranslatedValue(), translatedValue)) {
    				if (attributeValueVO.getTranslatedValue() == null) {	// Delete Translation
    					throw new AppException("Translated Value cannot be removed", null);
    				} else if (translatedValue == null) {	// Insert New Translation
    					translation = new Translation(attributeValue, null, attributeValueVO.getTranslatedValue());
    					translationRepository.save(translation);
    				} else {	// Modify Existing Translation
    					attributeValue.getTranslation().setValue(attributeValueVO.getTranslatedValue());
    				}
    			}
    		}
    	}
    	
    	LogManager.getLogger().debug("2. Before update, no. of attributes in DB: " + (toDeleteAttributeValueList == null ? 0 : toDeleteAttributeValueList.size()));
		if (toDeleteAttributeValueList != null) {	// Delete AV
	    	for(AttributeValue toDeleteAttributeValue : toDeleteAttributeValueList) {
	    		domainValueFlags.setDomainValue(toDeleteAttributeValue.getAttribute());
	    		if (domainValueFlags.getIsInputAsAttribute() && !incomingAttributeValueWithIdList.contains(toDeleteAttributeValue.getId())) {
	    			toDeleteAttributeValue.setDeleter(SecurityContext.getCurrentUser());
	    			toDeleteAttributeValue.setDeletedAt(new Timestamp(System.currentTimeMillis()));
					attributeValueRepository.save(toDeleteAttributeValue);
					 // Soft delete of parent (AttributeValue); Children (Translation) left untouched
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

    public SearchResultsVO searchPerson(PersonSearchCriteriaVO personSearchCriteriaVO) {
    	List<AttributeValueVO> attributeValueVOList;
    	StringBuilder querySB;
    	List<Person> personList;
    	long searchResultsCount;
    	int searchResultAttributesListSize;
    	SearchResultsVO searchResultsVO;
    	Map<Long, Integer> attributeVsColumnMap;
    	List<List<String>> searchResultsList, searchResultsPostXtraFilterList, searchResultsFinal;
    	List<String> personAttributesList;
    	DomainValueFlags domainValueFlags;
    	String attrVal, parentNamesSsv, spouseNamesSsv, childNamesSsv, parentsCriteria, spousesCriteria, childrenCriteria;
    	DomainValue domainValue;
    	boolean[] nonEmptyColumnArr;
    	
    	attributeValueVOList = personSearchCriteriaVO.getAttributeValueVOList();
    	parentsCriteria = null;
    	spousesCriteria = null;
    	childrenCriteria = null;
    	domainValueFlags = new DomainValueFlags();
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
				domainValue = domainValueRepository.findById(Long.valueOf(attributeValueVO.getAttributeDvId()))
						.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + attributeValueVO.getAttributeDvId(), null));
				domainValueFlags.setDomainValue(domainValue);
				querySB.append(" AND (");
				if (Objects.equals(domainValueFlags.getValidationJsRegEx(), Constants.TRANSLATABLE_REGEX) &&
						personSearchCriteriaVO.isLenient()) {	// Beware: PostgreSQL specific syntax
					querySB.append("(");
				    for (String alternative : UtilFuncs.normaliseForSearch(attributeValueVO.getAttributeValue())) {
			    		querySB.append(" av.normalised_value LIKE '%");
			    		querySB.append(alternative);
			    		querySB.append("%' OR");
				    }
				    querySB.delete(querySB.length() - 3, querySB.length());
					querySB.append(")");
				} else {
		    		querySB.append(" LOWER(av.attribute_value) LIKE '%");
		    		querySB.append(attributeValueVO.getAttributeValue().toLowerCase());
		    		querySB.append("%'");
				}
	    		if (domainValueFlags.getIsTranslatable() && !SecurityContext.getCurrentLanguageDvId().equals(Constants.DEFAULT_LANGUAGE_DV_ID)) {
		    		querySB.append(" OR EXISTS (SELECT 1 FROM translation t WHERE t.attribute_value_fk = av.id AND LOWER(t.value) LIKE '%");	// Beware: PostgreSQL specific syntax
		    		querySB.append(attributeValueVO.getAttributeValue().toLowerCase());
		    		querySB.append("%')");
	    		}
	    		querySB.append("))");
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
    		} else if (attributeValueVO.getAttributeDvId() == -4) {
    			childrenCriteria = attributeValueVO.getAttributeValue().toLowerCase();
    		}
    	}
		querySB.append(" ORDER BY p.id;");
    	
    	personList = personRepository.executeDynamicQuery(querySB.toString());
    	
    	searchResultsVO = new SearchResultsVO();
    	if (personList.size() == 0) {
    		return searchResultsVO;
    	}
    	
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
    				if (domainValueFlags.getAttributeDomain() == null || domainValueFlags.getAttributeDomain().equals("")) {
    					attrVal = attributeValue.getAvValue();
    				}
    				else {
    					domainValue = domainValueRepository.findById(Long.valueOf(attributeValue.getAvValue()))
    							.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + attributeValue.getAvValue(), null));
    					attrVal = domainValue.getDvValue();
    				}
	    			if (attributeVsColumnMap.containsKey(attributeValue.getAttribute().getId())) {
	    				UtilFuncs.listSet(personAttributesList, attributeVsColumnMap.get(attributeValue.getAttribute().getId()), attrVal, "");
	    			}
	    			else {
	    				attributeVsColumnMap.put(attributeValue.getAttribute().getId(), attributeVsColumnMap.size() + 1);
	    				UtilFuncs.listSet(searchResultsList.get(0), attributeVsColumnMap.size(), attributeValue.getAttribute().getDvValue(), "");
	    				UtilFuncs.listSet(personAttributesList, attributeVsColumnMap.size(), attrVal, "");
	    			}
    			}
    		}
    	}
		// Add parents, spouses & children; Also apply search criteria based on them
    	searchResultsPostXtraFilterList = new ArrayList<List<String>>(searchResultsList.size());
		searchResultAttributesListSize = searchResultsList.get(0).size();
		UtilFuncs.listSet(searchResultsList.get(0), searchResultAttributesListSize, "Parents", "");
		UtilFuncs.listSet(searchResultsList.get(0), searchResultAttributesListSize + 1, "Spouses", "");
		UtilFuncs.listSet(searchResultsList.get(0), searchResultAttributesListSize + 2, "Children", "");
    	searchResultsCount = 0;
		searchResultsPostXtraFilterList.add(0, searchResultsList.get(0));
    	nonEmptyColumnArr = new boolean[searchResultsList.get(0).size()];
    	for (int ind = 1; ind < searchResultsList.size(); ind++) {
			parentNamesSsv = "";
			spouseNamesSsv = "";
			childNamesSsv = "";
    		for (Map.Entry<Person, AttributeValue> relativeAttributeEntry : retrieveRelativesAndAttributes(personList.get(ind - 1), Arrays.asList(Constants.RELATION_NAME_FATHER, Constants.RELATION_NAME_MOTHER), Arrays.asList(Constants.PERSON_ATTRIBUTE_DV_ID_FIRST_NAME))) {
    			parentNamesSsv += "/" + relativeAttributeEntry.getValue().getAvValue();
    		}
    		if (parentsCriteria != null && (parentNamesSsv.equals("") || !parentNamesSsv.toLowerCase().contains(parentsCriteria))) {
    			continue;
    		}
    		for (Map.Entry<Person, AttributeValue> relativeAttributeEntry : retrieveRelativesAndAttributes(personList.get(ind - 1), Arrays.asList(Constants.RELATION_NAME_HUSBAND, Constants.RELATION_NAME_WIFE), Arrays.asList(Constants.PERSON_ATTRIBUTE_DV_ID_FIRST_NAME))) {
    			spouseNamesSsv += "/" + relativeAttributeEntry.getValue().getAvValue();
    		}
    		if (spousesCriteria != null && (spouseNamesSsv.equals("") || !spouseNamesSsv.toLowerCase().contains(spousesCriteria))) {
    			continue;
    		}
    		for (Map.Entry<Person, AttributeValue> relativeAttributeEntry : retrieveRelativesAndAttributes(personList.get(ind - 1), Arrays.asList(Constants.RELATION_NAME_SON, Constants.RELATION_NAME_DAUGHTER), Arrays.asList(Constants.PERSON_ATTRIBUTE_DV_ID_FIRST_NAME))) {
    			childNamesSsv += "/" + relativeAttributeEntry.getValue().getAvValue();
    		}
    		if (childrenCriteria != null && (childNamesSsv.equals("") || !childNamesSsv.toLowerCase().contains(childrenCriteria))) {
    			continue;
    		}
			UtilFuncs.listSet(searchResultsList.get(ind), searchResultAttributesListSize, parentNamesSsv, "");
			UtilFuncs.listSet(searchResultsList.get(ind), searchResultAttributesListSize + 1, spouseNamesSsv, "");
			UtilFuncs.listSet(searchResultsList.get(ind), searchResultAttributesListSize + 2, childNamesSsv, "");
			searchResultsPostXtraFilterList.add(searchResultsList.get(ind));
			for (int avInd = 0; avInd < searchResultsList.get(ind).size(); avInd++) {
				if (!searchResultsList.get(ind).get(avInd).equals("")) {
					nonEmptyColumnArr[avInd] = true;
				}
			}
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
    		// Remove empty columns, caused by above code-based-filter for parents, spouses & children criteria
    		searchResultsFinal = new ArrayList<List<String>>(searchResultsPostXtraFilterList.size());
    		searchResultsVO.setResultsList(searchResultsFinal);
    		for (List<String> searchResult : searchResultsPostXtraFilterList) {
    			searchResultsFinal.add(extractListSpecifiedIndices(searchResult, nonEmptyColumnArr));
    		}
    	}
    	return searchResultsVO;
    }

    private List<String> extractListSpecifiedIndices(List<String> inList, boolean[] copyFlagsArr) {
    	List<String> outList;
    	outList = new ArrayList<String>();
    	for (int ind = 0; ind < copyFlagsArr.length; ind++) {
    		if (copyFlagsArr[ind]) {
    			outList.add(inList.get(ind));
    		}
    	}
    	return outList;
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
    	Collections.sort(relatedPerson1VOList);

    	return relatedPerson1VOList;
    }
    
    private RelatedPerson1VO getOtherPerson(Relation relation, Person forPerson) {
    	long reqdAttributeDvId1, reqdAttributeDvId2;
    	RelatedPerson1VO relatedPerson1VO;
    	
    	relatedPerson1VO = serviceParts.new RelatedPerson1VO();
    	if (relation.getPerson1().equals(forPerson)) {
    		relatedPerson1VO.person = relation.getPerson2();
    		reqdAttributeDvId1 = Constants.RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1;
    		reqdAttributeDvId2 = Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON2_FOR_PERSON1;
    	}
    	else {
    		relatedPerson1VO.person = relation.getPerson1();
    		reqdAttributeDvId1 = Constants.RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2;
    		reqdAttributeDvId2 = Constants.RELATION_ATTRIBUTE_DV_ID_SEQUENCE_OF_PERSON1_FOR_PERSON2;
    	}
    	relatedPerson1VO.seqNo = 1D;
		for (AttributeValue attributeValue : relation.getAttributeValueList()) {
			if (attributeValue.getAttribute().getId() == reqdAttributeDvId1 &&
					serviceParts.isCurrentValidAttributeValue(attributeValue)) {
				relatedPerson1VO.relationDvId = attributeValue.getAttributeValue();
			}
			if (attributeValue.getAttribute().getId() == reqdAttributeDvId2 &&
					serviceParts.isCurrentValidAttributeValue(attributeValue)) {
				relatedPerson1VO.seqNo = Double.parseDouble(attributeValue.getAttributeValue());
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

    public List<List<Object>> importPrData(Iterable<CSVRecord> csvRecords) {
    	// Two passes, one to validate and one to store into DB. This avoids gap in person/relation/attributeValue ids, caused by exception during storing.
		List<List<Object>> validationMessageList;
		List<Object> validationMessage;
		
		validationMessageList = validatePrData(csvRecords);
		if (validationMessageList.size() == 1) {
			validationMessage = new ArrayList<Object>(1);
			validationMessageList.set(0, validationMessage);
			validationMessage.add("File imported Successfully.");
			try {
				storePrData(csvRecords);
			} catch (Exception e) {
				e.printStackTrace();
				validationMessage.set(0, e.getMessage());
			}
		}
		
		return validationMessageList;
    }
    
    public List<List<Object>> validatePrData(Iterable<CSVRecord> csvRecords) {
		List<List<Object>> validationMessageList;
		List<Object> validationMessageHeader;
    	int cellCount, cellInd, lastCellInd, lastRecordLevel;
    	Iterator<String> strItr;
    	String cellContent, personAttributeValuesArr[], currentGender, previousGender;
    	long personId;
    	Optional<Person> person;
    	AttributeValue genderAv;
    	DomainValue genderAttributeDv;
		
		validationMessageList = new ArrayList<List<Object>>();
		validationMessageHeader = new ArrayList<Object>(3);
		validationMessageList.add(validationMessageHeader);
		
		validationMessageHeader.add("Row");
		validationMessageHeader.add("Column");
		validationMessageHeader.add("Error");
		
		previousGender = null;
		lastRecordLevel = -1;
    	recordLoop: for (CSVRecord csvRecord : csvRecords) {
    		cellCount = 0;
    		strItr = csvRecord.iterator();	// In this version of CSVRecord, toList() and values() are not public
    		cellInd = -1;					// and regular iterator doesn't support nextIndex()
    		lastCellInd = -1;
    		while (strItr.hasNext()) {
    			cellInd++;
    			cellContent = strItr.next();
    			if (cellContent.equals("")) {
    				continue;
    			}
				cellCount++;
				if (cellCount > 2) {
					addValidationMessage(validationMessageList, csvRecord, null, "Invalid record structure: More than two values found.");
					continue recordLoop;
				}
				
				if (lastCellInd == -1) {	// Current one is first non-null value
					if (cellInd / 2 > lastRecordLevel + 1) {
    					addValidationMessage(validationMessageList, csvRecord, null, "Invalid record structure: Atleast one parent should be given.");
    					lastRecordLevel = cellInd / 2;
    					continue recordLoop;
					}
					 // After this point, don't use lastCellInd, lastRecordLevel
					lastRecordLevel = cellInd / 2;
					lastCellInd = cellInd;
				} else {
					if (cellInd != lastCellInd + 1) {	// Should be adjacent to each other
    					addValidationMessage(validationMessageList, csvRecord, null, "Invalid record structure: Two values are not adjacent to each other.");
    					continue recordLoop;
					}
					if (cellInd % 2 == 0) {
    					addValidationMessage(validationMessageList, csvRecord, null, "Invalid record structure: First value should be in odd column (A, C, E, ...) and second one in even column (B, D, F, ...).");
    					continue recordLoop;
					}
				}
    			
    			personAttributeValuesArr = cellContent.split("#", -1);
    			if (personAttributeValuesArr.length != 2 && personAttributeValuesArr.length != 4) {
					addValidationMessage(validationMessageList, csvRecord, cellInd, "Invalid cell content: Missing or extraneous components.");
					continue recordLoop;
    			}
    			
    			if (!personAttributeValuesArr[0].equals("")) {
    		    	try {
    		    		Double.parseDouble(personAttributeValuesArr[0]);
    		    	} catch(NumberFormatException nfe) {
    					addValidationMessage(validationMessageList, csvRecord, cellInd, "Invalid cell content: Sequence no. not numeric.");
    					continue recordLoop;
    		    	}
    			}
    			
    			currentGender = null;
    			if (personAttributeValuesArr.length == 2) {
    		    	try {
    		    		personId = Long.parseLong(personAttributeValuesArr[1]);
    		    	} catch(NumberFormatException nfe) {
    					addValidationMessage(validationMessageList, csvRecord, cellInd, "Invalid cell content: Person id not numeric.");
    					continue recordLoop;
    		    	}
    	    		person = personRepository.findByIdAndTenant(personId, SecurityContext.getCurrentTenant());
    	    		if (!person.isPresent()) {
    					addValidationMessage(validationMessageList, csvRecord, cellInd, "Invalid cell content: Person doesn't exist.");
    					continue recordLoop;
    	    		}
    	    		genderAttributeDv = domainValueRepository.findById(Constants.PERSON_ATTRIBUTE_DV_ID_GENDER)
    						.orElseThrow(() -> new AppException("Invalid Attribute Dv Id " + Constants.PERSON_ATTRIBUTE_DV_ID_GENDER, null));
    				genderAv = attributeValueRepository.findByPersonAndAttribute(person.get(), genderAttributeDv)
    						.orElseThrow(() -> new AppException("Invalid gender", null));
    	    		currentGender = genderAv.getAttributeValue();
    			} else {
    	    		if (personAttributeValuesArr[1].equals("")) {
    					addValidationMessage(validationMessageList, csvRecord, cellInd, "Invalid cell content: Person name cannot be empty.");
    					continue recordLoop;
    	    		}
    	    		currentGender = personAttributeValuesArr[2];
    	    		if (!currentGender.equals("M") && !currentGender.equals("F")) {
    					addValidationMessage(validationMessageList, csvRecord, cellInd, "Invalid cell content: Unsupported Gender.");
    					continue recordLoop;
    				}
    	    		
    			}
				if (cellCount == 1) {
					previousGender = currentGender;
				} else if (currentGender.equals(previousGender)) {
					previousGender = null;
					addValidationMessage(validationMessageList, csvRecord, cellInd, "Invalid cells contents: Same Gender Spouse relation is currently unsupported.");
					continue recordLoop;
				}
    		}
    	}
		return validationMessageList;
    }

    private void addValidationMessage(List<List<Object>> validationMessageList, CSVRecord csvRecord, Integer colInd, String message) {
		List<Object> validationMessage;
		
		validationMessage = new ArrayList<Object>(3);
		validationMessageList.add(validationMessage);
		
		validationMessage.add(csvRecord.getRecordNumber());
		validationMessage.add(colInd == null ? null : colInd + 1);
		validationMessage.add(message);
    	
    }

    private void storePrData(Iterable<CSVRecord> csvRecords) {
    	/* A cell content (Person details) is either skipped (if person id) or INSERTed.
    	 * For relationship to be INSERTed, no prior relationship should exist between the two persons already.
    	 * There is no DELETE or MODIFY of Person and Relation.
    	 * Special first name DITTO with gender to handle multiple spouses Vs. Empty cell to mean details are not known
    	 * Different sequence within each parent not supported
    	 */
    	int level, cellCount, cellInd;
    	Person mainPerson, spousePerson;
    	Relation relation;
    	AttributeValue attributeValue, mainPersonGenderAv, spousePersonGenderAv;
    	DomainValue firstNamePersAttributeDv, genderPersAttributeDv, labelPersAttributeDv, person1ForPerson2RelAttributeDv, person2ForPerson1RelAttributeDv, sequenceOfPerson2ForPerson1RelAttributeDv;
    	List<Person> malePersonList, femalePersonList;
    	ParsedCellContentVO parsedCellContentVO;
    	Double withinSpouseSequenceNo, withinParentSequenceNo;
    	boolean isRelationNewlyCreated;
    	Iterator<String> strItr;
    	String cellContent;
    	
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
    		
    		cellCount = 0;
    		strItr = csvRecord.iterator();
    		cellInd = -1;
    		level = -1;
    		while (strItr.hasNext()) {
    			cellInd++;
    			cellContent = strItr.next();
    			if (cellContent.equals("")) {
    				continue;
    			}
				cellCount++;
				if (cellCount == 1) {
					level = cellInd / 2;
				}
    	    	parsedCellContentVO = cellContentsToPerson(cellContent, level, malePersonList, femalePersonList, firstNamePersAttributeDv, genderPersAttributeDv, labelPersAttributeDv);
				if (cellInd % 2 == 0) {
	    	    	mainPerson = parsedCellContentVO.person;
	    	    	withinParentSequenceNo = parsedCellContentVO.sequenceNo;
	    	    	mainPersonGenderAv = parsedCellContentVO.genderAv;
				} else {
        	    	spousePerson = parsedCellContentVO.person;
        	    	spousePersonGenderAv = parsedCellContentVO.genderAv;
				}
    		}
    		
    		if (mainPerson == null) {
    			if (spousePerson == null) {
    				continue;
    			} else {
	    			// Insert a dummy
	    			if (spousePersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE)) {
	    				cellContent = "#Unknown#F#";
	    			} else { // if (spousePersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_FEMALE))
	    				cellContent = "#Unknown#M#";
	    			}
	    			cellCount++;
	    	    	parsedCellContentVO = cellContentsToPerson(cellContent, level, malePersonList, femalePersonList, firstNamePersAttributeDv, genderPersAttributeDv, labelPersAttributeDv);
	    	    	mainPerson = parsedCellContentVO.person;
	    	    	mainPersonGenderAv = parsedCellContentVO.genderAv;
    			}
    		}

    		if (cellCount == 2) {
    	    	isRelationNewlyCreated = false;
    	    	if ((relation = relationRepository.findRelationGivenPersons(mainPerson.getId(), spousePerson.getId(), SecurityContext.getCurrentTenantId())) == null) {
    	    		isRelationNewlyCreated = true;
        			if (mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE)) {
        				relation = new Relation(mainPerson, spousePerson);
        			} else if (mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_FEMALE)) {
        				relation = new Relation(spousePerson, mainPerson);
        			}
        	    	relation = relationRepository.save(relation);
        	    	
    				attributeValue = new AttributeValue(person1ForPerson2RelAttributeDv, Constants.RELATION_NAME_HUSBAND, null, relation);
    	    		attributeValueRepository.save(attributeValue);
    	    		
    				attributeValue = new AttributeValue(person2ForPerson1RelAttributeDv, Constants.RELATION_NAME_WIFE, null, relation);
    	    		attributeValueRepository.save(attributeValue);
    	    		
    	    	}
    	    	saveSequenceNo(relation, sequenceOfPerson2ForPerson1RelAttributeDv, withinSpouseSequenceNo, isRelationNewlyCreated);
    			
    		}

    		if (mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE) ||
    				spousePerson != null &&
    				spousePersonGenderAv != null && spousePersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_FEMALE)) {
    			UtilFuncs.listSet(malePersonList, level, mainPerson, null);
    			UtilFuncs.listSet(femalePersonList, level, spousePerson, null);
    		} else if (mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_FEMALE) ||
    				spousePerson != null &&
    				spousePersonGenderAv != null && spousePersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE)) {
    			UtilFuncs.listSet(malePersonList, level, spousePerson, null);
    			UtilFuncs.listSet(femalePersonList, level, mainPerson, null);
			}
    		for (int ind = level+1; ind < malePersonList.size(); ind++) {
    			malePersonList.set(ind, null);
    			femalePersonList.set(ind, null);
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
    	AttributeValue attributeValue, genderAv;
    	ParsedCellContentVO parsedCellContentVO;
    	Double sequenceNo;
    	boolean isMale;
    	
		personAttributeValuesArr = cellContents.split("#", -1);
		
    	sequenceNo = null;
		if (!personAttributeValuesArr[0].equals("")) {
    		sequenceNo = Double.parseDouble(personAttributeValuesArr[0]);
		}
		
		if (personAttributeValuesArr.length == 2) {
    		personId = Long.parseLong(personAttributeValuesArr[1]);
    		person = personRepository.findByIdAndTenant(personId, SecurityContext.getCurrentTenant())
    				.orElseThrow(() -> new AppException("Invalid Person Id " + personAttributeValuesArr[1], null));
			genderAv = attributeValueRepository.findByPersonAndAttribute(person, genderPersAttributeDv)
					.orElseThrow(() -> new AppException("Invalid gender", null));
	    	
			isMale = false;
    		if (genderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE)) {
    			isMale = true;
			}
    		
		} else {
	    	
			isMale = false;
    		if (personAttributeValuesArr[2].equals("M")) {
    			isMale = true;
			}
    		
    		if (personAttributeValuesArr[1].equalsIgnoreCase("DITTO")) {
    			if (isMale) {
    				person = malePersonList.get(level);
    			} else {
    				person = femalePersonList.get(level);
    			}
    			genderAv = attributeValueRepository.findByPersonAndAttribute(person, genderPersAttributeDv)
    					.orElseThrow(() -> new AppException("Invalid gender", null));
    		} else {
        		person = new Person();
        		person = personRepository.save(person);
        		
	    		attributeValue = new AttributeValue(firstNamePersAttributeDv, personAttributeValuesArr[1], person, null);
	    		attributeValueRepository.save(attributeValue);
	    		
	        	genderAv = new AttributeValue(genderPersAttributeDv, isMale? Constants.GENDER_NAME_MALE : Constants.GENDER_NAME_FEMALE, person, null);
	    		attributeValueRepository.save(genderAv);
	    		
	    		if (!personAttributeValuesArr[3].equals("")) {
		    		attributeValue = new AttributeValue(labelPersAttributeDv, personAttributeValuesArr[3], person, null);
		    		attributeValueRepository.save(attributeValue);
	    		}
    		}
    	}
    	
    	parsedCellContentVO = new ParsedCellContentVO();
    	parsedCellContentVO.person = person;
    	parsedCellContentVO.sequenceNo = sequenceNo;
    	parsedCellContentVO.genderAv = genderAv;
    	return parsedCellContentVO;
    }
    
    private void establishParent(Person mainPerson, AttributeValue mainPersonGenderAv, Double sequenceNo, int level, String parentRelationName, List<Person> personList, DomainValue person1ForPerson2RelAttributeDv, DomainValue person2ForPerson1RelAttributeDv, DomainValue sequenceOfPerson2ForPerson1RelAttributeDv) {
    	AttributeValue attributeValue;
    	Relation relation;
    	boolean isRelationNewlyCreated;

    	if (level > 0 && personList.get(level - 1) != null) {
    		
    		isRelationNewlyCreated = false;
    		if ((relation = relationRepository.findRelationGivenPersons(mainPerson.getId(), personList.get(level - 1).getId(), SecurityContext.getCurrentTenantId())) == null) {
        		isRelationNewlyCreated = true;
		    	relation = new Relation(personList.get(level - 1), mainPerson);
		    	relation = relationRepository.save(relation);
		    	
				attributeValue = new AttributeValue(person1ForPerson2RelAttributeDv, parentRelationName, null, relation);
	    		attributeValueRepository.save(attributeValue);
	    		
				if (mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_MALE)) {
					attributeValue = new AttributeValue(person2ForPerson1RelAttributeDv, Constants.RELATION_NAME_SON, null, relation);
				} else { // if (mainPersonGenderAv.getAttributeValue().equals(Constants.GENDER_NAME_FEMALE))
					attributeValue = new AttributeValue(person2ForPerson1RelAttributeDv, Constants.RELATION_NAME_DAUGHTER, null, relation);
				}
	    		attributeValueRepository.save(attributeValue);
    		}
    		
	    	saveSequenceNo(relation, sequenceOfPerson2ForPerson1RelAttributeDv, sequenceNo, isRelationNewlyCreated);
    	}
    		
    }

    private void saveSequenceNo(Relation relation, DomainValue sequenceOfPerson2ForPerson1RelAttributeDv, Double sequenceNo, boolean isRelationNewlyCreated) {
    	AttributeValue attributeValue;
    	String formattedSequenceNo;
    	
		if (sequenceNo != null) {
			if (sequenceNo.doubleValue() == sequenceNo.intValue()) {
				formattedSequenceNo = String.format("%d", sequenceNo.intValue());
			} else {
				formattedSequenceNo = String.format("%.1f", sequenceNo);	// Beware - Only one digit after decimal
			}
			if (isRelationNewlyCreated) {
				attributeValue = null;
			} else {
				attributeValue = attributeValueRepository.findByRelationAndAttribute(relation, sequenceOfPerson2ForPerson1RelAttributeDv)
						.orElseGet(() -> null);
			}
			if (attributeValue == null) {
				attributeValue = new AttributeValue(sequenceOfPerson2ForPerson1RelAttributeDv, formattedSequenceNo, null, relation);
			} else {
				attributeValue.setAttributeValue(formattedSequenceNo);
			}
    		attributeValueRepository.save(attributeValue);
		}
    }
    
    // Classes that can be avoided with JavaTuples
    protected class RelatedPerson2VO {
    	Person person;
    	int level;
    	boolean isSpouse;	// Whether, during tree traversal, this node was added as a spouse of another node
    }
    
    protected class RelatedPerson3VO  implements Comparable<RelatedPerson3VO> {
    	long personId;
    	double seqNo;

    	public RelatedPerson3VO(long personId, double seqNo) {
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
    	Double sequenceNo;
    	AttributeValue genderAv;
    }
}
