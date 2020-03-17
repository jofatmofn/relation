package org.sakuram.relation.valueobject;

import java.util.List;

public class SearchResultsVO {
	long resultsCount;
	List<List<String>> resultsList;
	
	public long getResultsCount() {
		return resultsCount;
	}
	
	public void setResultsCount(long resultsCount) {
		this.resultsCount = resultsCount;
	}
	
	public List<List<String>> getResultsList() {
		return resultsList;
	}
	
	public void setResultsList(List<List<String>> resultsList) {
		this.resultsList = resultsList;
	}
	
}
