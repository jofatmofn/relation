async function invokeService(serviceUrl, requestVO)
{
	return new Promise(function(resolve, reject) 
	{
		var httpRequest, netAction, responseParseOut;
		blockPage();
		httpRequest = new XMLHttpRequest();
		httpRequest.timeout = 10000;
		httpRequest.onreadystatechange = function()
		{
			if(this.readyState == 4)
			{
				unblockPage();
				if(this.status == 200)
				{
					if (httpRequest.response == null) {
						netAction = 1;
					}
					else if (httpRequest.responseText == "") {
						netAction = 2;
					}
					else {
						if (httpRequest.getResponseHeader("content-type") == "application/json") {
							netAction = 3;
						}
						else {
							netAction = 4;
						}
					}
				}
				else if(this.status == 500)
				{
					netAction = 5;
				}
				else
				{
					netAction = 6;
				}
				/* ontimeout, if applicable, is triggered after this!!! */
			}
		};
		httpRequest.open('POST', serviceUrl);
		httpRequest.setRequestHeader('Content-Type', 'application/json');
		httpRequest.ontimeout = function () {
			netAction = 7;
	    };
		httpRequest.onloadend = function () {
			console.log("netAction: " + netAction);
			switch(netAction) {
				case 1:
					resolve(null);
					return;
				case 2:
					resolve("");
					return;
				case 3:
					resolve(JSON.parse(httpRequest.responseText));
					return;
				case 4:
					resolve(httpRequest.responseText);
					return;
				case 5:
					responseParseOut = JSON.parse(httpRequest.responseText);
					if (responseParseOut.message == "No message available") {
						reject("Error: " + responseParseOut.error + ". Message: " + responseParseOut.message);
					}
					else {
						reject(responseParseOut.message);
					}
					return;
				case 7:
					reject("Taking long duration. Try giving lesser workload.");
					return;
				case 6:
					reject(serviceUrl + " failed to run and returned with the status " + httpRequest.status);
					return;
			}
	    };
		if (requestVO == null) {
			httpRequest.send();
		}
		else if(requestVO == "") {
			httpRequest.send("{}");
		}
		else if(typeof requestVO == "string") {
			httpRequest.send(requestVO);
		}
		else {
			httpRequest.send(JSON.stringify(requestVO));
		}
	});
	
}

function blockPage() {
	var blockingElement;
	blockingElement = document.querySelector(".blockPage");
	if (blockingElement == null) {
		blockingElement = document.createElement("div");
		blockingElement.classList.add("blockPage");
		blockingElement.innerHTML = "<span>Please wait...<span>";
		document.getElementsByTagName("body")[0].appendChild(blockingElement);
	}
}

function unblockPage() {
	var blockingElement;
	blockingElement = document.querySelector(".blockPage");
	if (blockingElement != null) {
		blockingElement.remove();
	}
}
