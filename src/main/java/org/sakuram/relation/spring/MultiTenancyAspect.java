package org.sakuram.relation.spring;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.sakuram.relation.util.SecurityContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MultiTenancyAspect {
	
    @PersistenceContext
    public EntityManager entityManager;
	
	@Before("target(org.sakuram.relation.repository.MultiTenancyInterface)")
	public void activateTenantFilter(JoinPoint joinPoint) throws Throwable {
		LogManager.getLogger().debug("activateTenantFilter Aspect on: " + joinPoint.getTarget().toString());
		if (SecurityContext.getCurrentTenantId() != null) {
			entityManager.unwrap(Session.class).enableFilter("tenantFilter").setParameter("tenantId", SecurityContext.getCurrentTenantId());
			LogManager.getLogger().debug("Applying Tenant Filter: " + SecurityContext.getCurrentTenantId());
		}
	}
	
}
