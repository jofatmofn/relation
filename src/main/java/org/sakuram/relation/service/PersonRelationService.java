package org.sakuram.relation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakuram.relation.bean.AttributeValue;
import org.sakuram.relation.bean.DomainValue;
import org.sakuram.relation.bean.Person;
import org.sakuram.relation.bean.Relation;
import org.sakuram.relation.repository.DomainValueRepository;
import org.sakuram.relation.repository.PersonRepository;
import org.sakuram.relation.repository.RelationRepository;
import org.sakuram.relation.util.Constants;
import org.sakuram.relation.valueobject.DomainValueVO;
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
	@Autowired
	DomainValueRepository domainValueRepository;
	
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
	
    public Map<String, List<DomainValueVO>> retrieveDomainValues() {
    	DomainValueVO domainValueVO;
    	List<DomainValueVO> domainValueVOList;
    	List<DomainValue> domainValueList;
    	String flagsArr[];
    	Map<String, List<DomainValueVO>> categoryWiseDomainValueVOListMap;
    	
    	domainValueList = domainValueRepository.findAll();
    	
    	categoryWiseDomainValueVOListMap = new HashMap<String, List<DomainValueVO>>();
    	for (DomainValue domainValue : domainValueList) {
    		if (categoryWiseDomainValueVOListMap.containsKey(domainValue.getCategory())) {
    			domainValueVOList = categoryWiseDomainValueVOListMap.get(domainValue.getCategory());
    		}
    		else {
    			domainValueVOList = new ArrayList<DomainValueVO>();
    			categoryWiseDomainValueVOListMap.put(domainValue.getCategory(), domainValueVOList);
    		}
    		domainValueVO = new DomainValueVO();
    		domainValueVOList.add(domainValueVO);
    		
    		domainValueVO.setId(domainValue.getId());
    		domainValueVO.setCategory(domainValue.getCategory()); // Redundant
    		domainValueVO.setValue(domainValue.getValue());
    		
    		domainValueVO.setAttributeDomain("");
    		if (domainValue.getFlagsCsv() != null && !domainValue.getFlagsCsv().equals("")) {
    			flagsArr = domainValue.getFlagsCsv().split(Constants.CSV_SEPARATOR);
    		}
    		else {
    			flagsArr = new String[0];
    		}
    		if (domainValue.getCategory().equals(Constants.CATEGORY_RELATION_NAME) || domainValue.getCategory().equals(Constants.CATEGORY_RELATION_SUB_TYPE)) {
    			if (flagsArr.length > Constants.FLAG_POSITION_RELATION_TYPE && flagsArr[Constants.FLAG_POSITION_RELATION_TYPE].equals(Constants.FLAG_RELATION_TYPE_PARENT_CHILD)) {
    				domainValueVO.setRelationParentChild(true);
    			}
    			else {
    				domainValueVO.setRelationParentChild(false);
    			}
    			if (flagsArr.length > Constants.FLAG_POSITION_RELATION_TYPE && flagsArr[Constants.FLAG_POSITION_RELATION_TYPE].equals(Constants.FLAG_RELATION_TYPE_SPOUSE)) {
    				domainValueVO.setRelationSpouse(true);
    			}
    			else {
    				domainValueVO.setRelationSpouse(false);
    			}
    		}
    		if (domainValue.getCategory().equals(Constants.CATEGORY_PERSON_ATTRIBUTE) || domainValue.getCategory().equals(Constants.CATEGORY_RELATION_ATTRIBUTE)) {
    			if (flagsArr.length > Constants.FLAG_POSITION_ATTRIBUTE_DOMAIN) {
    				domainValueVO.setAttributeDomain(flagsArr[Constants.FLAG_POSITION_ATTRIBUTE_DOMAIN]);    				
    			}
    		}
    	}
    	
    	return categoryWiseDomainValueVOListMap;
    }
}
