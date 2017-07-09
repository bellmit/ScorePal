'use strict';

/* way to include content from one HTML file in another, using ID to replace a node with content
<span id="includeone"/>
And at the beginning of the body of your page, you'll need to do this:
<body onLoad="clientSideInclude('includeone', 'includeone.html');">
*/

function clientSideInclude(id, url) {
    var req = false;
    // For Safari, Firefox, and other non-MS browsers
    if (window.XMLHttpRequest) {
        try {
            req = new XMLHttpRequest();
        } catch (e) {
            req = false;
        }
    } else if (window.ActiveXObject) {
        // For Internet Explorer on Windows
        try {
            req = new ActiveXObject("Msxml2.XMLHTTP");
        } catch (e) {
            try {
                req = new ActiveXObject("Microsoft.XMLHTTP");
            } catch (e) {
                req = false;
            }
        }
    }
    var element = document.getElementById(id);
    if (!element) {
        alert("Bad id " + id +
            "passed to clientSideInclude." +
            "You need a div or span element " +
            "with this id in your page.");
        return;
    }
    if (req) {
        // Synchronous request, wait till we have it all
        req.open('GET', url, false);
        req.send(null);
        element.innerHTML = req.responseText;
        // also remove any hidden atrribute from the source div to show it
        element.removeAttribute('hidden');
    } else {
        element.innerHTML =
            "Sorry, your browser does not support " +
            "XMLHTTPRequest objects. This page requires " +
            "Internet Explorer 5 or better for Windows, " +
            "or Firefox for any system, or Safari. Other " +
            "compatible browsers may also exist.";
    }
}