var elems = document.getElementsByTagName("pre");
for (var i = 0; i < elems.length; i++) {
	hljs.highlightBlock(elems[i]);
}
