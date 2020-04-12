package org.sakuram.relation.valueobject;

public class RetrieveRelationsRequestVO {
	private long startPersonId;
	private Short maxDepth;

	public long getStartPersonId() {
		return startPersonId;
	}

	public void setStartPersonId(long startPersonId) {
		this.startPersonId = startPersonId;
	}

	public Short getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(Short maxDepth) {
		this.maxDepth = maxDepth;
	}
}
