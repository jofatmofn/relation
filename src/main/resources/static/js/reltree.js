var domainValueVOList, highlightedNode, loginUserPersonId, domainValueVOMap;

async function drawGraph() {

	loginUserPersonId = 6;	// TODO: After integration with login, this should be user's person id
	domainValueVOList = await invokeService("/basic/retrieveDomainValues", "");
	domainValueVOMap = new Map();
	for (let domainValueVO of domainValueVOList) {
		domainValueVOMap.set(domainValueVO.id, domainValueVO);
	}

	// Instantiate sigma
	s = new sigma({
		graph: await invokeService("/basic/retrieveRelations", {startPersonId : loginUserPersonId}),
		renderer: {
			container: "graph-container",
			type: "canvas"
		}
	});
	
	s.bind('overNode', function(e) {
	  s.settings('doubleClickEnabled', false);
	});

	s.bind('outNode', function(e) {
	  s.settings('doubleClickEnabled', true);
	});			
	s.bind('clickNode', editPersonNode);
	
	s.bind('doubleClickNode', async function(e) {
		console.log(e.type, e.data.node.label, e.data.captor);
		s.graph.clear();
		s.graph.read(await invokeService("/basic/retrieveRelations", {startPersonId : e.data.node.id}));
		s.refresh();
	});

	// *** sigma.layout.noverlap ***
	var config = {
		nodeMargin: 3.0,
		scaleNodes: 1.3
	};
	
	var listener = s.configNoverlap(config);
	
	listener.bind('start stop interpolate', function(event) {
		console.log(event.type);
	});

	s.startNoverlap();
	
	// *** sigma.plugins.dragNodes ***
	var dragListener = sigma.plugins.dragNodes(s, s.renderers[0]);

	dragListener.bind('startdrag', function(event) {
	  console.log(event);
	});
	dragListener.bind('drag', function(event) {
	  console.log(event);
	});
	dragListener.bind('drop', function(event) {
	  console.log(event);
	});
	dragListener.bind('dragend', function(event) {
	  console.log(event);
	});

}

async function editPersonNode(e) {
	console.log(e.type, e.data.node.label, e.data.captor);
	var attributeValueVOList, rightBarElement, valueElement, attributeValueBlockElement, saveButtonElement, addButtonElement;
	
	if (highlightedNode != undefined) {
		highlightedNode.color = "rgb(1,179,255)";
	}
	e.data.node.color = "rgb(179,179,179)";
	highlightedNode = e.data.node;
	s.refresh();
	attributeValueVOList = await invokeService("/basic/retrievePersonAttributes", e.data.node.id);
	rightBarElement = document.getElementById("sidebar");
	rightBarElement.setAttribute("personid",e.data.node.id);
	rightBarElement.innerHTML = "";
	for (let attributeValueVO of attributeValueVOList) {
		attributeValueBlockElement = document.createElement("div");
		rightBarElement.appendChild(attributeValueBlockElement);
		attributeValueBlockElement.className = "attrVal";
		attributeValueBlockElement.appendChild(document.createTextNode(attributeValueVO.attributeName));
		createAttributeBlock(attributeValueBlockElement, attributeValueVO);
	}
	addButtonElement = document.createElement("button");
	rightBarElement.appendChild(addButtonElement);
	addButtonElement.appendChild(document.createTextNode("+"));
	addButtonElement.onclick = function() {
		var selectElement, optionElement;
		attributeValueBlockElement = document.createElement("div");
		rightBarElement.appendChild(attributeValueBlockElement);
		attributeValueBlockElement.className = "attrVal";
		
		selectElement = document.createElement("select");
		attributeValueBlockElement.appendChild(selectElement);
		selectElement.setAttribute("name","attributenames");
		for (let domainValueVO of domainValueVOList) {
			if (domainValueVO.category == CATEGORY_PERSON_ATTRIBUTE && domainValueVO.inputAsAttribute) {
				optionElement = document.createElement("option");
				selectElement.appendChild(optionElement);
				optionElement.setAttribute("value", domainValueVO.id);
				optionElement.appendChild(document.createTextNode(domainValueVO.value));
			}
		}
		createAttributeBlock(attributeValueBlockElement, {attributeDvId: parseInt(selectElement.options[0].value)});
		selectElement.onchange = function() {
			var avbChildNodeList, skippedNodeCount, avbChildNode;
			avbChildNodeList = attributeValueBlockElement.childNodes;
			skippedNodeCount = 0;
			while (avbChildNodeList.length > skippedNodeCount) {
				avbChildNode = avbChildNodeList[skippedNodeCount];
				if (avbChildNode.nodeName != "SELECT" || !avbChildNode.hasAttribute("name") || avbChildNode.getAttribute("name") != "attributenames") {
					avbChildNode.remove();
				}
				else {
					skippedNodeCount = skippedNodeCount + 1;
				}
			}
			createAttributeBlock(attributeValueBlockElement, {attributeDvId: parseInt(selectElement.options[selectElement.selectedIndex].value)});
		};
		
	};
	
	saveButtonElement = document.createElement("button");
	rightBarElement.appendChild(saveButtonElement);
	saveButtonElement.appendChild(document.createTextNode("Save"));
	saveButtonElement.onclick = async function() {
		var attributeValueVOList, attributeValueVO, saveAttributesRequestVO, inputElements;
		attributeValueVOList = [];
		saveAttributesRequestVO = {entityId: rightBarElement.getAttribute("personid"), attributeValueVOList: attributeValueVOList};
		
		for (let attributeValueBlkElement of rightBarElement.querySelectorAll("div[attributedvid]")) {
			// TODO: Validation across attributeValue elements (based on repetitionType)
			inputElements = attributeValueBlkElement.getElementsByTagName("input");
			if (inputElements[0].value != "") {
				attributeValueVO = {attributeDvId: attributeValueBlkElement.getAttribute("attributedvid"), attributeValue: inputElements[0].value, valueAccurate: inputElements[1].checked, startDate: null, endDate: null};
				if (inputElements.length > 2) {
					if (inputElements[2].value != "") {
						attributeValueVO.startDate = pikadayToIsoFormat(inputElements[2].value);
					}
					if (inputElements[3].value != "") {
						attributeValueVO.endDate = pikadayToIsoFormat(inputElements[3].value);
					}
				}
				attributeValueVOList.push(attributeValueVO);
			}
		}
		await invokeService("/basic/savePersonAttributes", saveAttributesRequestVO);
		alert("Saved");
	};	
}

function pikadayToIsoFormat(pDateStr) {
	var pDate, year, month, day;
	pDate = new Date(pDateStr);
	year = pDate.getFullYear();
	month = pDate.getMonth() + 1;
	day = pDate.getDate();
	return [year, month < 10 ? "0" + month : month, day < 10 ? "0" + day : day].join('-');
}

function isoToPikadayFormat(iDateStr) {
	// This function is required because of the issue https://github.com/Pikaday/Pikaday/issues/655
	const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
	const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
	var iDate, dayNo;
	iDate = new Date(iDateStr);
	dayNo = iDate.getDate();
	return [days[iDate.getDay()], months[iDate.getMonth()], dayNo < 10 ? "0" + dayNo : dayNo, iDate.getFullYear()].join(" ");
}

function createAttributeBlock(attributeValueBlockElement, attributeValueVO) {
	var valueElement, isAccurateElement, startDateElement, endDateElement;
	var startDatePicker, endDatePicker, attributeDomainValueVO;
	
	attributeDomainValueVO = domainValueVOMap.get(attributeValueVO.attributeDvId);
	attributeValueBlockElement.setAttribute("attributedvid", attributeValueVO.attributeDvId);
	
	attributeValueBlockElement.appendChild(document.createElement("br"));
	
	attributeValueBlockElement.appendChild(document.createTextNode("Value: "));
	
	valueElement = document.createElement("input");
	attributeValueBlockElement.appendChild(valueElement);
	valueElement.setAttribute("type","text");	// TODO: Based on the person/relation attribute, the type of input will vary
	if (attributeValueVO.attributeValue != undefined) {
		valueElement.setAttribute("value", attributeValueVO.attributeValue);
	}
	
	attributeValueBlockElement.appendChild(document.createElement("br"));
	
	attributeValueBlockElement.appendChild(document.createTextNode("Is Accurate: "));
	isAccurateElement = document.createElement("input");
	attributeValueBlockElement.appendChild(isAccurateElement);
	isAccurateElement.setAttribute("type","checkbox");
	if (attributeValueVO.valueAccurate != undefined && attributeValueVO.valueAccurate) {
		isAccurateElement.setAttribute("checked", "");
	}
	
	if (attributeDomainValueVO.repetitionType != FLAG_ATTRIBUTE_REPETITION_NOT_ALLOWED) {
		attributeValueBlockElement.appendChild(document.createElement("br"));
		
		attributeValueBlockElement.appendChild(document.createTextNode("Start Date: "));
		startDateElement = document.createElement("input");
		attributeValueBlockElement.appendChild(startDateElement);
		startDateElement.setAttribute("type","text");
		startDatePicker = new Pikaday({field: startDateElement, theme: "dark-theme", minDate: new Date(1, 1, 1)});
		if (attributeValueVO.startDate != undefined) {
			startDateElement.setAttribute("value", isoToPikadayFormat(attributeValueVO.startDate));
		}
		
		attributeValueBlockElement.appendChild(document.createElement("br"));
		
		attributeValueBlockElement.appendChild(document.createTextNode("End Date: "));
		endDateElement = document.createElement("input");
		attributeValueBlockElement.appendChild(endDateElement);
		endDateElement.setAttribute("type","text");
		endDatePicker = new Pikaday({field: endDateElement, theme: "dark-theme"});
		if (attributeValueVO.endDate != undefined) {
			endDateElement.setAttribute("value", isoToPikadayFormat(attributeValueVO.endDate));
		}
	}
	
	if (attributeDomainValueVO.inputAsAttribute) {
		valueElement.removeAttribute("disabled","");
		isAccurateElement.removeAttribute("disabled","");
		if (attributeDomainValueVO.repetitionType != FLAG_ATTRIBUTE_REPETITION_NOT_ALLOWED) {
			startDateElement.removeAttribute("disabled","");
			endDateElement.removeAttribute("disabled","");
		}
	}
	else {
		valueElement.setAttribute("disabled","");
		isAccurateElement.setAttribute("disabled","");
		if (attributeDomainValueVO.repetitionType != FLAG_ATTRIBUTE_REPETITION_NOT_ALLOWED) {
			startDateElement.setAttribute("disabled","");
			endDateElement.setAttribute("disabled","");
		}
	}
}
