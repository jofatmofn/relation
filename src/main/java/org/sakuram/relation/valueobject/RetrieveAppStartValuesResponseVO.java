package org.sakuram.relation.valueobject;

import java.util.List;

public class RetrieveAppStartValuesResponseVO {
	List<DomainValueVO> domainValueVOList;
	boolean isAppReadOnly;
	
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
	
}
