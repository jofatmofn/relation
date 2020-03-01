package org.sakuram.relation.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sakuram.relation.algo.ShortestPathBreadthFirst;
import org.sakuram.relation.bean.Person;
import org.sakuram.relation.repository.PersonRepository;
import org.sakuram.relation.valueobject.RelatedPersonsVO;
import org.sakuram.relation.valueobject.GraphVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AlgoService {
	
	@Autowired
	PersonRepository personRepository;

	@Autowired
	ShortestPathBreadthFirst shortestPathBreadthFirst;
	@Autowired
	ServiceParts serviceParts;
	
	public GraphVO retrieveRelationPath(RelatedPersonsVO relatedPersonsVO) {
    	List<Person> personList;
    	Map<Long, Person> allPersonsMap;
    	LinkedList<Person> relatedPersonList;
    	
    	personList = personRepository.findAll();
    	allPersonsMap = new HashMap<Long, Person>(personList.size());
    	for (Person person : personList) {
    		allPersonsMap.put(person.getId(), person);
    	}
    	
    	relatedPersonList = shortestPathBreadthFirst.findPathBiBFS(allPersonsMap, relatedPersonsVO.getPerson1Id(), relatedPersonsVO.getPerson2Id());
    	return serviceParts.buildGraph(new HashSet<Person>(relatedPersonList), null);
	}

}
