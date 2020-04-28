package org.sakuram.relation.service;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.sakuram.relation.util.PatternBasedXY;
import org.sakuram.relation.util.PatternBasedXY.XY;
import org.sakuram.relation.valueobject.RelatedPersonsVO;
import org.sakuram.relation.valueobject.RelationVO;
import org.sakuram.relation.valueobject.GraphVO;
import org.sakuram.relation.valueobject.PersonVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	@Autowired
	PatternBasedXY patternBasedXY;
	
	@Value("${relation.application.ascertain.relation.graph.pattern}")
	String ASCERTAIN_RELATION_GRAPH_PATTERN;
	
	public GraphVO retrieveRelationPath(RelatedPersonsVO relatedPersonsVO) {
    	List<Person> personList;
    	Map<Long, Person> allPersonsMap;
    	LinkedList<Person> relatedPersonList;
    	Set<Person> relatedPersonSet;
    	List<String> excludeRelationIdList;
    	XY xy;
    	
    	GraphVO retrieveRelationsResponseVO;
    	Map<Long, PersonVO> personVOMap;
    	List<Relation> relationList;
    	List<RelationVO> relationVOList;
    	PersonVO personVO;
    	
    	excludeRelationIdList = Arrays.asList(relatedPersonsVO.getExcludeRelationIdCsv().split(", *"));
    	personList = personRepository.findAll();
    	allPersonsMap = new HashMap<Long, Person>(personList.size());
    	for (Person person : personList) {
    		allPersonsMap.put(person.getId(), person);
    	}
    	
    	relatedPersonList = shortestPathBreadthFirst.findPathBiBFS(allPersonsMap, relatedPersonsVO.getPerson1Id(), relatedPersonsVO.getPerson2Id(), excludeRelationIdList);
    	relatedPersonSet = new HashSet<Person>(relatedPersonList);
    	
    	retrieveRelationsResponseVO = new GraphVO();
    	personVOMap = new HashMap<Long, PersonVO>();
    	relationVOList = new ArrayList<RelationVO>();
    	retrieveRelationsResponseVO.setEdges(relationVOList);
    	relationList = new ArrayList<Relation>();
    	
    	patternBasedXY.init(ASCERTAIN_RELATION_GRAPH_PATTERN, 10, 10, 20);
    	for (Person person : relatedPersonList) {
    		personVO = serviceParts.addToPersonVOMap(personVOMap, person);
    		xy = patternBasedXY.getNextXY();
    		personVO.setX(xy.x);
    		personVO.setY(xy.y);
    	}
    	
    	relationList = relationRepository.findByPerson1InAndPerson2In(relatedPersonSet, relatedPersonSet);
    	for (Relation relation : relationList) {
    		if (!excludeRelationIdList.contains(String.valueOf(relation.getId()))) {
    			serviceParts.addToRelationVOList(relationVOList, relation, null, true);
    		}
    	}
    	
    	retrieveRelationsResponseVO.setNodes(new ArrayList<PersonVO>(personVOMap.values()));
    	return retrieveRelationsResponseVO;
	}

}
