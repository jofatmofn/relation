package org.sakuram.relation.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.sakuram.relation.service.PersonRelationService;
import org.sakuram.relation.util.Constants;
import org.sakuram.relation.valueobject.AttributeValueVO;
import org.sakuram.relation.valueobject.RetrieveRelationsRequestVO;
import org.sakuram.relation.valueobject.GraphVO;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/basic")
public class PersonRelationController {
	
    @Autowired
    PersonRelationService personRelationService;
    
    @RequestMapping(value = "/retrieveRelations", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GraphVO retrieveRelations(HttpSession httpSession, @RequestBody RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	return personRelationService.retrieveRelations(retrieveRelationsRequestVO);
    }
    
    @RequestMapping(value = "/retrieveRelationsBetween", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RelationVO> retrieveRelationsBetween(HttpSession httpSession, @RequestBody RetrieveRelationsBetweenRequestVO retrieveRelationsBetweenRequestVO) {
    	return personRelationService.retrieveRelationsBetween(retrieveRelationsBetweenRequestVO);
    }
    
    @RequestMapping(value = "/retrieveTree", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GraphVO retrieveTree(HttpSession httpSession, @RequestBody RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	return personRelationService.retrieveTree(retrieveRelationsRequestVO);
    }
    
    @RequestMapping(value = "/exportTree", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportTree(HttpSession httpSession, HttpServletResponse response, @RequestBody RetrieveRelationsRequestVO retrieveRelationsRequestVO) throws IOException {
    	List<List<Object>> recordList;
    	
    	recordList = personRelationService.exportTree(retrieveRelationsRequestVO);
    	try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT)) {
    		for (List<Object> record : recordList) {
    			csvPrinter.printRecord(record);
    		}
    	}
    	response.setContentType("text/csv");
    	response.setHeader("Content-Disposition", "attachment; filename=\"fulltree.csv\"");
    }
    
    @RequestMapping(value = "/retrieveParceners", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GraphVO retrieveParceners(HttpSession httpSession, @RequestBody RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	return personRelationService.retrieveParceners(retrieveRelationsRequestVO);
    }
    
    @RequestMapping(value = "/retrieveAppStartValues", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public RetrieveAppStartValuesResponseVO retrieveAppStartValues(HttpSession httpSession, @AuthenticationPrincipal OAuth2User principal) {
    	RetrieveAppStartValuesResponseVO retrieveAppStartValuesResponseVO;
    	
    	retrieveAppStartValuesResponseVO = personRelationService.retrieveAppStartValues((Long) httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID));
    	if (principal != null) {
    		retrieveAppStartValuesResponseVO.setLoggedInUser(principal.getAttribute("name") + " (" + principal.getAttribute("email") + ")");
    	}
    	return retrieveAppStartValuesResponseVO;
    }
    
    @RequestMapping(value = "/retrievePersonAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RetrievePersonAttributesResponseVO retrievePersonAttributes(HttpSession httpSession, @RequestBody long entityId) {
    	return personRelationService.retrievePersonAttributes(entityId);
    }
    
    @RequestMapping(value = "/retrieveRelationAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RetrieveRelationAttributesResponseVO retrieveRelationAttributes(HttpSession httpSession, @RequestBody long entityId) {
    	return personRelationService.retrieveRelationAttributes(entityId);
    }
    
    @RequestMapping(value = "/savePersonAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SaveAttributesResponseVO savePersonAttributes(HttpSession httpSession, @RequestBody SaveAttributesRequestVO saveAttributesRequestVO) {
    	return personRelationService.savePersonAttributes(saveAttributesRequestVO);
    }
    
    @RequestMapping(value = "/saveRelationAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SaveAttributesResponseVO saveRelationAttributes(HttpSession httpSession, @RequestBody SaveAttributesRequestVO saveAttributesRequestVO) {
    	return personRelationService.saveRelationAttributes(saveAttributesRequestVO);
    }
    
    @RequestMapping(value = "/searchPerson", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SearchResultsVO searchPerson(HttpSession httpSession, @RequestBody List<AttributeValueVO> attributeValueVOList) {
    	return personRelationService.searchPerson(attributeValueVOList);
    }
    
    @RequestMapping(value = "/saveRelation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public long saveRelation(HttpSession httpSession, @RequestBody RelatedPersonsVO saveRelationRequestVO) {
    	return personRelationService.saveRelation(saveRelationRequestVO);
    }
    
    @RequestMapping(value = "/deleteRelation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteRelation(HttpSession httpSession, @RequestBody Long relationId) {
    	personRelationService.deleteRelation(relationId);
    }
    
    @RequestMapping(value = "/deletePerson", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deletePerson(HttpSession httpSession, @RequestBody Long personId) {
    	personRelationService.deletePerson(personId);
    }
    
}
