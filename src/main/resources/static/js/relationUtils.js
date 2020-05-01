async function invokeService(serviceUrl, requestVO)
{
	return new Promise(function(resolve, reject) 
	{
		var httpRequest;
		blockPage();
		httpRequest = new XMLHttpRequest();
		httpRequest.onreadystatechange = function()
		{
			if(this.readyState == 4)
			{
				unblockPage();
				if(this.status == 200)
				{
					if (httpRequest.response == null) {
						resolve(null);
					}
					else if (httpRequest.responseText == "") {
						resolve("");
					}
					else {
						resolve(JSON.parse(httpRequest.responseText));
					}
				}
				else if(this.status == 500)
				{
					reject(JSON.parse(httpRequest.responseText).message);
				}
				else
				{
					reject(serviceUrl + " failed to run and returned with the status "+this.status);
				}
			}
		};
		httpRequest.open('POST', serviceUrl);
		httpRequest.setRequestHeader('Content-Type', 'application/json');
		if (requestVO == null) {
			httpRequest.send();
		}
		else if(requestVO == "") {
			httpRequest.send("{}");
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
