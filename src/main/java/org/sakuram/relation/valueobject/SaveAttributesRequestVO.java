package org.sakuram.relation.valueobject;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SaveAttributesRequestVO {
	private long entityId;
	private List<AttributeValueVO> attributeValueVOList;
	private long creatorId;
	private byte[] photo;

}
