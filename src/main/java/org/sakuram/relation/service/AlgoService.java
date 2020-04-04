package org.sakuram.relation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakuram.relation.algo.ShortestPathBreadthFirst;
import org.sakuram.relation.bean.Person;
import org.sakuram.relation.bean.Relation;
import org.sakuram.relation.repository.PersonRepository;
import org.sakuram.relation.repository.RelationRepository;
import org.sakuram.relation.valueobject.RelatedPersonsVO;
import org.sakuram.relation.valueobject.RelationVO;
import org.sakuram.relation.valueobject.GraphVO;
import org.sakuram.relation.valueobject.PersonVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AlgoService {
	
	@Autowired
	PersonRepository personRepository;
	@Autowired
	RelationRepository relationRepository;

	@Autowired
	ShortestPathBreadthFirst shortestPathBreadthFirst;
	@Autowired
	ServiceParts serviceParts;
	
	public GraphVO retrieveRelationPath(RelatedPersonsVO relatedPersonsVO) {
    	List<Person> personList;
    	Map<Long, Person> allPersonsMap;
    	LinkedList<Person> relatedPersonList;
    	Set<Person> relatedPersonSet;
    	
    	GraphVO retrieveRelationsResponseVO;
    	Map<Long, PersonVO> personVOMap;
    	List<Relation> relationList;
    	List<RelationVO> relationVOList;
    	PersonVO personVO;
    	    	
    	personList = personRepository.findAll();
    	allPersonsMap = new HashMap<Long, Person>(personList.size());
    	for (Person person : personList) {
    		allPersonsMap.put(person.getId(), person);
    	}
    	
    	relatedPersonList = shortestPathBreadthFirst.findPathBiBFS(allPersonsMap, relatedPersonsVO.getPerson1Id(), relatedPersonsVO.getPerson2Id());
    	relatedPersonSet = new HashSet<Person>(relatedPersonList);
    	
    	retrieveRelationsResponseVO = new GraphVO();
    	personVOMap = new HashMap<Long, PersonVO>();
    	relationVOList = new ArrayList<RelationVO>();
    	retrieveRelationsResponseVO.setEdges(relationVOList);
    	relationList = new ArrayList<Relation>();
    	
    	for (Person person : relatedPersonSet) {
    		personVO = serviceParts.addToPersonVOMap(personVOMap, person);
    		personVO.setX(Math.random() * 100);
    		personVO.setY(Math.random() * 100);
    	}
    	
    	relationList = relationRepository.findByPerson1InAndPerson2In(relatedPersonSet, relatedPersonSet);
    	for (Relation relation : relationList) {
    		serviceParts.addToRelationVOList(relationVOList, relation, null);
    	}
    	
    	retrieveRelationsResponseVO.setNodes(new ArrayList<PersonVO>(personVOMap.values()));
    	return retrieveRelationsResponseVO;
	}

}
