package org.sakuram.relation.service;

import org.sakuram.relation.bean.Tenant;
import org.sakuram.relation.repository.TenantRepository;
import org.sakuram.relation.util.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@Transactional
public class ProjectUserService {
	@Autowired
	TenantRepository tenantRepository;
	
    public Long switchProject(@RequestBody String projectId) {
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

}
