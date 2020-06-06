package org.sakuram.relation.controller;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.sakuram.relation.service.ProjectUserService;
import org.sakuram.relation.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    
    @RequestMapping(value = "/preLogout", method = RequestMethod.POST)
    public void preLogout(HttpSession httpSession, @AuthenticationPrincipal OAuth2User principal) {
    	httpSession.removeAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID);
		LogManager.getLogger().info("Logout: " + principal.getAttribute("name"));
    	return;
    }
    
    @RequestMapping(value = "/postLogin", method = RequestMethod.POST)
    public void postLogin(HttpSession httpSession, @AuthenticationPrincipal OAuth2User principal) {
    	long userId;
		if (principal != null && httpSession != null && httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID) == null) {
			userId = projectUserService.findOrSaveUser(principal.getAttribute("sub"), principal.getAttribute("iss").toString());
			httpSession.setAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID, userId);
			LogManager.getLogger().info("Login: " + principal.getAttribute("name") + " / " + userId);
		}
    	return;
    }
    
}
