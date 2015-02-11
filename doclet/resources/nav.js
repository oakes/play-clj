function goToHash() {
	if (window.location.hash != "") {
		document.getElementById("content-frame").src = window.location.hash.substr(1)
	}
}

function setHash(link) {
	window.parent.location.hash = link.getAttribute("newHash")
}
