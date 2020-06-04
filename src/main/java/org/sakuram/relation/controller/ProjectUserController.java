package org.sakuram.relation.controller;

import javax.servlet.http.HttpSession;

import org.sakuram.relation.service.ProjectUserService;
import org.sakuram.relation.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projectuser")
public class ProjectUserController {

    @Autowired
    ProjectUserService projectUserService;
    
    @RequestMapping(value = "/switchProject", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void switchProject(HttpSession httpSession, @RequestBody String projectId) {
    	httpSession.removeAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID);
    	httpSession.setAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID, projectUserService.switchProject(projectId));
    	return;
    }
    
}
