package org.sakuram.relation.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.sakuram.relation.repository.CustomRepositoryImpl;
import org.sakuram.relation.util.SecurityContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MultiTenancyAspect {
	
	@Before("target(org.sakuram.relation.repository.CustomRepositoryImpl)")
	public void activateTenantFilter(JoinPoint joinPoint) throws Throwable {
	     Long tenantId = SecurityContext.getCurrentTenant();
	     if (tenantId != null) {
	          Session session = ((CustomRepositoryImpl)(joinPoint.getTarget())).getSession();
	          Filter filter = session.enableFilter("tenantFilter");
	          filter.setParameter("tenantId", tenantId);
	     }
	}
	
}
