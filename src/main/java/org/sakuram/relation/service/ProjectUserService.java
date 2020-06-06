package org.sakuram.relation.service;

import org.apache.logging.log4j.LogManager;
import org.sakuram.relation.bean.AppUser;
import org.sakuram.relation.bean.Privilege;
import org.sakuram.relation.bean.Tenant;
import org.sakuram.relation.repository.AppUserRepository;
import org.sakuram.relation.repository.PrivilegeRepository;
import org.sakuram.relation.repository.TenantRepository;
import org.sakuram.relation.util.AppException;
import org.sakuram.relation.util.Constants;
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
	@Autowired
	PrivilegeRepository privilegeRepository;
	
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
    
    public boolean isAppReadOnly(Long tenantId, Long appUserId) {
    	AppUser appUser;
    	Tenant tenant;
    	Privilege privilege;
    	boolean isReadOnly;
    	long roleDvId;
    	
    	isReadOnly = true;
    	if (tenantId != null && appUserId != null) {
    		tenant = tenantRepository.findById(tenantId)
    				.orElseThrow(() -> new AppException("Invalid Tenant Id " + tenantId, null));
    		appUser = appUserRepository.findById(appUserId)
    				.orElseThrow(() -> new AppException("Invalid User Id " + appUserId, null));
    		privilege = privilegeRepository.findByTenantAndAppUser(tenant, appUser);
    		if (privilege != null) {
    			roleDvId = privilege.getRole().getId();
    			if (roleDvId == Constants.ROLE_DV_ID_COLLABORATOR || roleDvId == Constants.ROLE_DV_ID_CREATOR) {
    				isReadOnly = false;
    			}
    		}
    	}
		LogManager.getLogger().info("Tenant: " + tenantId + ". AppUser: " + appUserId + ". Privilege: " + isReadOnly);
    	return isReadOnly;
    }
}
