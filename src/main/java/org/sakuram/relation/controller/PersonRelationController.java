package org.sakuram.relation.controller;

import java.util.List;
import org.sakuram.relation.service.PersonRelationService;
import org.sakuram.relation.util.Constants;
import org.sakuram.relation.valueobject.AttributeValueVO;
import org.sakuram.relation.valueobject.DomainValueVO;
import org.sakuram.relation.valueobject.RetrieveRelationsRequestVO;
import org.sakuram.relation.valueobject.RetrieveRelationsResponseVO;
import org.sakuram.relation.valueobject.SaveAttributesRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
    public RetrieveRelationsResponseVO retrieveRelations(@RequestBody RetrieveRelationsRequestVO retrieveRelationsRequestVO) {
    	return personRelationService.retrieveRelations(retrieveRelationsRequestVO);
    }
    
    @RequestMapping(value = "/retrieveDomainValues", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DomainValueVO> retrieveDomainValues() {
    	return personRelationService.retrieveDomainValues();
    }
    
    @RequestMapping(value = "/retrievePersonAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AttributeValueVO> retrievePersonAttributes(@RequestBody long entityId) {
    	return personRelationService.retrieveAttributes(Constants.ENTITY_TYPE_PERSON, entityId);
    }
    
    @RequestMapping(value = "/retrieveRelationAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AttributeValueVO> retrieveRelationAttributes(@RequestBody long entityId) {
    	return personRelationService.retrieveAttributes(Constants.ENTITY_TYPE_RELATION, entityId);
    }
    
    @RequestMapping(value = "/savePersonAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void savePersonAttributes(@RequestBody SaveAttributesRequestVO saveAttributesRequestVO) {
    	personRelationService.savePersonAttributes(saveAttributesRequestVO);
    }
    
    @RequestMapping(value = "/saveRelationAttributes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveRelationAttributes(@RequestBody SaveAttributesRequestVO saveAttributesRequestVO) {
    	personRelationService.saveRelationAttributes(saveAttributesRequestVO);
    }
    
}
