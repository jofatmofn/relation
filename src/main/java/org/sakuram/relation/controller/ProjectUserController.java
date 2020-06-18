package org.sakuram.relation.controller;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.sakuram.relation.bean.Tenant;
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
    public boolean switchProject(HttpSession httpSession, @RequestBody String projectId) {
    	httpSession.removeAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID);
    	httpSession.setAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID, projectUserService.switchProject(projectId));
    	return projectUserService.isAppReadOnly(
    			(Long)httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID),
    			(Long)httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID));
    }
    
    @RequestMapping(value = "/preLogout", method = RequestMethod.POST)
    public void preLogout(HttpSession httpSession, @AuthenticationPrincipal OAuth2User principal) {
    	httpSession.removeAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID);
		LogManager.getLogger().info("Logout: " + principal.getAttribute("name"));
    	return;
    }
    
    @RequestMapping(value = "/postLogin", method = RequestMethod.POST)
    public boolean postLogin(HttpSession httpSession, @AuthenticationPrincipal OAuth2User principal) {
    	long appUserId;
		if (principal != null && httpSession != null && httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID) == null) {
			appUserId = projectUserService.findOrSaveUser(principal.getAttribute("iss").toString(), principal.getAttribute("sub"), principal.getAttribute("email"));
			httpSession.setAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID, appUserId);
			LogManager.getLogger().info("Fresh Login: " + principal.getAttribute("name") + " / " + appUserId);
		}
		else if (principal != null) {
			LogManager.getLogger().info("Earlier Login: " + principal.getAttribute("name"));
		}
		else {
			LogManager.getLogger().info("Without Login");
		}
    	return projectUserService.isAppReadOnly(
    			(Long)httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID),
    			(Long)httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID));
    }
    
    @RequestMapping(value = "/createProject", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createProject(HttpSession httpSession, @RequestBody String projectName) {
    	Tenant tenant;
    	Long appUserId;
    	
    	appUserId = (Long)httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID);
    	tenant = projectUserService.createProject(projectName, appUserId);
    	httpSession.setAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID, tenant.getId());
    	return tenant.getProjectId();
    }
    
}
