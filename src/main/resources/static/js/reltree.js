var domainValueVOList, highlightedEntity, loginUserPersonId, domainValueVOMap;
var paSelectElement, raSelectElement, paDomainValueVOList, raDomainValueVOList, isPersonNode;
var action, selectElementMap;

async function drawGraph() {
	
	var selectElement;
	window.addEventListener("unhandledrejection", event =>
	{
		alert(event.reason);
	});
	
	loginUserPersonId = 6;	// TODO: After integration with login, this should be user's person id
	domainValueVOList = await invokeService("/basic/retrieveDomainValues", "");
	domainValueVOMap = new Map();
	paSelectElement = document.createElement("select");
	paSelectElement.setAttribute("name","attributenames");
	raSelectElement = document.createElement("select");
	raSelectElement.setAttribute("name","attributenames");
	selectElementMap = new Map();
	paDomainValueVOList = [];
	raDomainValueVOList = [];
	for (let domainValueVO of domainValueVOList) {
		domainValueVOMap.set(domainValueVO.id, domainValueVO);
		optionElement = document.createElement("option");
		optionElement.setAttribute("value", domainValueVO.id);
		optionElement.appendChild(document.createTextNode(domainValueVO.value));
		if (domainValueVO.category == CATEGORY_PERSON_ATTRIBUTE) {
			if (domainValueVO.inputAsAttribute) {
				paSelectElement.appendChild(optionElement);
				paDomainValueVOList.push(domainValueVO);
			}
		}
		else if (domainValueVO.category == CATEGORY_RELATION_ATTRIBUTE) {
			if (domainValueVO.inputAsAttribute) {
				raSelectElement.appendChild(optionElement);
				raDomainValueVOList.push(domainValueVO);
			}
		}
		else {
			if (selectElementMap.has(domainValueVO.category)) {
				selectElement = selectElementMap.get(domainValueVO.category);
			}
			else {
				selectElement = document.createElement("select");
				selectElement.setAttribute("name", domainValueVO.category);
				selectElementMap.set(domainValueVO.category, selectElement);
			}
			selectElement.appendChild(optionElement);
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
			doubleClickEnabled: false,
			minEdgeSize: 0.5,
			maxEdgeSize: 4,
			enableEdgeHovering: true,
			edgeHoverColor: 'edge',
			defaultEdgeHoverColor: '#000',
			edgeHoverSizeRatio: 1,
			edgeHoverExtremities: true
		}
	});
	
	/* https://github.com/jacomyal/sigma.js/issues/910
	s.bind('overNode', function(e) {
	  s.settings('doubleClickEnabled', false);
	});

	s.bind('outNode', function(e) {
	  s.settings('doubleClickEnabled', true);
	}); */
	
	s.bind('clickNode', editEntityAttributes);
	
	s.bind('clickEdge', editEntityAttributes);
	
	s.bind('doubleClickNode', async function(e) {
		console.log(e.type, e.data.node.label, e.data.captor);
		if (e.data.node.id != NEW_ENTITY_ID && e.data.node.id != SEARCH_ENTITY_ID) {
			s.graph.clear();
			s.graph.read(await invokeService("/basic/retrieveRelations", {startPersonId : e.data.node.id}));
			s.refresh();
		}
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
	var attributeValueVOList, rightBarElement, valueElement, attributeValueBlockElement, actionButtonElement, addButtonElement;
	var person1Node, person2Node;
	
	if (highlightedEntity != undefined) {
		highlightedEntity.color = DEFAULT_COLOR;
	}
	
	attributeValueVOList = [];
	action = ACTION_SAVE;
	if (e.type == "clickNode") {
		isPersonNode = true;
		highlightedEntity = e.data.node;
		console.log(e.type, e.data.node.label, e.data.captor);
		if (e.data.node.id == SEARCH_ENTITY_ID) {
			action = ACTION_SEARCH;
		}
		if (e.data.node.id > 0) {
			attributeValueVOList = await invokeService("/basic/retrievePersonAttributes", e.data.node.id);
		}
	}
	else {
		isPersonNode = false;
		highlightedEntity = e.data.edge;
		console.log(e.type, e.data.edge.label, e.data.captor);
		if (e.data.edge.id > 0) {
			attributeValueVOList = await invokeService("/basic/retrieveRelationAttributes", e.data.edge.id);
		}
	}
	
	highlightedEntity.color = HIGHLIGHT_COLOR;
	
	rightBarElement = document.getElementById("sidebarbody");
	rightBarElement.innerHTML = "";
	/* Current attribute values from back-end */
	for (let attributeValueVO of attributeValueVOList) {
		if (attributeValueVO.attributeDvId == PERSON_ATTRIBUTE_DV_ID_LABEL) {
			highlightedEntity.label = attributeValueVO.attributeValue;
		}
		attributeValueBlockElement = document.createElement("div");
		rightBarElement.appendChild(attributeValueBlockElement);
		attributeValueBlockElement.className = "attrVal";
		attributeValueBlockElement.appendChild(document.createTextNode(attributeValueVO.attributeName));
		createAttributeBlock(attributeValueBlockElement, attributeValueVO);
	}
	/* Mandatory attributes for new entity */
	if (action == ACTION_SAVE && attributeValueVOList.length == 0) {
		for (let attributeDomainValueVO of (isPersonNode ? paDomainValueVOList : raDomainValueVOList)) {
			if (attributeDomainValueVO.inputMandatory) {
				attributeValueBlockElement = document.createElement("div");
				rightBarElement.appendChild(attributeValueBlockElement);
				attributeValueBlockElement.className = "attrVal";
				attributeValueBlockElement.appendChild(document.createTextNode(attributeDomainValueVO.value));
				createAttributeBlock(attributeValueBlockElement, {attributeDvId: attributeDomainValueVO.id});
			}
		}
	}
	s.refresh();
	
	document.getElementById("sidebarbuttons").innerHTML = "<button id='addbutton'>+</button><button id='actionbutton'>" + action + "</button>";
	addButtonElement = document.getElementById("addbutton");
	actionButtonElement = document.getElementById("actionbutton");
	
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
	
	switch(action) {
		case ACTION_SAVE:
			if (isPersonNode) {
				document.getElementById("sidebartitle").textContent = "Details of " + (highlightedEntity.id == NEW_ENTITY_ID ? "new person" : highlightedEntity.label + "(" + highlightedEntity.id + ")");
			}
			else {
				person1Node = s.graph.nodes(highlightedEntity.source);
				person2Node = s.graph.nodes(highlightedEntity.target);
				document.getElementById("sidebartitle").textContent = "Details of relation between " + person1Node.label + "(" + person1Node.id + ") and " + person2Node.label + "(" + person2Node.id + ")";
			}
			break;
		case ACTION_SEARCH:
			document.getElementById("sidebartitle").textContent = "Person Search Criteria";
			break;
	}
	
	actionButtonElement.onclick = async function() {
		var attributeValueVOList, attributeValueVO, saveAttributesRequestVO, inputElements;
		var attributeVsValueListMap, attributeDvId, attributeDomainValueVO;
		var ind1, ind2, attributeValueNBlkList, searchedPersonId, entityId;
		var attributeValueId, relationPerson1ForPerson2, relationPerson2ForPerson1, relationSubType;
		
		attributeValueVOList = [];
		attributeVsValueListMap = new Map();
		saveAttributesRequestVO = {entityId: highlightedEntity.id, attributeValueVOList: attributeValueVOList};
		
		for (let attributeValueBlkElement of rightBarElement.querySelectorAll("div[attributedvid]")) {
			inputElements = attributeValueBlkElement.querySelectorAll("input,select:not([name=attributenames])");
			attributeDvId = parseInt(attributeValueBlkElement.getAttribute("attributedvid"));
			attributeValueVO = {attributeDvId: attributeDvId, id: null, attributeValue: (inputElements[0].tagName == "INPUT" ? inputElements[0].value : inputElements[0].options[inputElements[0].selectedIndex].value), valueAccurate: null, startDate: null, endDate: null};
			if (action == ACTION_SAVE) {
				if (attributeValueBlkElement.hasAttribute("attributevalueid")) {
					attributeValueVO.id = parseInt(attributeValueBlkElement.getAttribute("attributevalueid"));
				}
				attributeValueVO.valueAccurate = inputElements[1].checked;
				if (inputElements.length > 2) {
					if (inputElements[2].value != "") {
						attributeValueVO.startDate = pikadayToIsoFormat(inputElements[2].value);
					}
					if (inputElements[3].value != "") {
						attributeValueVO.endDate = pikadayToIsoFormat(inputElements[3].value);
					}
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
		if (action == ACTION_SAVE) {
			// Validations
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
			if (!isPersonNode) {
				relationPerson1ForPerson2 = attributeVsValueListMap.get(RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2)[0].attributeValueVO.attributeValue;
				relationPerson2ForPerson1 = attributeVsValueListMap.get(RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1)[0].attributeValueVO.attributeValue;
				attributeDomainValueVO = domainValueVOMap.get(parseInt(relationPerson1ForPerson2));
				if (!VALID_RELATIONS_JSON.includes(JSON.stringify([relationPerson1ForPerson2, relationPerson2ForPerson1]))) {
					alert("Invalid pair of relations");
					return;
				}
				if (attributeVsValueListMap.has(RELATION_ATTRIBUTE_DV_ID_RELATION_SUB_TYPE)) {
					relationSubType = attributeVsValueListMap.get(RELATION_ATTRIBUTE_DV_ID_RELATION_SUB_TYPE)[0].attributeValueVO.attributeValue;
					if (attributeDomainValueVO.relationParentChild && !VALID_RELSUBTYPES_PARENT_CHILD.includes(relationSubType) ||
						attributeDomainValueVO.relationSpouse && !VALID_RELSUBTYPES_SPOUSE.includes(relationSubType)) {
						alert("Invalid relation sub type");
						return;
					}
				}
			}
		}
		switch(action) {
			case ACTION_SAVE:
				entityId = await invokeService((isPersonNode ? "/basic/savePersonAttributes" : "/basic/saveRelationAttributes"), saveAttributesRequestVO);
				alert("Saved");
				if (isPersonNode && highlightedEntity.id == NEW_ENTITY_ID) {
					s.graph.dropNode(NEW_ENTITY_ID);
					s.graph.addNode({
						id: entityId,
						size: 5.0,
						x: Math.random() / 10,
						y: Math.random() / 10,
						dX: 0,
						dY: 0,
						type: 'goo'
					});
					highlightedEntity = s.graph.nodes(entityId);
					highlightedEntity.color = HIGHLIGHT_COLOR;
				}
				highlightedEntity.label = (isPersonNode ? attributeVsValueListMap.get(PERSON_ATTRIBUTE_DV_ID_LABEL)[0].attributeValueVO.attributeValue :
					domainValueVOMap.get(parseInt(attributeVsValueListMap.get(RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2)[0].attributeValueVO.attributeValue)).value + "-" + domainValueVOMap.get(parseInt(attributeVsValueListMap.get(RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1)[0].attributeValueVO.attributeValue)).value);
				s.refresh();
				break;
			case ACTION_SEARCH:
				searchedPersonId = await invokeService("/basic/searchPerson", attributeValueVOList);
				if (searchedPersonId == NEW_ENTITY_ID) {
					alert("Person with the specified properties could not be found");
				}
				else {
					s.graph.dropNode(SEARCH_ENTITY_ID);
					if (s.graph.nodes(searchedPersonId) != null) {
						alert("Person exists already");
						s.renderers[0].dispatchEvent('clickNode', {node: s.graph.nodes(searchedPersonId)});
					}
					else {
						addPerson(searchedPersonId);
					}
				}
				break;
		}
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
	var deleteBlockImageElement;
	
	attributeDomainValueVO = domainValueVOMap.get(attributeValueVO.attributeDvId);
	attributeValueBlockElement.setAttribute("attributedvid", attributeValueVO.attributeDvId);
	if (attributeValueVO.id != undefined) {
		attributeValueBlockElement.setAttribute("attributevalueid", attributeValueVO.id);
	}
	
	attributeValueBlockElement.appendChild(document.createTextNode("     "));
	deleteBlockImageElement = document.createElement("img");
	attributeValueBlockElement.appendChild(deleteBlockImageElement);
	deleteBlockImageElement.setAttribute("src","img/delete.png");
	deleteBlockImageElement.setAttribute("alt","Delete Property");
	deleteBlockImageElement.setAttribute("style","width='50%';height='50%'");
	deleteBlockImageElement.onclick = async function() {
		attributeValueBlockElement.remove();
	};
	
	attributeValueBlockElement.appendChild(document.createElement("br"));
	
	attributeValueBlockElement.appendChild(document.createTextNode("Value: "));
	
	if (attributeDomainValueVO.attributeDomain == "") {
		valueElement = document.createElement("input");
		valueElement.setAttribute("type","text");
		if (attributeValueVO.attributeValue != undefined) {
			valueElement.setAttribute("value", attributeValueVO.attributeValue);
		}
	}
	else {
		valueElement = selectElementMap.get(attributeDomainValueVO.attributeDomain).cloneNode(true);
		if (attributeValueVO.attributeValue != undefined) {
			valueElement.value = domainValueVOMap.get(parseInt(attributeValueVO.attributeValue)).id;
		}
	}
	attributeValueBlockElement.appendChild(valueElement);

	if (action != ACTION_SAVE) {
		return;
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

function addPerson(personId = NEW_ENTITY_ID) {
	if (s.graph.nodes(personId) == null) {
		s.graph.addNode({
			id: personId,
			size: 5.0,
			x: Math.random(),
			y: Math.random(),
			type: 'goo'
		});
	}
	if (personId == NEW_ENTITY_ID) {
		s.graph.nodes(NEW_ENTITY_ID).label = 'Yet to be Added';
	}
	s.renderers[0].dispatchEvent('clickNode', {node: s.graph.nodes(personId)});
}

function searchPerson() {
	if (s.graph.nodes(SEARCH_ENTITY_ID) == null) {
		s.graph.addNode({
			id: SEARCH_ENTITY_ID,
			size: 5.0,
			x: Math.random(),
			y: Math.random(),
			type: 'goo'
		});
	}
	s.graph.nodes(SEARCH_ENTITY_ID).label = 'Yet to be Searched';
	s.renderers[0].dispatchEvent('clickNode', {node: s.graph.nodes(SEARCH_ENTITY_ID)});
}

function clearGraph() {
	s.graph.clear();
	s.refresh();
	document.getElementById("sidebartitle").innerHTML = "";
	document.getElementById("sidebarbuttons").innerHTML = "";
	document.getElementById("sidebarbody").innerHTML = "";
}

function relatePersons() {
	var actionButtonElement;
	
	document.getElementById("sidebarbuttons").innerHTML = "<button id='actionbutton'>Relate</button>";
	document.getElementById("sidebartitle").textContent = "Related Persons";

	getPersonsPair();
	
	actionButtonElement = document.getElementById("actionbutton");
	actionButtonElement.onclick = async function() {
		var person1Id, person2Id, relationId, person1Element, person2Element;
		person1Element = document.getElementById("person1");
		person2Element = document.getElementById("person2");
		person1Id = parseInt(person1Element.options[person1Element.selectedIndex].value);
		person2Id = parseInt(person2Element.options[person2Element.selectedIndex].value);
		if (person1Id == person2Id) {
			alert("Same person cannot be part of a relation");
			return;
		}
		relationId = await invokeService("/basic/saveRelation", {person1Id: person1Id, person2Id: person2Id});
		s.graph.addEdge({
			id: relationId,
			source: person1Id,
			target: person2Id,
			label: '',
			size: 5.0,
			type: 'goo'
		});
		s.renderers[0].dispatchEvent('clickEdge', {edge: s.graph.edges(relationId)});
	}
}

function ascertainRelation() {
	var actionButtonElement;
	
	document.getElementById("sidebarbuttons").innerHTML = "<button id='actionbutton'>Ascertain</button>";
	document.getElementById("sidebartitle").textContent = "Ascertain Relation";

	getPersonsPair();
	
	actionButtonElement = document.getElementById("actionbutton");
	actionButtonElement.onclick = async function() {
		var person1Id, person2Id, relationId, person1Element, person2Element;
		person1Element = document.getElementById("person1");
		person2Element = document.getElementById("person2");
		person1Id = parseInt(person1Element.options[person1Element.selectedIndex].value);
		person2Id = parseInt(person2Element.options[person2Element.selectedIndex].value);
		if (person1Id == person2Id) {
			alert("Same person cannot be part of a relation");
			return;
		}
		s.graph.clear();
		s.graph.read(await invokeService("/algo/retrieveRelationPath", {person1Id: person1Id, person2Id: person2Id}));
		s.refresh();
	}
}

function getPersonsPair() {
	var selectElement, optionElement, node, rightBarElement, person1Element, person2Element;
	
	selectElement = document.createElement("select");
	selectElement.setAttribute("name","persons");
	for (let node of s.graph.nodes()) {
		if (node.id != NEW_ENTITY_ID && node.id != SEARCH_ENTITY_ID) {
			optionElement = document.createElement("option");
			optionElement.setAttribute("value", node.id);
			optionElement.appendChild(document.createTextNode(node.label + " (" + node.id + ")"));
			selectElement.appendChild(optionElement);
		}
	}
	rightBarElement = document.getElementById("sidebarbody");
	
	rightBarElement.innerHTML = "";
	rightBarElement.appendChild(document.createTextNode("Person 1"));
	person1Element = selectElement.cloneNode(true);
	person1Element.setAttribute("id", "person1");
	rightBarElement.appendChild(person1Element);
	
	rightBarElement.appendChild(document.createElement("br"));
	rightBarElement.appendChild(document.createTextNode("Person 2"));
	person2Element = selectElement.cloneNode(true);
	person2Element.setAttribute("id", "person2");
	rightBarElement.appendChild(person2Element);
	
}
