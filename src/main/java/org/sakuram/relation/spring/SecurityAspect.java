package org.sakuram.relation.spring;

import java.util.stream.Stream;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.sakuram.relation.bean.AppUser;
import org.sakuram.relation.bean.DomainValue;
import org.sakuram.relation.bean.Tenant;
import org.sakuram.relation.repository.AppUserRepository;
import org.sakuram.relation.repository.DomainValueRepository;
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
	@Autowired
	DomainValueRepository domainValueRepository;
	
	@Around("(((execution(* org.sakuram.relation.controller.PersonRelationController.*(..))) && (!execution(* org.sakuram.relation.controller.PersonRelationController.retrieveAppStartValues(..)))) || execution(* org.sakuram.relation.controller.AlgoController.*(..))) && args(httpSession,..)")
	public Object identifyTenant(ProceedingJoinPoint proceedingJoinPoint, HttpSession httpSession) throws Throwable {
		Object returnValueObject;
		Long tenantId;
    	Tenant tenant;
	
		LogManager.getLogger().debug("identifyTenant");
		if (httpSession == null || httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID) == null) {
	    	 throw new AppException("Establish Project before using the system", null);
	    }
		tenantId = (Long)(httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID));
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

	@Around("(execution(* org.sakuram.relation.controller.*.*(..)) && args(httpSession,..))")
	public Object identifyUser(ProceedingJoinPoint proceedingJoinPoint, HttpSession httpSession) throws Throwable {
		Object returnValueObject;
    	Long appUserId;
    	AppUser appUser;
    	boolean isWriteAccessRequired;
    	MethodSignature callingMethodSignature;
		
    	callingMethodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
    	isWriteAccessRequired = Stream.of("create", "save", "delete", "import").anyMatch(s -> callingMethodSignature.getMethod().getName().startsWith(s));
		if (isWriteAccessRequired && (httpSession == null || httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID) == null)) {
			throw new AppException("Establish yourself by logging-in to the system", null);
		}
		if (httpSession != null && httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID) != null) {
			appUserId = (Long)(httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID));
			SecurityContext.setCurrentUserId(appUserId);
	    	appUser = appUserRepository.findById(appUserId)
	    			.orElseThrow(() -> new AppException("Invalid User Id " + appUserId, null));
	    	SecurityContext.setCurrentUser(appUser);
			LogManager.getLogger().debug("User for PersonRelationController: " + appUserId);
			if (isWriteAccessRequired && projectUserService.isAppReadOnly(SecurityContext.getCurrentTenant(), appUser) &&
					!callingMethodSignature.getDeclaringTypeName().endsWith("PersonRelationController") &&
					!callingMethodSignature.getMethod().getName().equals("savePersonAttributes")) {
				throw new AppException("Hacking attempt. You do not have privilege to perform this operation", null);
			}
		}
		
		returnValueObject = proceedingJoinPoint.proceed();
		
		SecurityContext.setCurrentUserId(null);
		SecurityContext.setCurrentUser(null);
		return returnValueObject;
	}

	@Around("(execution(* org.sakuram.relation.controller.PersonRelationController.*(..)) || execution(* org.sakuram.relation.controller.AlgoController.retrieveRelationPath(..))) && args(httpSession,..)")
	public Object identifyLanguage(ProceedingJoinPoint proceedingJoinPoint, HttpSession httpSession) throws Throwable {
		Object returnValueObject;
    	Long languageDvId;
    	DomainValue languageDv;
		
		if (httpSession == null || httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_LANGUAGE_DV_ID) == null) {
			languageDvId = Constants.DEFAULT_LANGUAGE_DV_ID;
		} else {
			languageDvId = (Long)(httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_LANGUAGE_DV_ID));
		}
		SecurityContext.setCurrentLanguageDvId(languageDvId);
		languageDv = domainValueRepository.findById(languageDvId)
    			.orElseThrow(() -> new AppException("Invalid Language Id " + languageDvId, null));
    	SecurityContext.setCurrentLanguageDv(languageDv);
		LogManager.getLogger().debug("Established Language: " + languageDvId);
		
		returnValueObject = proceedingJoinPoint.proceed();
		
		SecurityContext.setCurrentLanguageDvId(null);
		SecurityContext.setCurrentLanguageDv(null);
		return returnValueObject;
	}
	
}
