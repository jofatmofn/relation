package org.sakuram.relation.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.sakuram.relation.service.PersonRelationService;
import org.sakuram.relation.util.AppException;
import org.sakuram.relation.valueobject.AttributeValueVO;
import org.sakuram.relation.valueobject.RetrieveRelationsRequestVO;
import org.sakuram.relation.valueobject.GraphVO;
import org.sakuram.relation.valueobject.SaveAttributesRequestVO;
import org.sakuram.relation.valueobject.SearchResultsVO;
import org.sakuram.relation.valueobject.RelatedPersonsVO;
import org.sakuram.relation.valueobject.RelationVO;
import org.sakuram.relation.valueobject.RetrieveAppStartValuesResponseVO;
import org.sakuram.relation.valueobject.RetrieveRelationAttributesResponseVO;
import org.sakuram.relation.valueobject.RetrieveRelationsBetweenRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/basic")
public class PersonRelationController {
	
	private final String SESSION_ATTRIBUTE_LOGGED_IN_USER = "loggedInUser";
	
    @Autowired
    PersonRelationService personRelationService;
    
	@Value("${relation.application.readonly}")
	boolean isAppReadOnly;
	
    @RequestMapping(value = "/retrieveRelations", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GraphVO retrieveRelations(@RequestBody RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	return personRelationService.retrieveRelations(retrieveRelationsRequestVO);
    }
    
    @RequestMapping(value = "/retrieveRelationsBetween", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RelationVO> retrieveRelationsBetween(@RequestBody RetrieveRelationsBetweenRequestVO retrieveRelationsBetweenRequestVO) {
    	return personRelationService.retrieveRelationsBetween(retrieveRelationsBetweenRequestVO);
    }
    
    @RequestMapping(value = "/retrieveTree", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GraphVO retrieveTree(@RequestBody RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	return personRelationService.retrieveTree(retrieveRelationsRequestVO);
    }
    
    @RequestMapping(value = "/retrieveAppStartValues", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public RetrieveAppStartValuesResponseVO retrieveAppStartValues() {
    	return personRelationService.retrieveAppStartValues();
    }
    
    @RequestMapping(value = "/retrievePersonAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AttributeValueVO> retrievePersonAttributes(@RequestBody long entityId) {
    	return personRelationService.retrievePersonAttributes(entityId);
    }
    
    @RequestMapping(value = "/retrieveRelationAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RetrieveRelationAttributesResponseVO retrieveRelationAttributes(@RequestBody long entityId) {
    	return personRelationService.retrieveRelationAttributes(entityId);
    }
    
    @RequestMapping(value = "/savePersonAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public long savePersonAttributes(HttpSession httpSession, @RequestBody SaveAttributesRequestVO saveAttributesRequestVO) {
    	if (isAppReadOnly) {
    		throw new AppException("Application is running in READ ONLY mode", null);
    	}
    	saveAttributesRequestVO.setCreatorId(getCreatorId(httpSession));
    	return personRelationService.savePersonAttributes(saveAttributesRequestVO);
    }
    
    @RequestMapping(value = "/saveRelationAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveRelationAttributes(HttpSession httpSession, @RequestBody SaveAttributesRequestVO saveAttributesRequestVO) {
    	if (isAppReadOnly) {
    		throw new AppException("Application is running in READ ONLY mode", null);
    	}
    	saveAttributesRequestVO.setCreatorId(getCreatorId(httpSession));
    	personRelationService.saveRelationAttributes(saveAttributesRequestVO);
    }
    
    @RequestMapping(value = "/searchPerson", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SearchResultsVO searchPerson(@RequestBody List<AttributeValueVO> attributeValueVOList) {
    	return personRelationService.searchPerson(attributeValueVOList);
    }
    
    @RequestMapping(value = "/saveRelation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public long saveRelation(HttpSession httpSession, @RequestBody RelatedPersonsVO saveRelationRequestVO) {
    	if (isAppReadOnly) {
    		throw new AppException("Application is running in READ ONLY mode", null);
    	}
    	saveRelationRequestVO.setCreatorId(getCreatorId(httpSession));
    	return personRelationService.saveRelation(saveRelationRequestVO);
    }
    
    @RequestMapping(value = "/deleteRelation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteRelation(HttpSession httpSession, @RequestBody Long relationId) {
    	if (isAppReadOnly) {
    		throw new AppException("Application is running in READ ONLY mode", null);
    	}
    	personRelationService.deleteRelation(relationId, getCreatorId(httpSession));
    }
    
    @RequestMapping(value = "/deletePerson", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deletePerson(HttpSession httpSession, @RequestBody Long personId) {
    	if (isAppReadOnly) {
    		throw new AppException("Application is running in READ ONLY mode", null);
    	}
    	personRelationService.deletePerson(personId, getCreatorId(httpSession));
    }
    
    private long getCreatorId(HttpSession httpSession) {
    	return 6L;	// TODO: After login feature, the following code is to be used
    	/* Object loggedInUserObject;
    	loggedInUserObject = httpSession.getAttribute(SESSION_ATTRIBUTE_LOGGED_IN_USER);
    	if (loggedInUserObject == null) {
    		new AppException("Log in to access this functionality", null);
    	}
    	return (long)loggedInUserObject; */
    }
}
