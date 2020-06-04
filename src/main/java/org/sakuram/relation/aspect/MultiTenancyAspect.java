package org.sakuram.relation.aspect;

import javax.servlet.http.HttpSession;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.sakuram.relation.repository.CustomRepositoryImpl;
import org.sakuram.relation.util.AppException;
import org.sakuram.relation.util.Constants;
import org.sakuram.relation.util.TenantContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MultiTenancyAspect {
	@Before("target(org.sakuram.relation.repository.CustomRepositoryImpl)")
	public void activateTenantFilter(JoinPoint joinPoint) throws Throwable {
	     Long tenantId = TenantContext.getCurrentTenant();
	     if (tenantId != null) {
	          Session session = ((CustomRepositoryImpl)(joinPoint.getTarget())).getSession();
	          Filter filter = session.enableFilter("tenantFilter");
	          filter.setParameter("tenantId", tenantId);
	     }
	}
	
	@Around("(execution(* org.sakuram.relation.controller.PersonRelationController.*(..)) || execution(* org.sakuram.relation.controller.AlgoController.*(..))) && args(httpSession,..)")
	public Object identifyTenant(ProceedingJoinPoint proceedingJoinPoint, HttpSession httpSession) throws Throwable {
		Object returnValueObject;
		if (httpSession == null || httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID) == null) {
	    	 throw new AppException("Establish Project before using the system", null);
	     }
	     org.sakuram.relation.util.TenantContext.setCurrentTenant((Long)(httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_PROJECT_SURROGATE_ID)));
	     returnValueObject = proceedingJoinPoint.proceed();
	     org.sakuram.relation.util.TenantContext.setCurrentTenant(null);
	     return returnValueObject;
	}
}
