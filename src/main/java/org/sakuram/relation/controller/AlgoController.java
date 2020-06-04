package org.sakuram.relation.controller;

import javax.servlet.http.HttpSession;

import org.sakuram.relation.service.AlgoService;
import org.sakuram.relation.valueobject.RelatedPersonsVO;
import org.sakuram.relation.valueobject.GraphVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/algo")
public class AlgoController {

    @Autowired
    AlgoService algoService;
    
    @RequestMapping(value = "/retrieveRelationPath", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GraphVO retrieveRelationPath(HttpSession httpSession, @RequestBody RelatedPersonsVO relatedPersonsVO) {
    	return algoService.retrieveRelationPath(relatedPersonsVO);
    }
    
}
