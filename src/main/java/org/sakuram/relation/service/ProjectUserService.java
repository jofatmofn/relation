package org.sakuram.relation.service;

import org.sakuram.relation.bean.AppUser;
import org.sakuram.relation.bean.Tenant;
import org.sakuram.relation.repository.AppUserRepository;
import org.sakuram.relation.repository.TenantRepository;
import org.sakuram.relation.util.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectUserService {
	@Autowired
	TenantRepository tenantRepository;
	@Autowired
	AppUserRepository appUserRepository;
	
    public Long switchProject(String projectId) {
    	Tenant tenant;
    	if (projectId == null) {
    		throw new AppException("Specify a Project to switch to", null);
    	}
    	tenant = tenantRepository.findByProjectId(projectId);
    	if (tenant == null) {
    		throw new AppException("Specified Project " + projectId + " does not exist", null);
    	}
    	return tenant.getId();
    }

    public Long findOrSaveUser(String identityProvider, String identityProviderUserId) {
    	AppUser appUser;
    	
    	appUser = appUserRepository.findByIdentityProviderAndIdentityProviderUserId(identityProvider, identityProviderUserId);
    	if (appUser == null) {
    		appUser = new AppUser();
    		appUser.setIdentityProvider(identityProvider);
    		appUser.setIdentityProviderUserId(identityProviderUserId);
    		appUser = appUserRepository.save(appUser);
    	}
    	return appUser.getId();
    }
}
