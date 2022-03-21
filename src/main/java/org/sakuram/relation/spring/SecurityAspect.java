package org.sakuram.relation.spring;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.sakuram.relation.bean.AppUser;
import org.sakuram.relation.bean.Tenant;
import org.sakuram.relation.repository.AppUserRepository;
import org.sakuram.relation.repository.TenantRepository;
import org.sakuram.relation.service.ProjectUserService;
import org.sakuram.relation.util.AppException;
import org.sakuram.relation.util.Constants;
import org.sakuram.relation.util.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAspect {
	
	@Autowired
	ProjectUserService projectUserService;
	@Autowired
	AppUserRepository appUserRepository;
	@Autowired
	TenantRepository tenantRepository;
	
	@Around("(((execution(* org.sakuram.relation.controller.PersonRelationController.*(..))) && (!execution(* org.sakuram.relation.controller.PersonRelationController.retrieveAppStartValues(..)))) || execution(* org.sakuram.relation.controller.AlgoController.*(..))) && args(httpSession,..)")
	public Object identifyTenant(ProceedingJoinPoint proceedingJoinPoint, HttpSession httpSession) throws Throwable {
		Object returnValueObject;
		Long tenantId;
    	Tenant tenant;
	
		LogManager.getLogger().debug("identifyTenant");
		tenantId = (Long)(httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID));
		if (httpSession == null || tenantId == null) {
	    	 throw new AppException("Establish Project before using the system", null);
	    }
		SecurityContext.setCurrentTenantId(tenantId);
    	tenant = tenantRepository.findById(tenantId)
    			.orElseThrow(() -> new AppException("Invalid Tenant Id " + tenantId, null));
    	SecurityContext.setCurrentTenant(tenant);
		LogManager.getLogger().debug("Established Project: " + tenantId);
		
		returnValueObject = proceedingJoinPoint.proceed();
		
		SecurityContext.setCurrentTenantId(null);
		SecurityContext.setCurrentTenant(null);
		return returnValueObject;
	}

	@Around("(execution(* org.sakuram.relation.controller.*Controller.save*(..)) || execution(* org.sakuram.relation.controller.*Controller.delete*(..)) || execution(* org.sakuram.relation.controller.*Controller.import*(..))) && args(httpSession,..)")
	public Object identifyUser(ProceedingJoinPoint proceedingJoinPoint, HttpSession httpSession) throws Throwable {
		Object returnValueObject;
    	Long appUserId;
    	AppUser appUser;
		
		appUserId = (Long)(httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID));
		if (httpSession == null || appUserId == null) {
			throw new AppException("Establish yourself by logging-in to the system", null);
		}
		SecurityContext.setCurrentUserId(appUserId);
    	appUser = appUserRepository.findById(appUserId)
    			.orElseThrow(() -> new AppException("Invalid User Id " + appUserId, null));
    	SecurityContext.setCurrentUser(appUser);
		LogManager.getLogger().debug("Established User: " + appUserId);
		if (projectUserService.isAppReadOnly(SecurityContext.getCurrentTenant(), appUser)) {
			throw new AppException("Hacking attempt. You do not have privilege to perform this operation", null);
		}
		
		returnValueObject = proceedingJoinPoint.proceed();
		
		SecurityContext.setCurrentUserId(null);
		SecurityContext.setCurrentUser(null);
		return returnValueObject;
	}

}
