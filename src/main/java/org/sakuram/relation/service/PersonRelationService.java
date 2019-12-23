package org.sakuram.relation.service;

import java.util.ArrayList;
import java.util.List;

import org.sakuram.relation.bean.AttributeValue;
import org.sakuram.relation.bean.Person;
import org.sakuram.relation.bean.Relation;
import org.sakuram.relation.repository.PersonRepository;
import org.sakuram.relation.repository.RelationRepository;
import org.sakuram.relation.valueobject.PersonVO;
import org.sakuram.relation.valueobject.RelationVO;
import org.sakuram.relation.valueobject.RetrieveRelationsRequestVO;
import org.sakuram.relation.valueobject.RetrieveRelationsResponseVO;
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
	
	public RetrieveRelationsResponseVO retrieveRelations(RetrieveRelationsRequestVO RetrieveRelationsRequestVO) {
    	RetrieveRelationsResponseVO retrieveRelationsResponseVO;
    	List<Person> personList;
    	List<PersonVO> personVOList;
    	PersonVO personVO;
    	List<Relation> relationList;
    	List<RelationVO> relationVOList;
    	RelationVO relationVO;
    	
    	retrieveRelationsResponseVO = new RetrieveRelationsResponseVO();
    	personVOList = new ArrayList<PersonVO>();
    	retrieveRelationsResponseVO.setNodes(personVOList);
    	relationVOList = new ArrayList<RelationVO>();
    	retrieveRelationsResponseVO.setEdges(relationVOList);
    	
    	personList = personRepository.findAll();
    	for (Person person : personList) {
    		personVO = new PersonVO();
    		personVOList.add(personVO);
    		
    		personVO.setId("n" + person.getId());
    		personVO.setSize("5.0");
    		personVO.setColor("rgb(1,179,255)");
    		personVO.setX(Math.random());
    		personVO.setY(Math.random());
    		for (AttributeValue attributeValue : person.getAttributeValueList()) {
    			personVO.setAttribute(attributeValue.getAttribute().getValue(), attributeValue.getAttributeValue());
    		}
    	}
    	
    	relationList = relationRepository.findAll();
    	for (Relation relation : relationList) {
    		relationVO = new RelationVO();
    		relationVOList.add(relationVO);
    		
    		relationVO.setId("e" + relation.getId());
    		relationVO.setSource("n" + relation.getPerson1().getId());
    		relationVO.setTarget("n" + relation.getPerson2().getId());
    		for (AttributeValue attributeValue : relation.getAttributeValueList()) {
    			relationVO.setAttribute(attributeValue.getAttribute().getValue(), attributeValue.getAttributeValue());
    		}
    	}
    	
    	return retrieveRelationsResponseVO;
    }
}
