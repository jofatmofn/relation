var domainValueVOList, isAppReadOnly, highlightedEntity, domainValueVOMap;
var paSelectElement, raSelectElement, paDomainValueVOList, raDomainValueVOList, isPersonNode;
var action, selectElementMap, doubleClick, paSearchXtraOptions;
var allPersonsSelectElement, malePersonsSelectElement, femalePersonsSelectElement;

async function drawGraph() {
	
	var selectElement, retrieveAppStartValuesResponseVO, buttonElement, urlParams, startPersonId;
	var ind, startProjectId;
	
	window.addEventListener("unhandledrejection", event =>
	{
		if (event.reason.stack != undefined && event.reason.stack.startsWith("sigma.")) {
			console.log(event);
			alert("System ran into trouble. Kindly refresh the screen, by pressing F5.");
		}
		else {
			alert(event.reason);
			switch (event.reason) {
				case "Establish Project before using the system":
					document.getElementById("project").value = 	"";
					break;
				case "Establish yourself by logging-in to the system":
					loginLogout("");
					isAppReadOnly=true;
					enableDisableRWFunctions();
					break;
			}
		}
	});
	
	isAppReadOnly = await invokeService("projectuser/postLogin", "");
	retrieveAppStartValuesResponseVO = await invokeService("basic/retrieveAppStartValues", "");
	if (retrieveAppStartValuesResponseVO.loggedInUser != null) {
		loginLogout(retrieveAppStartValuesResponseVO.loggedInUser);
	}
	domainValueVOList = retrieveAppStartValuesResponseVO.domainValueVOList;
	document.getElementById("project").value = retrieveAppStartValuesResponseVO.inUseProject;
	domainValueVOMap = new Map();
	paSelectElement = document.createElement("select");
	paSelectElement.setAttribute("name","attributenames");
	paSelectElement.classList.add("propdrop");
	raSelectElement = document.createElement("select");
	raSelectElement.setAttribute("name","attributenames");
	raSelectElement.classList.add("propdrop");
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
				selectElement.classList.add("propdrop");
				selectElementMap.set(domainValueVO.category, selectElement);
			}
			selectElement.appendChild(optionElement);
		}
	}

	paSearchXtraOptions = [];
	ind = 0;
	for (label of ["Person Id", "Parents", "Spouses"]) {
		optionElement = document.createElement("option");
		optionElement.setAttribute("value", --ind);
		optionElement.appendChild(document.createTextNode(label));
		paSearchXtraOptions.push(optionElement);
	}
	
	// Instantiate sigma
	urlParams = new URLSearchParams(window.location.search);
	startPersonId = urlParams.get('startPersonId');
	startProjectId = urlParams.get('startProjectId');
	s = new sigma({
		renderer: {
			container: "graph-container",
			type: "canvas"
		},
		settings: {
			doubleClickEnabled: false,
			minEdgeSize: 0.5,
			maxEdgeSize: 2,
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
	
	doubleClick = 0; // https://github.com/jacomyal/sigma.js/issues/506
						// To avoid double click also firing single click.
						// However, this is causing some delays!
	
	s.bind('clickNode', function(e) {
		window.setTimeout(function () {
			if(doubleClick) {
				doubleClick--;
				return;
			}
			editEntityAttributes(e);
		}, sigma.settings.doubleClickTimeout + 100);
	});
	
	s.bind('clickEdge', editEntityAttributes);
	
	s.bind('doubleClickNode', async function(e) {
		console.log(e.type, e.data.node.label, e.data.captor);
		doubleClick = 2;
		if (e.data.node.id != NEW_ENTITY_ID && e.data.node.id != SEARCH_ENTITY_ID) {
			s.graph.clear();
			if (document.querySelector('input[type="radio"][name="cfgPersonDblClk"]:checked').value == "drel") {
				s.graph.read(await invokeService("basic/retrieveRelations", {startPersonId : e.data.node.id}));
			}
			else if (document.querySelector('input[type="radio"][name="cfgPersonDblClk"]:checked').value == "parceners") {
				s.graph.read(await invokeService("basic/retrieveParceners", {startPersonId : e.data.node.id}));
			}
			else {
				if (document.querySelector('input[type="radio"][name="viewOrExportTree"]:checked').value == "view") {
					s.graph.read(await invokeService("basic/retrieveTree", {startPersonId : e.data.node.id, 
						maxDepth : parseInt(document.getElementById("depth").options[document.getElementById("depth").selectedIndex].value)}));
				} else {
					data = await invokeService("basic/exportTree", {startPersonId : e.data.node.id, 
						maxDepth : parseInt(document.getElementById("depth").options[document.getElementById("depth").selectedIndex].value)}, timeout_ms=200000)
					// https://stackoverflow.com/questions/46638343/download-csv-file-as-response-on-ajax-request
					const downloadData = (function() {
					    const a = document.createElement("a");
					    document.body.appendChild(a);
					    a.style = "display: none";
					    return function (data, fileName) {
					        const blob = new Blob([data], {type: "octet/stream"}),
					            url = window.URL.createObjectURL(blob);
					        a.href = url;
					        a.download = fileName;
					        a.click();
					            setTimeout(function() {
					                window.URL.revokeObjectURL(url);
					            }, 100);
					    };
					}());

					downloadData(data, "tree.csv");
				}
			}
			s.refresh();
		}
		clearSidebar();
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

	if (startProjectId != null) {
		isAppReadOnly = await invokeService("projectuser/switchProject", startProjectId);
		document.getElementById("project").value = startProjectId;
		if (startPersonId != null) {
			s.graph.read(await invokeService("basic/retrieveRelations", {startPersonId : startPersonId}));
		}
	}
	enableDisableRWFunctions();

}

async function editEntityAttributes(e) {
	var attributeValueVOList, rightBarElement, valueElement, attributeValueBlockElement, actionButtonElement, addButtonElement;
	var person1Node, person2Node, retrieveRelationAttributesResponseVO,  person1GenderDVId, person2GenderDVId, person1ForPerson2SelectElement,  person2ForPerson2SelectElement;
	var retrievePersonAttributesResponseVO, photoImageElement;
	var person1AsPerRelationId;
	
	if (highlightedEntity != undefined) {
		highlightedEntity.color = DEFAULT_COLOR;
	}
	
	attributeValueVOList = [];
	action = ACTION_SAVE;
	clearSidebar();
	if (e.type == "clickNode") {
		isPersonNode = true;
		highlightedEntity = e.data.node;
		console.log(e.type, e.data.node.label, e.data.captor);
		if (e.data.node.id == SEARCH_ENTITY_ID) {
			action = ACTION_SEARCH;
		}
		if (e.data.node.id > 0) {
			retrievePersonAttributesResponseVO = await invokeService("basic/retrievePersonAttributes", e.data.node.id);
			photoImageElement = document.getElementById("sidebarphotoImg");
			document.getElementById("sidebarphotoInput").value = '';
			photoImageElement.parentElement.setAttribute("style", "display: block");
			if (retrievePersonAttributesResponseVO.photo == null) {
				photoImageElement.removeAttribute("src");
			}
			else {
				photoImageElement.setAttribute("src", "data:image/jpg;base64," + retrievePersonAttributesResponseVO.photo);
			}
			attributeValueVOList = retrievePersonAttributesResponseVO.attributeValueVOList;
		}
	}
	else {
		isPersonNode = false;
		highlightedEntity = e.data.edge;
		console.log(e.type, e.data.edge.label, e.data.captor);
		if (e.data.edge.id > 0) {
			retrieveRelationAttributesResponseVO = await invokeService("basic/retrieveRelationAttributes", e.data.edge.id);
			attributeValueVOList = retrieveRelationAttributesResponseVO.attributeValueVOList;
			person1GenderDVId = retrieveRelationAttributesResponseVO.person1GenderDVId;
			person2GenderDVId = retrieveRelationAttributesResponseVO.person2GenderDVId;
			person1AsPerRelationId = retrieveRelationAttributesResponseVO.person1Id;
		}
		else {
			if (!e.data.edge.id.startsWith("S")) {
				alert("Debug: Scenario when e.data.edge.id equals 0!");
			}
			return;
		}
	}
	
	highlightedEntity.color = HIGHLIGHT_COLOR;
	
	rightBarElement = document.getElementById("sidebarbody");
	/* Current attribute values from back-end */
	for (let attributeValueVO of attributeValueVOList) {
		if (attributeValueVO.attributeDvId == PERSON_ATTRIBUTE_DV_ID_LABEL) {
			highlightedEntity.label = attributeValueVO.attributeValue;
		}
		attributeValueBlockElement = document.createElement("fieldset");
		rightBarElement.appendChild(attributeValueBlockElement);
		attributeValueBlockElement.appendChild(document.createTextNode(attributeValueVO.attributeName));
		createAttributeBlock(attributeValueBlockElement, attributeValueVO, action);
	}
	/* Mandatory attributes for new entity */
	if (action == ACTION_SAVE && attributeValueVOList.length == 0) {
		for (let attributeDomainValueVO of (isPersonNode ? paDomainValueVOList : raDomainValueVOList)) {
			if (attributeDomainValueVO.inputMandatory) {
				attributeValueBlockElement = document.createElement("fieldset");
				rightBarElement.appendChild(attributeValueBlockElement);
				attributeValueBlockElement.appendChild(document.createTextNode(attributeDomainValueVO.value));
				createAttributeBlock(attributeValueBlockElement, {attributeDvId: attributeDomainValueVO.id}, action);
			}
		}
	}
	s.refresh();
	
	for (let attributeValueBlkElement of rightBarElement.querySelectorAll("fieldset[attributedvid]")) {
		attributeDvId = parseInt(attributeValueBlkElement.getAttribute("attributedvid"));
		if (attributeDvId == RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2) {
			person1ForPerson2SelectElement = attributeValueBlkElement.querySelector("select[name=RelName]");
		}
		else if (attributeDvId == RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1) {
			person2ForPerson1SelectElement = attributeValueBlkElement.querySelector("select[name=RelName]");
		}
	}
	
	/* Set default value for person2ForPerson1 */
	/* Beware: This code may go out of sync with what is configured */
	if (!isPersonNode && person1GenderDVId != null && person2GenderDVId != null) {
		person1ForPerson2SelectElement.onchange = function() {
			person1ForPerson2RelationDVId = parseInt(person1ForPerson2SelectElement.options[person1ForPerson2SelectElement.selectedIndex].value);
			switch(true) {
				case person1GenderDVId == GENDER_MALE_DV_ID && person1ForPerson2RelationDVId == RELATION_NAME_HUSBAND_DV_ID && person2GenderDVId == GENDER_FEMALE_DV_ID:
					person2ForPerson1SelectElement.value = RELATION_NAME_WIFE_DV_ID;
					break;
				case person1GenderDVId == GENDER_FEMALE_DV_ID && person1ForPerson2RelationDVId == RELATION_NAME_WIFE_DV_ID && person2GenderDVId == GENDER_MALE_DV_ID:
					person2ForPerson1SelectElement.value = RELATION_NAME_HUSBAND_DV_ID;
					break;
				case (person1GenderDVId == GENDER_MALE_DV_ID && person1ForPerson2RelationDVId == RELATION_NAME_FATHER_DV_ID || person1GenderDVId == GENDER_FEMALE_DV_ID && person1ForPerson2RelationDVId == RELATION_NAME_MOTHER_DV_ID) && person2GenderDVId == GENDER_MALE_DV_ID:
					person2ForPerson1SelectElement.value = RELATION_NAME_SON_DV_ID;
					break;
				case (person1GenderDVId == GENDER_MALE_DV_ID && person1ForPerson2RelationDVId == RELATION_NAME_FATHER_DV_ID || person1GenderDVId == GENDER_FEMALE_DV_ID && person1ForPerson2RelationDVId == RELATION_NAME_MOTHER_DV_ID) && person2GenderDVId == GENDER_FEMALE_DV_ID:
					person2ForPerson1SelectElement.value = RELATION_NAME_DAUGHTER_DV_ID;
					break;
				case (person1GenderDVId == GENDER_MALE_DV_ID && person1ForPerson2RelationDVId == RELATION_NAME_SON_DV_ID || person1GenderDVId == GENDER_FEMALE_DV_ID && person1ForPerson2RelationDVId == RELATION_NAME_DAUGHTER_DV_ID) && person2GenderDVId == GENDER_MALE_DV_ID:
					person2ForPerson1SelectElement.value = RELATION_NAME_FATHER_DV_ID;
					break;
				case (person1GenderDVId == GENDER_MALE_DV_ID && person1ForPerson2RelationDVId == RELATION_NAME_SON_DV_ID || person1GenderDVId == GENDER_FEMALE_DV_ID && person1ForPerson2RelationDVId == RELATION_NAME_DAUGHTER_DV_ID) && person2GenderDVId == GENDER_FEMALE_DV_ID:
					person2ForPerson1SelectElement.value = RELATION_NAME_MOTHER_DV_ID;
					break;
				/* On error do nothing; Validations come later */
			}
		}
	}
	
	document.getElementById("sidebarbuttons").innerHTML = "<button id='addbutton'" + (action == ACTION_SAVE && isAppReadOnly ? " disabled" : "") + ">+</button><button id='actionbutton'" + (action == ACTION_SAVE && isAppReadOnly ? " disabled" : "") + ">" + action + "</button>";
	if (action == ACTION_SAVE) {
		if (isPersonNode) {
			document.getElementById("sidebarbuttons").innerHTML += "<button id='deletebutton'" + (isAppReadOnly || highlightedEntity.id == NEW_ENTITY_ID ? " disabled" : "") + ">Delete Person</button>";
			document.getElementById("deletebutton").onclick = async function() {
				if (confirm("Going to delete the Person.")) {
					await invokeService("basic/deletePerson", highlightedEntity.id);
					s.graph.dropNode(highlightedEntity.id);	// The node and each edge that is bound to it
					s.refresh();
					clearSidebar();
					alert("Person DELETED successfully");
				}
			};
		}
		else {
			document.getElementById("sidebarbuttons").innerHTML += "<button id='deletebutton'" + (isAppReadOnly ? " disabled" : "") + ">Delete Relation</button>";
			document.getElementById("deletebutton").onclick = async function() {
				if (confirm("Going to delete the Relation.")) {
					await invokeService("basic/deleteRelation", highlightedEntity.id);
					s.graph.dropEdge(highlightedEntity.id); // The edge
					s.refresh();
					clearSidebar();
					alert("Relation DELETED successfully");
				}
			};
		}
	}
	addButtonElement = document.getElementById("addbutton");
	actionButtonElement = document.getElementById("actionbutton");
	
	addButtonElement.onclick = function() {
		var selectElement, optionElement;
		attributeValueBlockElement = document.createElement("fieldset");
		rightBarElement.appendChild(attributeValueBlockElement);
		
		selectElement = (isPersonNode ? paSelectElement : raSelectElement).cloneNode(true);
		if (action == ACTION_SEARCH) {
			for (optionElement of paSearchXtraOptions) {
				selectElement.appendChild(optionElement);
			}
		}
		attributeValueBlockElement.appendChild(selectElement);
		if (isPersonNode && action == ACTION_SEARCH) {
			selectElement.value = PERSON_ATTRIBUTE_DV_ID_LABEL;
			createAttributeBlock(attributeValueBlockElement, {attributeDvId: PERSON_ATTRIBUTE_DV_ID_LABEL}, action);
		}
		else {
			createAttributeBlock(attributeValueBlockElement, {attributeDvId: parseInt(selectElement.options[0].value)}, action);
		}
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
			createAttributeBlock(selectElement.parentElement, {attributeDvId: parseInt(selectElement.options[selectElement.selectedIndex].value)}, action);
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
				document.getElementById("sidebartitle").textContent = "Details of relation between " +
						(person1Node.id == person1AsPerRelationId ? person1Node.label + " and " + person2Node.label : person2Node.label + " and " + person1Node.label);
			}
			break;
		case ACTION_SEARCH:
			document.getElementById("sidebartitle").textContent = "Person Search Criteria";
			break;
	}
	
	actionButtonElement.onclick = async function() {
		var attributeValueVOList, attributeValueVO, saveAttributesRequestVO, inputElements;
		var attributeVsValueListMap, attributeDvId, attributeDomainValueVO;
		var ind1, ind2, attributeValueNBlkList, searchedPersonId, saveAttributesResponseVO;
		var toInsertAttributeValueDummyId, relationPerson1ForPerson2, relationPerson2ForPerson1, relationSubType;
		var searchResultsWindowElement, searchResultsTableElement, searchCloseButtonElement, searchReturnButtonElement, searchResultsVO, searchResultsList, srInputElement, srRowNo, searchMessageElement;
		var relationVO, personIdsList, photoInputElement, file;
		
		attributeValueVOList = [];
		attributeVsValueListMap = new Map();
		saveAttributesRequestVO = {entityId: highlightedEntity.id, attributeValueVOList: attributeValueVOList};
		toInsertAttributeValueDummyId = 1;
		
		for (let attributeValueBlkElement of rightBarElement.querySelectorAll("fieldset[attributedvid]")) {
			inputElements = attributeValueBlkElement.querySelectorAll("input,select:not([name=attributenames])");
			attributeDvId = parseInt(attributeValueBlkElement.getAttribute("attributedvid"));
			attributeValueVO = {attributeDvId: attributeDvId, id: null, attributeValue: (inputElements[0].tagName == "INPUT" ? inputElements[0].value : inputElements[0].options[inputElements[0].selectedIndex].value), valueAccurate: null, startDate: null, endDate: null};
			if (action == ACTION_SAVE) {
				if (attributeValueBlkElement.hasAttribute("attributevalueid")) {
					attributeValueVO.id = parseInt(attributeValueBlkElement.getAttribute("attributevalueid"));
				}
				else {
					toInsertAttributeValueDummyId--;
					attributeValueVO.id = toInsertAttributeValueDummyId;
					attributeValueBlkElement.setAttribute("attributevalueid", toInsertAttributeValueDummyId);
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
				/* Beware: The following validation may go out of sync with what is configured */
				person1ForPerson2RelationDVId = parseInt(person1ForPerson2SelectElement.options[person1ForPerson2SelectElement.selectedIndex].value);
				person2ForPerson1RelationDVId = parseInt(person2ForPerson1SelectElement.options[person2ForPerson1SelectElement.selectedIndex].value);
				if (person1GenderDVId == GENDER_MALE_DV_ID && person1ForPerson2RelationDVId != RELATION_NAME_HUSBAND_DV_ID && person1ForPerson2RelationDVId != RELATION_NAME_FATHER_DV_ID && person1ForPerson2RelationDVId != RELATION_NAME_SON_DV_ID ||
					person1GenderDVId == GENDER_FEMALE_DV_ID && person1ForPerson2RelationDVId != RELATION_NAME_WIFE_DV_ID && person1ForPerson2RelationDVId != RELATION_NAME_MOTHER_DV_ID && person1ForPerson2RelationDVId != RELATION_NAME_DAUGHTER_DV_ID ||
					person2GenderDVId == GENDER_MALE_DV_ID && person2ForPerson1RelationDVId != RELATION_NAME_HUSBAND_DV_ID && person2ForPerson1RelationDVId != RELATION_NAME_FATHER_DV_ID && person2ForPerson1RelationDVId != RELATION_NAME_SON_DV_ID ||
					person2GenderDVId == GENDER_FEMALE_DV_ID && person2ForPerson1RelationDVId != RELATION_NAME_WIFE_DV_ID && person2ForPerson1RelationDVId != RELATION_NAME_MOTHER_DV_ID && person2ForPerson1RelationDVId != RELATION_NAME_DAUGHTER_DV_ID) {
						alert("Relation is not appropriate for the Gender");
						return;
				}
			}
		}
		switch(action) {
			case ACTION_SAVE:
				if (isPersonNode) {
					photoInputElement = document.getElementById("sidebarphotoInput");
					if ('files' in photoInputElement) {
						switch(photoInputElement.files.length) {
							case 0:
								break;
							case 1:
								file = photoInputElement.files[0];
								if (!file.name.toLowerCase().endsWith(".jpg") && !file.name.toLowerCase().endsWith(".png")) {
									alert("Only JPG and PNG files are allowed");
									return;
								}
								if (file.size > 1048576) { // 1 MB
									alert("Only file of maximum size 1 MB allowed");
									return;
								}
								saveAttributesRequestVO.photo = await fileToByteArray(file);
								break;
							default:
								alert("Only one photo is permitted");
								return;
						}
					}
				}
				saveAttributesResponseVO = await invokeService((isPersonNode ? "basic/savePersonAttributes" : "basic/saveRelationAttributes"), saveAttributesRequestVO);
				toInsertAttributeValueDummyId = 1;
				for (let insertedAttributeValueId of saveAttributesResponseVO.insertedAttributeValueIdList) {
					toInsertAttributeValueDummyId--;
					attributeValueBlkElement = rightBarElement.querySelector("fieldset[attributedvid][attributevalueid='" + toInsertAttributeValueDummyId + "']");
					if (attributeValueBlkElement == null) {
						alert("Saved values are not properly refreshed on the screen. Please click on the " + (isPersonNode ? "node" : "edge") + " again.");
						return;
					}
					attributeValueBlkElement.setAttribute("attributevalueid", insertedAttributeValueId);
				}
				alert("Saved");
				document.getElementById("deletebutton").removeAttribute("disabled");
				if (isPersonNode && highlightedEntity.id == NEW_ENTITY_ID) {
					s.graph.dropNode(NEW_ENTITY_ID);
					s.graph.addNode({
						id: saveAttributesResponseVO.entityId,
						size: 5.0,
						x: Math.random() * 100,
						y: Math.random() * 100,
						dX: 0,
						dY: 0,
						type: 'goo'
					});
					highlightedEntity = s.graph.nodes(saveAttributesResponseVO.entityId);
					highlightedEntity.color = HIGHLIGHT_COLOR;
				}
				highlightedEntity.label = (isPersonNode ? attributeVsValueListMap.get(PERSON_ATTRIBUTE_DV_ID_LABEL)[0].attributeValueVO.attributeValue :
					domainValueVOMap.get(parseInt(attributeVsValueListMap.get(RELATION_ATTRIBUTE_DV_ID_PERSON1_FOR_PERSON2)[0].attributeValueVO.attributeValue)).value + "-" + domainValueVOMap.get(parseInt(attributeVsValueListMap.get(RELATION_ATTRIBUTE_DV_ID_PERSON2_FOR_PERSON1)[0].attributeValueVO.attributeValue)).value);
				s.refresh();
				break;
			case ACTION_SEARCH:
				if (attributeValueVOList.length == 0) {
					alert("Specify search criteria");
					return;
				}
				searchResultsVO = await invokeService("basic/searchPerson", attributeValueVOList);
				if (searchResultsVO.resultsList == null) {
					searchedPersonId = NEW_ENTITY_ID;
				}
				
				if (searchedPersonId == NEW_ENTITY_ID) {
					alert("Person with the specified properties could not be found");
				}
				else {
					searchResultsList = searchResultsVO.resultsList;
					searchResultsWindowElement = document.getElementById("searchresultswindow");
					searchMessageElement = document.getElementById("searchMessage");
					searchResultsTableElement = document.getElementById("searchresultstable");
					searchCloseButtonElement = document.getElementById("searchCloseButton");
					searchReturnButtonElement = document.getElementById("searchReturnButton");
					/* Open the modal window and display search results */
					searchResultsWindowElement.style.display = "block";
					/* When the user clicks anywhere outside of the modal, close it
					window.onclick = function(event) {
						if (event.target == searchResultsWindowElement) {
							searchResultsWindowElement.style.display = "none";
						}
					} */
					searchResultsTableElement.innerHTML = "";
					if (searchResultsVO.morePresentInDb) {
						searchMessageElement.innerText = "Showing partial result of " + (searchResultsList.length - 1) + " persons. Provide more restricting criteria to limit the results."
					}
					else {
						searchMessageElement.innerText = "Showing " + (searchResultsList.length - 1) + " persons."
					}
					
					srRowNo = -1;
					for (let attributesList of searchResultsList) {
						srRowNo++;
						srRowElement = document.createElement("tr");
						srCellElement = document.createElement("td");
						srRowElement.appendChild(srCellElement);
						if (srRowNo > 0) { // To exclude heading
							srInputElement = document.createElement("input");
							srCellElement.appendChild(srInputElement);
							srInputElement.setAttribute("type", "radio");
							srInputElement.setAttribute("name", "searchresultradio");
							srInputElement.setAttribute("value", attributesList[0]); // personId
						}
						if (srRowNo == 1) { // First data row
							srInputElement.checked=true;
						}
						searchResultsTableElement.appendChild(srRowElement);
						for (let attributeValue of attributesList) {
							srCellElement = document.createElement("td");
							srRowElement.appendChild(srCellElement);
							srCellElement.appendChild(document.createTextNode(attributeValue));
						}
					}
					searchCloseButtonElement.onclick = function() {
						searchResultsWindowElement.style.display = "none";
					}
					searchReturnButtonElement.onclick = async function() {
						searchedPersonId = document.querySelector('input[type="radio"][name="searchresultradio"]:checked').value;
						searchResultsWindowElement.style.display = "none";
						s.graph.dropNode(SEARCH_ENTITY_ID);
						if (s.graph.nodes(searchedPersonId) != null) {
							alert("Person exists already");
							s.renderers[0].dispatchEvent('clickNode', {node: s.graph.nodes(searchedPersonId)});
						}
						else {
							personIdsList = [];
							for (let node of s.graph.nodes()) {
								if (node.id != NEW_ENTITY_ID) {
									personIdsList.push(node.id);
								}
							}
							s.graph.addNode({
								id: searchedPersonId,
								size: 5.0,
								x: Math.random() * 100,
								y: Math.random() * 100,
								type: 'goo'
							});
							for (let relationVO of await invokeService("basic/retrieveRelationsBetween", {end1PersonId : searchedPersonId, end2PersonIdsList : personIdsList})) {
								s.graph.addEdge({
									id: relationVO.id,
									source: relationVO.source,
									target: relationVO.target,
									label: relationVO.label,
									size: relationVO.size,
									type: 'goo'
								});
							}
							s.renderers[0].dispatchEvent('clickNode', {node: s.graph.nodes(searchedPersonId)});
						}
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

function createAttributeBlock(attributeValueBlockElement, attributeValueVO, action) {
	var valueElement, isAccurateElement, startDateElement, endDateElement;
	var startDatePicker, endDatePicker, attributeDomainValueVO;
	var deleteBlockImageElement, regEx;
	
	if (attributeValueVO.attributeDvId > 0) {
		attributeDomainValueVO = domainValueVOMap.get(attributeValueVO.attributeDvId);
	}
	else {
		attributeDomainValueVO = {attributeDomain: ""}
	}
	attributeValueBlockElement.setAttribute("attributedvid", attributeValueVO.attributeDvId);
	if (attributeValueVO.id != undefined) {
		attributeValueBlockElement.setAttribute("attributevalueid", attributeValueVO.id);
	}
	if (action == ACTION_SAVE && isAppReadOnly) {
		attributeValueBlockElement.setAttribute("disabled", "");
	}
	else {
		attributeValueBlockElement.appendChild(document.createTextNode("     "));
		deleteBlockImageElement = document.createElement("img");
		attributeValueBlockElement.appendChild(deleteBlockImageElement);
		deleteBlockImageElement.setAttribute("src","img/delete.png");
		deleteBlockImageElement.setAttribute("alt","Delete Property");
		deleteBlockImageElement.setAttribute("width","5%");
		deleteBlockImageElement.setAttribute("height","3%");
		deleteBlockImageElement.onclick = async function() {
			if (confirm("Going to delete the Property.")) {
				attributeValueBlockElement.remove();
			}
		};
	}
	
	attributeValueBlockElement.appendChild(document.createElement("br"));
	
	attributeValueBlockElement.appendChild(document.createTextNode("Value: "));
	
	if (attributeDomainValueVO.attributeDomain == "") {
		valueElement = document.createElement("input");
		valueElement.setAttribute("type","text");
		if (attributeValueVO.attributeValue != undefined) {
			valueElement.setAttribute("value", attributeValueVO.attributeValue);
		}
		if (attributeDomainValueVO.validationJsRegEx != null && attributeDomainValueVO.validationJsRegEx != "") {
			regEx = new RegExp(attributeDomainValueVO.validationJsRegEx, 'u');
			valueElement.setAttribute("onkeypress","return blockSpecialCharOnKeyPress(event, " + regEx + ")");
			regEx = new RegExp('^(' + attributeDomainValueVO.validationJsRegEx + ')*$', 'u');
			valueElement.setAttribute("onpaste","return blockSpecialCharOnPaste(event, " + regEx + ")");
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
		startDateElement.classList.add("startenddate");
		startDatePicker = new Pikaday({field: startDateElement, theme: "dark-theme", minDate: new Date(1001, 0, 1)});
		if (attributeValueVO.startDate != undefined) {
			startDateElement.setAttribute("value", isoToPikadayFormat(attributeValueVO.startDate));
		}
		
		attributeValueBlockElement.appendChild(document.createElement("br"));
		
		attributeValueBlockElement.appendChild(document.createTextNode("End Date: "));
		endDateElement = document.createElement("input");
		attributeValueBlockElement.appendChild(endDateElement);
		endDateElement.setAttribute("type","text");
		endDateElement.classList.add("startenddate");
		endDatePicker = new Pikaday({field: endDateElement, theme: "dark-theme", minDate: new Date(1001, 0, 1)});
		if (attributeValueVO.endDate != undefined) {
			endDateElement.setAttribute("value", isoToPikadayFormat(attributeValueVO.endDate));
		}
	}
	
	if (attributeDomainValueVO.inputAsAttribute) {
		valueElement.removeAttribute("disabled");
		isAccurateElement.removeAttribute("disabled");
		if (attributeDomainValueVO.repetitionType != FLAG_ATTRIBUTE_REPETITION_NOT_ALLOWED) {
			startDateElement.removeAttribute("disabled");
			endDateElement.removeAttribute("disabled");
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
			x: Math.random() * 100,
			y: Math.random() * 100,
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
			x: Math.random() * 100,
			y: Math.random() * 100,
			type: 'goo'
		});
	}
	s.graph.nodes(SEARCH_ENTITY_ID).label = 'Yet to be Searched';
	s.renderers[0].dispatchEvent('clickNode', {node: s.graph.nodes(SEARCH_ENTITY_ID)});
}

function clearGraph() {
	s.graph.clear();
	s.refresh();
	clearSidebar();
}

function clearSidebar() {
	document.getElementById("sidebartitle").innerHTML = "";
	document.getElementById("sidebarbuttons").innerHTML = "";
	document.getElementById("sidebarbody").innerHTML = "";
	document.getElementById("sidebarphoto").setAttribute("style", "display: none");
}

function relatePersons() {
	var rightBarElement, fatherPersonElement, motherPersonElement, childPersonElement, husbandPersonElement, wifePersonElement;
	var actionButtonElement, parentChildRadioElement;
	
	clearSidebar();
	document.getElementById("sidebarbuttons").innerHTML = "<button id='actionbutton'>Relate</button>";
	document.getElementById("sidebartitle").textContent = "Related Persons";

	rightBarElement = document.getElementById("sidebarbody");
	rightBarElement.innerHTML = `<input type="radio" id="PC" name="relGrp" value="PC" checked onclick="switchRelGrp(this);"/>
					<label for="PC">Parent-Child</label>
					<input type="radio" id="Sp" name="relGrp" value="Sp" onclick="switchRelGrp(this);"/>
					<label for="Sp">Spouse</label>
					<div id="relatedPersons"></div>`
	parentChildRadioElement = document.getElementById("PC");
	switchRelGrp(parentChildRadioElement);
	
	actionButtonElement = document.getElementById("actionbutton");
	actionButtonElement.onclick = async function() {
		var person1Id, person2Id, relationId, fatherPersonElement, motherPersonElement, childPersonElement, husbandPersonElement, wifePersonElement;
		var selectedRelGrp;
		
		selectedRelGrp = document.querySelector('input[name="relGrp"]:checked')?.value;
		
		if (selectedRelGrp == "PC") {
			fatherPersonElement = document.getElementById("fatherPerson");
			motherPersonElement = document.getElementById("motherPerson");
			childPersonElement = document.getElementById("childPerson");
			fatherPersonId = parseInt(fatherPersonElement.options[fatherPersonElement.selectedIndex].value);
			motherPersonId = parseInt(motherPersonElement.options[motherPersonElement.selectedIndex].value);
			childPersonId = parseInt(childPersonElement.options[childPersonElement.selectedIndex].value);
			if (fatherPersonId == childPersonId || motherPersonId == childPersonId) {
				alert("Same person cannot be part of a relation");
				return;
			}
			if (fatherPersonId == -1 && motherPersonId == -1) {
				alert("One of Father, Mother is required");
				return;
			}
			if (motherPersonId != -1) {
				relationId = await saveRelation({person1Id: motherPersonId, person2Id: childPersonId, person1ForPerson2: RELATION_NAME_MOTHER_DV_ID});
			}
			// Beware: The above and below saveRelation are NOT part of a single TRANSACTION
			if (fatherPersonId != -1) {
				relationId = await saveRelation({person1Id: fatherPersonId, person2Id: childPersonId, person1ForPerson2: RELATION_NAME_FATHER_DV_ID});
			}
		} else {
			husbandPersonElement = document.getElementById("husbandPerson");
			wifePersonElement = document.getElementById("wifePerson");
			husbandPersonId = parseInt(husbandPersonElement.options[husbandPersonElement.selectedIndex].value);
			wifePersonId = parseInt(wifePersonElement.options[wifePersonElement.selectedIndex].value);
			relationId = await saveRelation({person1Id: husbandPersonId, person2Id: wifePersonId, person1ForPerson2: '' + RELATION_NAME_HUSBAND_DV_ID});
		}
	}
}

async function saveRelation(relatedPersonsVO) {
	relationVO = await invokeService("basic/saveRelation", relatedPersonsVO);
	s.graph.addEdge({
		id: relationVO.id,
		source: relationVO.source,
		target: relationVO.target,
		label: relationVO.label,
		size: relationVO.size,
		type: 'goo'
	});
	s.renderers[0].dispatchEvent('clickEdge', {edge: s.graph.edges(relationVO.id)});
}

async function switchRelGrp(clickedRadioElement) {
	var relatedPersonsElement;
	
	await createPersonDropdown();
	relatedPersonsElement = document.getElementById("relatedPersons");
	relatedPersonsElement.innerHTML = "";
	
	if (clickedRadioElement.value == "PC") {
		
		relatedPersonsElement.appendChild(document.createTextNode("Father: "));
		fatherPersonElement = malePersonsSelectElement.cloneNode(true);
		makeDropdownOptional(fatherPersonElement);
		fatherPersonElement.setAttribute("id", "fatherPerson");
		relatedPersonsElement.appendChild(fatherPersonElement);
		relatedPersonsElement.appendChild(document.createElement("br"));
		
		relatedPersonsElement.appendChild(document.createTextNode("Mother: "));
		motherPersonElement = femalePersonsSelectElement.cloneNode(true);
		makeDropdownOptional(motherPersonElement);
		motherPersonElement.setAttribute("id", "motherPerson");
		relatedPersonsElement.appendChild(motherPersonElement);
		relatedPersonsElement.appendChild(document.createElement("br"));
		
		relatedPersonsElement.appendChild(document.createTextNode("Son/Daughter: "));
		childPersonElement = allPersonsSelectElement.cloneNode(true);
		childPersonElement.setAttribute("id", "childPerson");
		relatedPersonsElement.appendChild(childPersonElement);
		relatedPersonsElement.appendChild(document.createElement("br"));
	}
	else {
		relatedPersonsElement.appendChild(document.createTextNode("Husband: "));
		husbandPersonElement = malePersonsSelectElement.cloneNode(true);
		husbandPersonElement.setAttribute("id", "husbandPerson");
		relatedPersonsElement.appendChild(husbandPersonElement);
		relatedPersonsElement.appendChild(document.createElement("br"));
		
		relatedPersonsElement.appendChild(document.createTextNode("Wife: "));
		wifePersonElement = femalePersonsSelectElement.cloneNode(true);
		wifePersonElement.setAttribute("id", "wifePerson");
		relatedPersonsElement.appendChild(wifePersonElement);
		relatedPersonsElement.appendChild(document.createElement("br"));
	}
}

async function ascertainRelation() {
	var actionButtonElement;
	
	clearSidebar();
	document.getElementById("sidebarbuttons").innerHTML = "<button id='actionbutton'>Ascertain</button>";
	document.getElementById("sidebartitle").textContent = "Ascertain Relation";

	await getPersonsPair();
	
	actionButtonElement = document.getElementById("actionbutton");
	actionButtonElement.onclick = async function() {
		var person1Id, person2Id, relationId, person1Element, person2Element;
		var exclrelElement, excludeRelationIdCsv;
		person1Element = document.getElementById("person1");
		person2Element = document.getElementById("person2");
		exclrelElement = document.getElementById("exclrelids");
		person1Id = parseInt(person1Element.options[person1Element.selectedIndex].value);
		person2Id = parseInt(person2Element.options[person2Element.selectedIndex].value);
		excludeRelationIdCsv = exclrelElement.value;
		if (person1Id == person2Id) {
			alert("Same person cannot be part of a relation");
			return;
		}
		s.graph.clear();
		s.graph.read(await invokeService("algo/retrieveRelationPath", {person1Id: person1Id, person2Id: person2Id, excludeRelationIdCsv: excludeRelationIdCsv}), timeout_ms=50000);
		s.refresh();
		await getPersonsPair(person1Id, person2Id, excludeRelationIdCsv);
	}
}

async function getPersonsPair(person1Id, person2Id, excludeRelationIdCsv) {
	var rightBarElement, person1Element, person2Element;
	var exclrelElement;

	await createPersonDropdown();
	rightBarElement = document.getElementById("sidebarbody");
	
	rightBarElement.innerHTML = "";
	rightBarElement.appendChild(document.createTextNode("Person 1: "));
	person1Element = allPersonsSelectElement.cloneNode(true);
	person1Element.setAttribute("id", "person1");
	if (person1Id != null) {
		person1Element.value = person1Id;
	}
	rightBarElement.appendChild(person1Element);
	
	rightBarElement.appendChild(document.createElement("br"));
	rightBarElement.appendChild(document.createTextNode("Person 2: "));
	person2Element = allPersonsSelectElement.cloneNode(true);
	person2Element.setAttribute("id", "person2");
	if (person2Id != null) {
		person2Element.value = person2Id;
	}
	rightBarElement.appendChild(person2Element);
	
	rightBarElement.appendChild(document.createElement("br"));
	rightBarElement.appendChild(document.createTextNode("Exclude Relations: "));
	exclrelElement = document.createElement("input");
	exclrelElement.setAttribute("type","text");
	exclrelElement.setAttribute("id", "exclrelids");
	if (excludeRelationIdCsv != null) {
		exclrelElement.value = excludeRelationIdCsv;
	}
	rightBarElement.appendChild(exclrelElement);
	
}

async function createPersonDropdown()
{
	var optionElement, allPersonIds, allGenders, ind;
	
	allPersonsSelectElement = document.createElement("select");
	allPersonsSelectElement.setAttribute("name","persons");
	allPersonsSelectElement.classList.add("propdrop");
	malePersonsSelectElement = allPersonsSelectElement.cloneNode(true);
	femalePersonsSelectElement = allPersonsSelectElement.cloneNode(true);
	
	allPersonIds = [];
	for (let node of s.graph.nodes()) {
		if (node.id != NEW_ENTITY_ID && node.id != SEARCH_ENTITY_ID) {
			allPersonIds.push(node.id);
		}
	}
	if (allPersonIds.length == 0) {
		document.getElementById("actionbutton").disabled = true;
		return;
	}
	allGenders = await invokeService("basic/retrieveGendersOfPersons", allPersonIds);
	
	ind = -1;
	for (let node of s.graph.nodes()) {
		if (node.id != NEW_ENTITY_ID && node.id != SEARCH_ENTITY_ID) {
			optionElement = document.createElement("option");
			optionElement.setAttribute("value", node.id);
			optionElement.appendChild(document.createTextNode(node.label + " (" + node.id + ")"));
			allPersonsSelectElement.appendChild(optionElement);
			ind++;
			if (allGenders[ind] == GENDER_MALE_DV_ID) {
				malePersonsSelectElement.appendChild(optionElement.cloneNode(true));
			} else if (allGenders[ind] == GENDER_FEMALE_DV_ID) {
				femalePersonsSelectElement.appendChild(optionElement.cloneNode(true));
			}
		}
	}
}

function makeDropdownOptional(ddElement) {
	var optionElement;
	optionElement = document.createElement("option");
	optionElement.setAttribute("value", -1);
	optionElement.appendChild(document.createTextNode("-- Select --"));
	ddElement.prepend(optionElement);
}

function printGraph() {
	s.toSVG({download: true, filename: 'familytree.svg', labels: true, size: 1000});
	// s.renderers[0].snapshot({download: true});
}

function enDisableDepth(clickedRadioElement) {
	if (clickedRadioElement.value == "ftree") {
		document.getElementById("depth").removeAttribute("disabled");
		document.getElementById("viewTree").removeAttribute("disabled");
		document.getElementById("exportTree").removeAttribute("disabled");
	}
	else {
		document.getElementById("depth").setAttribute("disabled","");
		document.getElementById("viewTree").setAttribute("disabled","");
		document.getElementById("exportTree").setAttribute("disabled","");
	}
}

async function switchProject() {
	isAppReadOnly = await invokeService("projectuser/switchProject", document.getElementById("project").value);
	enableDisableRWFunctions();
	alert("Project switched successfully");
}

async function logout() {
	await invokeService("projectuser/preLogout", "");
	window.location.href = "./logout";
	/* postLogin will now be called which in-turn will take care of enableDisableRWFunctions */
}

function enableDisableRWFunctions() {
	for (let buttonElement of document.querySelectorAll("div#leftbarbuttons > button[rel-modify-data]")) {
		if (isAppReadOnly) {
			buttonElement.setAttribute("disabled","");
		}
		else {
			buttonElement.removeAttribute("disabled");
		}
	}
}

async function createProject() {
	var projectName;
	
	projectName = "";
	while (projectName == "") {
		projectName = prompt("Please enter a name for the project", "My Clan");
		if (projectName == null) {	/* Cancel Clicked */
			return;
		}
	}
	document.getElementById("project").value = 	await invokeService("projectuser/createProject", projectName);
	isAppReadOnly = false;
	enableDisableRWFunctions();
	alert("Project created successfully");
}

function loginLogout(loggedInUser) {
	document.getElementById("user").value = loggedInUser;
	if (loggedInUser != null && loggedInUser != "") {
		for (var element of document.getElementsByClassName("unauthenticated")) {
			element.style.display = "none";
		}
		for (var element of document.getElementsByClassName("authenticated")) {
			element.style.display = "inline-block";
		}
	}
	else {
		for (var element of document.getElementsByClassName("unauthenticated")) {
			element.style.display = "inline-block";
		}
		for (var element of document.getElementsByClassName("authenticated")) {
			element.style.display = "none";
		}
	}
}

function blockSpecialCharOnKeyPress(e, regEx) {
	if (! regEx.test(e.key)) {	/* document.all ? e.keyCode : e.which); */
		if (e.preventDefault) {
			e.preventDefault();
		}
		else {
			e.returnValue = false;
		}
	}
}

function blockSpecialCharOnPaste(e, regEx) {
	if (e.clipboardData) {
		content = e.clipboardData.getData('text/plain');
	}
	else if (window.clipboardData) {
		content = window.clipboardData.getData('Text');
	}
	if (! regEx.test(content)) {
		if (e.preventDefault) {
			e.preventDefault();
		}
		else {
			e.returnValue = false;
		}
	}
}

function nodeToFocus() {
	s.camera.goTo({x:0, y:0, ratio:1});
}

async function uploadPrData() {
	var actionButtonElement, rightBarElement, pRDataCsvInputElement, formData;
	
	clearSidebar();
	document.getElementById("sidebarbuttons").innerHTML = "<button id='actionbutton'>Upload</button>";
	document.getElementById("sidebartitle").textContent = "Upload Persons & Relations";

	rightBarElement = document.getElementById("sidebarbody");
	rightBarElement.innerHTML = "<input id='pRDataCsvInput' type='file' accept='.csv' />";
	
	actionButtonElement = document.getElementById("actionbutton");
	actionButtonElement.onclick = async function() {
		pRDataCsvInputElement = document.getElementById("pRDataCsvInput");
		if ('files' in pRDataCsvInputElement) {
			switch(pRDataCsvInputElement.files.length) {
				case 0:
					break;
				case 1:
					file = pRDataCsvInputElement.files[0];
					if (!file.name.toLowerCase().endsWith(".csv")) {
						alert("Only CSV files are allowed");
						return;
					}
					if (file.size > 1048576) { // 1 MB
						alert("Only file of maximum size 1 MB allowed");
						return;
					}
					let formData = new FormData();
					formData.append("file", pRDataCsvInput.files[0]);
					// TODO: Using invokeService instead of fetch?
					await fetch("basic/importPrData", {
						method: "POST",
						body: formData
					}).then(resp => {
						if(resp.ok) {
							alert("The file has been uploaded successfully.");
							return;
						}
						return resp.text().then(text => {throw new Error(text)});
					});
					break;
				default:
					alert("Only one file is permitted");
					return;
			}
		}
	}
}
