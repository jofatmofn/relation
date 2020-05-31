package org.sakuram.relation.valueobject;

import java.util.List;

public class RetrieveAppStartValuesResponseVO {
	List<DomainValueVO> domainValueVOList;
	boolean isAppReadOnly;
	String loggedInUser;
	
	public List<DomainValueVO> getDomainValueVOList() {
		return domainValueVOList;
	}
	
	public void setDomainValueVOList(List<DomainValueVO> domainValueVOList) {
		this.domainValueVOList = domainValueVOList;
	}
	
	public boolean isAppReadOnly() {
		return isAppReadOnly;
	}
	
	public void setAppReadOnly(boolean isAppReadOnly) {
		this.isAppReadOnly = isAppReadOnly;
	}

	public String getLoggedInUser() {
		return loggedInUser;
	}

	public void setLoggedInUser(String loggedInUser) {
		this.loggedInUser = loggedInUser;
	}
	
}
