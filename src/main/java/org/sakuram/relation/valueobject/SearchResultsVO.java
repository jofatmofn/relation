package org.sakuram.relation.valueobject;

import java.util.List;

public class SearchResultsVO {
	Boolean isMorePresentInDb;
	List<List<String>> resultsList;
	
	public Boolean isMorePresentInDb() {
		return isMorePresentInDb;
	}

	public void setMorePresentInDb(Boolean isMorePresentInDb) {
		this.isMorePresentInDb = isMorePresentInDb;
	}

	public List<List<String>> getResultsList() {
		return resultsList;
	}
	
	public void setResultsList(List<List<String>> resultsList) {
		this.resultsList = resultsList;
	}
	
}
