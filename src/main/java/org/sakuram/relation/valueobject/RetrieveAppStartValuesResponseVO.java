package org.sakuram.relation.valueobject;

import java.util.List;

public class RetrieveAppStartValuesResponseVO {
	List<DomainValueVO> domainValueVOList;
	String loggedInUser;
	String inUseProject;
	
	public List<DomainValueVO> getDomainValueVOList() {
		return domainValueVOList;
	}
	
	public void setDomainValueVOList(List<DomainValueVO> domainValueVOList) {
		this.domainValueVOList = domainValueVOList;
	}
	
	public String getLoggedInUser() {
		return loggedInUser;
	}

	public void setLoggedInUser(String loggedInUser) {
		this.loggedInUser = loggedInUser;
	}

	public String getInUseProject() {
		return inUseProject;
	}

	public void setInUseProject(String inUseProject) {
		this.inUseProject = inUseProject;
	}
	
}
