var domainValueVOList, highlightedEntity, loginUserPersonId, domainValueVOMap;
var paSelectElement, raSelectElement, paDomainValueVOList, raDomainValueVOList, isPersonNode;

async function drawGraph() {

	loginUserPersonId = 6;	// TODO: After integration with login, this should be user's person id
	domainValueVOList = await invokeService("/basic/retrieveDomainValues", "");
	domainValueVOMap = new Map();
	paSelectElement = document.createElement("select");
	paSelectElement.setAttribute("name","attributenames");
	raSelectElement = document.createElement("select");
	raSelectElement.setAttribute("name","attributenames");
	paDomainValueVOList = [];
	raDomainValueVOList = [];
	for (let domainValueVO of domainValueVOList) {
		domainValueVOMap.set(domainValueVO.id, domainValueVO);
		optionElement = document.createElement("option");
		optionElement.setAttribute("value", domainValueVO.id);
		optionElement.appendChild(document.createTextNode(domainValueVO.value));
		if (domainValueVO.category == CATEGORY_PERSON_ATTRIBUTE && domainValueVO.inputAsAttribute) {
			paSelectElement.appendChild(optionElement);
			paDomainValueVOList.push(domainValueVO);
		}
		else if (domainValueVO.category == CATEGORY_RELATION_ATTRIBUTE && domainValueVO.inputAsAttribute) {
			raSelectElement.appendChild(optionElement);
			raDomainValueVOList.push(domainValueVO);
		}
	}

	// Instantiate sigma
	s = new sigma({
		graph: await invokeService("/basic/retrieveRelations", {startPersonId : loginUserPersonId}),
		renderer: {
			container: "graph-container",
			type: "canvas"
		},
		settings: {
			minEdgeSize: 0.5,
			maxEdgeSize: 4,
			enableEdgeHovering: true,
			edgeHoverColor: 'edge',
			defaultEdgeHoverColor: '#000',
			edgeHoverSizeRatio: 1,
			edgeHoverExtremities: true
		}
	});
	
	s.bind('overNode', function(e) {
	  s.settings('doubleClickEnabled', false);
	});

	s.bind('outNode', function(e) {
	  s.settings('doubleClickEnabled', true);
	});
	
	s.bind('clickNode', editEntityAttributes);
	
	s.bind('clickEdge', editEntityAttributes);
	
	s.bind('doubleClickNode', async function(e) {
		console.log(e.type, e.data.node.label, e.data.captor);
		s.graph.clear();
		s.graph.read(await invokeService("/basic/retrieveRelations", {startPersonId : e.data.node.id}));
		s.refresh();
		document.getElementById("sidebarbody").innerHTML = "";
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

async function editEntityAttributes(e) {
	var attributeValueVOList, rightBarElement, valueElement, attributeValueBlockElement, saveButtonElement, addButtonElement;
	
	if (highlightedEntity != undefined) {
		highlightedEntity.color = DEFAULT_COLOR;
	}
	
	attributeValueVOList = [];
	if (e.type == "clickNode") {
		isPersonNode = true;
		highlightedEntity = e.data.node;
		console.log(e.type, e.data.node.label, e.data.captor);
		if (e.data.node.id > 0) {
			attributeValueVOList = await invokeService("/basic/retrievePersonAttributes", e.data.node.id);
		}
	}
	else {
		isPersonNode = false;
		highlightedEntity = e.data.edge;
		console.log(e.type, e.data.edge.label, e.data.captor);
		if (e.data.node.id > 0) {
			attributeValueVOList = await invokeService("/basic/retrieveRelationAttributes", e.data.edge.id);
		}
	}
	
	highlightedEntity.color = HIGHLIGHT_COLOR;
	s.refresh();
	
	rightBarElement = document.getElementById("sidebarbody");
	rightBarElement.setAttribute("entityid", (isPersonNode ? e.data.node.id : e.data.edge.id));
	rightBarElement.innerHTML = "";
	for (let attributeValueVO of attributeValueVOList) {
		attributeValueBlockElement = document.createElement("div");
		rightBarElement.appendChild(attributeValueBlockElement);
		attributeValueBlockElement.className = "attrVal";
		attributeValueBlockElement.appendChild(document.createTextNode(attributeValueVO.attributeName));
		createAttributeBlock(attributeValueBlockElement, attributeValueVO);
	}
	addButtonElement = document.getElementById("addbutton");
	addButtonElement.onclick = function() {
		var selectElement, optionElement;
		attributeValueBlockElement = document.createElement("div");
		rightBarElement.appendChild(attributeValueBlockElement);
		attributeValueBlockElement.className = "attrVal";
		
		selectElement = (isPersonNode ? paSelectElement : raSelectElement).cloneNode(true);
		attributeValueBlockElement.appendChild(selectElement);
		createAttributeBlock(attributeValueBlockElement, {attributeDvId: parseInt(selectElement.options[0].value)});
		selectElement.onchange = function() {
			var avbChildNodeList, skippedNodeCount, avbChildNode;
			avbChildNodeList = selectElement.parentElement.childNodes;
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
			createAttributeBlock(selectElement.parentElement, {attributeDvId: parseInt(selectElement.options[selectElement.selectedIndex].value)});
		};
		
	};
	
	saveButtonElement = document.getElementById("savebutton");
	saveButtonElement.onclick = async function() {
		var attributeValueVOList, attributeValueVO, saveAttributesRequestVO, inputElements;
		var attributeVsValueListMap, attributeDvId, attributeDomainValueVO;
		var ind1, ind2, attributeValueNBlkList;
		
		attributeValueVOList = [];
		attributeVsValueListMap = new Map();
		saveAttributesRequestVO = {entityId: rightBarElement.getAttribute("entityid"), attributeValueVOList: attributeValueVOList};
		
		for (let attributeValueBlkElement of rightBarElement.querySelectorAll("div[attributedvid]")) {
			inputElements = attributeValueBlkElement.getElementsByTagName("input");
			attributeValueBlkElement.className = "attrVal";
			attributeDvId = parseInt(attributeValueBlkElement.getAttribute("attributedvid"));
			attributeValueVO = {attributeDvId: attributeDvId, attributeValue: inputElements[0].value, valueAccurate: inputElements[1].checked, startDate: null, endDate: null};
			if (inputElements.length > 2) {
				if (inputElements[2].value != "") {
					attributeValueVO.startDate = pikadayToIsoFormat(inputElements[2].value);
				}
				if (inputElements[3].value != "") {
					attributeValueVO.endDate = pikadayToIsoFormat(inputElements[3].value);
				}
			}
			attributeValueVOList.push(attributeValueVO);
			if (attributeVsValueListMap.has(attributeDvId)) {
				attributeValueNBlkList = attributeVsValueListMap.get(attributeDvId);
			}
			else {
				attributeValueNBlkList = [];
				attributeVsValueListMap.set(attributeDvId, attributeValueNBlkList);
			}
			attributeValueNBlkList.push({attributeValueVO: attributeValueVO, attributeValueBlkElement: attributeValueBlkElement})
		}
		for (let attributeDomainValueVO of (isPersonNode ? paDomainValueVOList : raDomainValueVOList)) {
			if (attributeDomainValueVO.inputMandatory && !attributeVsValueListMap.has(attributeDomainValueVO.id)) {
				alert(attributeDomainValueVO.value + " is a mandatory property");
				return;
			}
			if (attributeDomainValueVO.repetitionType == FLAG_ATTRIBUTE_REPETITION_NON_OVERLAPPING_ALLOWED && attributeVsValueListMap.has(attributeDomainValueVO.id)) {
				attributeValueNBlkList = attributeVsValueListMap.get(attributeDomainValueVO.id);
				for (ind1 = 0; ind1 < attributeValueNBlkList.length - 1; ind1++) {
					for (ind2 = ind1 + 1; ind2 < attributeValueNBlkList.length; ind2++) {
						if (areOverlappingDates(attributeValueNBlkList[ind1].attributeValueVO.startDate, attributeValueNBlkList[ind1].attributeValueVO.endDate, attributeValueNBlkList[ind2].attributeValueVO.startDate, attributeValueNBlkList[ind2].attributeValueVO.endDate)) {
							alert("Multiple values with effective period overlapping not allowed for " + attributeDomainValueVO.value);
							return;
						}
					}
				}
			}
		}
		for (let keyValueArr of attributeVsValueListMap.entries()) {
			attributeDomainValueVO = domainValueVOMap.get(keyValueArr[0]);
			if (attributeDomainValueVO.repetitionType == FLAG_ATTRIBUTE_REPETITION_NOT_ALLOWED && keyValueArr[1].length > 1) {
				alert("Multiple Values not allowed for " + attributeDomainValueVO.value);
				return;
			}
			for (let value of keyValueArr[1]) {
				if (value.attributeValueVO.attributeValue == "") {
					value.attributeValueBlkElement.className = "attrValError";
					alert("Blank is not a valid value");
					return;
				}
				if (value.attributeValueVO.startDate != null && value.attributeValueVO.endDate != null && new Date(value.attributeValueVO.startDate) > new Date(value.attributeValueVO.endDate)) {
					value.attributeValueBlkElement.className = "attrValError";
					alert("Start date cannot be after end date");
					return;
				}
			}
		}
		await invokeService((isPersonNode ? "/basic/savePersonAttributes" : "/basic/saveRelationAttributes"), saveAttributesRequestVO);
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

function areOverlappingDates(startDate1Str, endDate1Str, startDate2Str, endDate2Str) {
	var startDate1, endDate1, startDate2, endDate2;
	if (startDate1Str != null) {
		startDate1 = new Date(startDate1Str);
	}
	if (endDate1Str != null) {
		endDate1 = new Date(endDate1Str);
	}
	if (startDate2Str != null) {
		startDate2 = new Date(startDate2Str);
	}
	if (endDate2Str != null) {
		endDate2 = new Date(endDate2Str);
	}
	if ((startDate1Str === null || endDate2Str === null || startDate1 <= endDate2) &&
		(endDate1Str === null || startDate2Str === null || endDate1 >= startDate2)) {
		return true;
	}
	return false;
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

function addPerson() {
	if (s.graph.nodes(NEW_ENTITY_ID) == null) {
		s.graph.addNode({
			id: NEW_ENTITY_ID,
			size: 5.0,
			x: Math.random() / 10,
			y: Math.random() / 10,
			dX: 0,
			dY: 0,
			type: 'goo'
		});
	}
	
	s.renderers[0].dispatchEvent('clickNode', {node: s.graph.nodes(NEW_ENTITY_ID)});
}
