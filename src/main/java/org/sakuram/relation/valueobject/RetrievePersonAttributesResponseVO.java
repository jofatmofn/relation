package org.sakuram.relation.valueobject;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RetrievePersonAttributesResponseVO {
	byte[] photo;
	List<AttributeValueVO> attributeValueVOList;

}
