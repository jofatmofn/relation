package org.sakuram.relation.aspect;

import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.sakuram.relation.util.AppException;
import org.sakuram.relation.util.Constants;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAspect {
	
	@Around("(execution(* org.sakuram.relation.controller.PersonRelationController.*(..)) || execution(* org.sakuram.relation.controller.AlgoController.*(..))) && args(httpSession,..)")
	public Object identifyTenant(ProceedingJoinPoint proceedingJoinPoint, HttpSession httpSession) throws Throwable {
		Object returnValueObject;
		if (httpSession == null || httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID) == null) {
	    	 throw new AppException("Establish Project before using the system", null);
	    }
		org.sakuram.relation.util.SecurityContext.setCurrentTenant((Long)(httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID)));
		returnValueObject = proceedingJoinPoint.proceed();
		org.sakuram.relation.util.SecurityContext.setCurrentTenant(null);
		return returnValueObject;
	}

	@Around("(execution(* org.sakuram.relation.controller.PersonRelationController.save*(..)) || execution(* org.sakuram.relation.controller.AlgoController.delete*(..))) && args(httpSession,..)")
	public Object identifyUser(ProceedingJoinPoint proceedingJoinPoint, HttpSession httpSession) throws Throwable {
		Object returnValueObject;
		
		if (httpSession == null || httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID) == null) {
			throw new AppException("Establish yourself by logging-in to the system", null);
		}
		org.sakuram.relation.util.SecurityContext.setCurrentUser((Long)(httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_USER_SURROGATE_ID)));
		returnValueObject = proceedingJoinPoint.proceed();
		org.sakuram.relation.util.SecurityContext.setCurrentUser(null);
		return returnValueObject;
	}

}
