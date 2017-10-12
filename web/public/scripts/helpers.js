'use strict';

/* way to include content from one HTML file in another, using ID to replace a node with content
<span id="includeone"/>
And at the beginning of the body of your page, you'll need to do this:
<body onLoad="clientSideInclude('includeone', 'includeone.html');">
*/

function clientSideInclude(url, id) {
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
    var element;
    if (id) {
        // add the loaded content to the specified id
        element = document.getElementById(id);
    }
    if (req) {
        // Synchronous request, wait till we have it all
        req.open('GET', url, false);
        req.send(null);
        if (element) {
            // set the contents to that we have retrieved
            element.innerHTML = req.responseText;
        }
        // and return the response text we have loaded
        return req.responseText;
    } else {
        element.innerHTML =
            "Sorry, your browser does not support " +
            "XMLHTTPRequest objects. This page requires " +
            "Internet Explorer 5 or better for Windows, " +
            "or Firefox for any system, or Safari. Other " +
            "compatible browsers may also exist.";
    }
}

function loadScript(url, callback) {
    // Adding the script tag to the head as suggested before
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;

    // Then bind the event to the callback function.
    // There are several events for cross browser compatibility.
    script.onreadystatechange = callback;
    script.onload = callback;

    // Fire the loading
    head.appendChild(script);
}

function fadeElement(element, isFadeIn, completedFunction) {
    // make sure the element has the required class to fade in / out
    element.classList.add('fadeable');
    element.classList.remove('visible');
    if (isFadeIn) {
        // fade the element in, make it visible
        setTimeout(function() { element.classList.add('visible'); }, 1);
    } else {
        // fade out the element, hide it
        setTimeout(function() { element.classList.remove('visible'); }, 1);
    }
    if (completedFunction) {
        // animation ends in a second, call the completion function then
        setTimeout(function() { completedFunction(); }, 500);
    }
}

function isValInList(value, listToTest) {
    // return if the passed value is in the passed list
    for (var i = 0; i < listToTest.length; i += 1) {
        if (listToTest[i] === value) {
            return true;
        }
    }
    return false;
}

function getAbsoluteOffsetFromBody(el) { // finds the offset of el from the body or html element
    var _x = 0;
    var _y = 0;
    while (el && !isNaN(el.offsetLeft) && !isNaN(el.offsetTop)) {
        _x += el.offsetLeft - el.scrollLeft + el.clientLeft;
        _y += el.offsetTop - el.scrollTop + el.clientTop;
        el = el.offsetParent;
    }
    return { top: _y, left: _x };
}

function getAbsoluteOffsetFromGivenElement(el, relativeEl) { // finds the offset of el from relativeEl
    var _x = 0;
    var _y = 0;
    while (el && el != relativeEl && !isNaN(el.offsetLeft) && !isNaN(el.offsetTop)) {
        _x += el.offsetLeft - el.scrollLeft + el.clientLeft;
        _y += el.offsetTop - el.scrollTop + el.clientTop;
        el = el.offsetParent;
    }
    return { top: _y, left: _x };
}

function getAbsoluteOffsetFromRelative(el) { // finds the offset of el from the first parent with position: relative
    var _x = 0;
    var _y = 0;
    while (el && !isNaN(el.offsetLeft) && !isNaN(el.offsetTop)) {
        _x += el.offsetLeft - el.scrollLeft + el.clientLeft;
        _y += el.offsetTop - el.scrollTop + el.clientTop;
        el = el.offsetParent;
        if (el != null) {
            if (getComputedStyle !== 'undefined')
                valString = getComputedStyle(el, null).getPropertyValue('position');
            else
                valString = el.currentStyle['position'];
            if (valString === "relative")
                el = null;
        }
    }
    return { top: _y, left: _x };
}

function scrollTo(element, to, duration) {
    //TODO Scrolling with animation doesn't seem to work - just jump there for now
    element.scrollIntoView(true);
    /*
    if (duration < 0) return;
    var difference = to - element.scrollTop;
    var perTick = difference / duration * 2;

    setTimeout(function() {
        element.scrollTop = element.scrollTop + perTick;
        scrollTo(element, to, duration - 2);
    }, 10);
}

function scrollToElement(element) {
    var yPos = element.getClientRects()[0].top;
    var yScroll = window.scrollY;
    var interval = setInterval(function() {
        yScroll -= 10;
        window.scroll(0, yPos);
        if (element.getClientRects()[0].top >= 0) {
            clearInterval(interval);
        }
    }, 5);*/
}

function getLocaleShortDateString(d) {
    var f = { "ar-SA": "dd/MM/yy", "bg-BG": "dd.M.yyyy", "ca-ES": "dd/MM/yyyy", "zh-TW": "yyyy/M/d", "cs-CZ": "d.M.yyyy", "da-DK": "dd-MM-yyyy", "de-DE": "dd.MM.yyyy", "el-GR": "d/M/yyyy", "en-US": "M/d/yyyy", "fi-FI": "d.M.yyyy", "fr-FR": "dd/MM/yyyy", "he-IL": "dd/MM/yyyy", "hu-HU": "yyyy. MM. dd.", "is-IS": "d.M.yyyy", "it-IT": "dd/MM/yyyy", "ja-JP": "yyyy/MM/dd", "ko-KR": "yyyy-MM-dd", "nl-NL": "d-M-yyyy", "nb-NO": "dd.MM.yyyy", "pl-PL": "yyyy-MM-dd", "pt-BR": "d/M/yyyy", "ro-RO": "dd.MM.yyyy", "ru-RU": "dd.MM.yyyy", "hr-HR": "d.M.yyyy", "sk-SK": "d. M. yyyy", "sq-AL": "yyyy-MM-dd", "sv-SE": "yyyy-MM-dd", "th-TH": "d/M/yyyy", "tr-TR": "dd.MM.yyyy", "ur-PK": "dd/MM/yyyy", "id-ID": "dd/MM/yyyy", "uk-UA": "dd.MM.yyyy", "be-BY": "dd.MM.yyyy", "sl-SI": "d.M.yyyy", "et-EE": "d.MM.yyyy", "lv-LV": "yyyy.MM.dd.", "lt-LT": "yyyy.MM.dd", "fa-IR": "MM/dd/yyyy", "vi-VN": "dd/MM/yyyy", "hy-AM": "dd.MM.yyyy", "az-Latn-AZ": "dd.MM.yyyy", "eu-ES": "yyyy/MM/dd", "mk-MK": "dd.MM.yyyy", "af-ZA": "yyyy/MM/dd", "ka-GE": "dd.MM.yyyy", "fo-FO": "dd-MM-yyyy", "hi-IN": "dd-MM-yyyy", "ms-MY": "dd/MM/yyyy", "kk-KZ": "dd.MM.yyyy", "ky-KG": "dd.MM.yy", "sw-KE": "M/d/yyyy", "uz-Latn-UZ": "dd/MM yyyy", "tt-RU": "dd.MM.yyyy", "pa-IN": "dd-MM-yy", "gu-IN": "dd-MM-yy", "ta-IN": "dd-MM-yyyy", "te-IN": "dd-MM-yy", "kn-IN": "dd-MM-yy", "mr-IN": "dd-MM-yyyy", "sa-IN": "dd-MM-yyyy", "mn-MN": "yy.MM.dd", "gl-ES": "dd/MM/yy", "kok-IN": "dd-MM-yyyy", "syr-SY": "dd/MM/yyyy", "dv-MV": "dd/MM/yy", "ar-IQ": "dd/MM/yyyy", "zh-CN": "yyyy/M/d", "de-CH": "dd.MM.yyyy", "en-GB": "dd/MM/yyyy", "es-MX": "dd/MM/yyyy", "fr-BE": "d/MM/yyyy", "it-CH": "dd.MM.yyyy", "nl-BE": "d/MM/yyyy", "nn-NO": "dd.MM.yyyy", "pt-PT": "dd-MM-yyyy", "sr-Latn-CS": "d.M.yyyy", "sv-FI": "d.M.yyyy", "az-Cyrl-AZ": "dd.MM.yyyy", "ms-BN": "dd/MM/yyyy", "uz-Cyrl-UZ": "dd.MM.yyyy", "ar-EG": "dd/MM/yyyy", "zh-HK": "d/M/yyyy", "de-AT": "dd.MM.yyyy", "en-AU": "d/MM/yyyy", "es-ES": "dd/MM/yyyy", "fr-CA": "yyyy-MM-dd", "sr-Cyrl-CS": "d.M.yyyy", "ar-LY": "dd/MM/yyyy", "zh-SG": "d/M/yyyy", "de-LU": "dd.MM.yyyy", "en-CA": "dd/MM/yyyy", "es-GT": "dd/MM/yyyy", "fr-CH": "dd.MM.yyyy", "ar-DZ": "dd-MM-yyyy", "zh-MO": "d/M/yyyy", "de-LI": "dd.MM.yyyy", "en-NZ": "d/MM/yyyy", "es-CR": "dd/MM/yyyy", "fr-LU": "dd/MM/yyyy", "ar-MA": "dd-MM-yyyy", "en-IE": "dd/MM/yyyy", "es-PA": "MM/dd/yyyy", "fr-MC": "dd/MM/yyyy", "ar-TN": "dd-MM-yyyy", "en-ZA": "yyyy/MM/dd", "es-DO": "dd/MM/yyyy", "ar-OM": "dd/MM/yyyy", "en-JM": "dd/MM/yyyy", "es-VE": "dd/MM/yyyy", "ar-YE": "dd/MM/yyyy", "en-029": "MM/dd/yyyy", "es-CO": "dd/MM/yyyy", "ar-SY": "dd/MM/yyyy", "en-BZ": "dd/MM/yyyy", "es-PE": "dd/MM/yyyy", "ar-JO": "dd/MM/yyyy", "en-TT": "dd/MM/yyyy", "es-AR": "dd/MM/yyyy", "ar-LB": "dd/MM/yyyy", "en-ZW": "M/d/yyyy", "es-EC": "dd/MM/yyyy", "ar-KW": "dd/MM/yyyy", "en-PH": "M/d/yyyy", "es-CL": "dd-MM-yyyy", "ar-AE": "dd/MM/yyyy", "es-UY": "dd/MM/yyyy", "ar-BH": "dd/MM/yyyy", "es-PY": "dd/MM/yyyy", "ar-QA": "dd/MM/yyyy", "es-BO": "dd/MM/yyyy", "es-SV": "dd/MM/yyyy", "es-HN": "dd/MM/yyyy", "es-NI": "dd/MM/yyyy", "es-PR": "dd/MM/yyyy", "am-ET": "d/M/yyyy", "tzm-Latn-DZ": "dd-MM-yyyy", "iu-Latn-CA": "d/MM/yyyy", "sma-NO": "dd.MM.yyyy", "mn-Mong-CN": "yyyy/M/d", "gd-GB": "dd/MM/yyyy", "en-MY": "d/M/yyyy", "prs-AF": "dd/MM/yy", "bn-BD": "dd-MM-yy", "wo-SN": "dd/MM/yyyy", "rw-RW": "M/d/yyyy", "qut-GT": "dd/MM/yyyy", "sah-RU": "MM.dd.yyyy", "gsw-FR": "dd/MM/yyyy", "co-FR": "dd/MM/yyyy", "oc-FR": "dd/MM/yyyy", "mi-NZ": "dd/MM/yyyy", "ga-IE": "dd/MM/yyyy", "se-SE": "yyyy-MM-dd", "br-FR": "dd/MM/yyyy", "smn-FI": "d.M.yyyy", "moh-CA": "M/d/yyyy", "arn-CL": "dd-MM-yyyy", "ii-CN": "yyyy/M/d", "dsb-DE": "d. M. yyyy", "ig-NG": "d/M/yyyy", "kl-GL": "dd-MM-yyyy", "lb-LU": "dd/MM/yyyy", "ba-RU": "dd.MM.yy", "nso-ZA": "yyyy/MM/dd", "quz-BO": "dd/MM/yyyy", "yo-NG": "d/M/yyyy", "ha-Latn-NG": "d/M/yyyy", "fil-PH": "M/d/yyyy", "ps-AF": "dd/MM/yy", "fy-NL": "d-M-yyyy", "ne-NP": "M/d/yyyy", "se-NO": "dd.MM.yyyy", "iu-Cans-CA": "d/M/yyyy", "sr-Latn-RS": "d.M.yyyy", "si-LK": "yyyy-MM-dd", "sr-Cyrl-RS": "d.M.yyyy", "lo-LA": "dd/MM/yyyy", "km-KH": "yyyy-MM-dd", "cy-GB": "dd/MM/yyyy", "bo-CN": "yyyy/M/d", "sms-FI": "d.M.yyyy", "as-IN": "dd-MM-yyyy", "ml-IN": "dd-MM-yy", "en-IN": "dd-MM-yyyy", "or-IN": "dd-MM-yy", "bn-IN": "dd-MM-yy", "tk-TM": "dd.MM.yy", "bs-Latn-BA": "d.M.yyyy", "mt-MT": "dd/MM/yyyy", "sr-Cyrl-ME": "d.M.yyyy", "se-FI": "d.M.yyyy", "zu-ZA": "yyyy/MM/dd", "xh-ZA": "yyyy/MM/dd", "tn-ZA": "yyyy/MM/dd", "hsb-DE": "d. M. yyyy", "bs-Cyrl-BA": "d.M.yyyy", "tg-Cyrl-TJ": "dd.MM.yy", "sr-Latn-BA": "d.M.yyyy", "smj-NO": "dd.MM.yyyy", "rm-CH": "dd/MM/yyyy", "smj-SE": "yyyy-MM-dd", "quz-EC": "dd/MM/yyyy", "quz-PE": "dd/MM/yyyy", "hr-BA": "d.M.yyyy.", "sr-Latn-ME": "d.M.yyyy", "sma-SE": "yyyy-MM-dd", "en-SG": "d/M/yyyy", "ug-CN": "yyyy-M-d", "sr-Cyrl-BA": "d.M.yyyy", "es-US": "M/d/yyyy" };

    var l = navigator.language ? navigator.language : navigator['userLanguage'],
        y = d.getFullYear(),
        m = d.getMonth() + 1,
        d = d.getDate();
    f = (l in f) ? f[l] : "MM/dd/yyyy";

    function z(s) { s = '' + s; return s.length > 1 ? s : '0' + s; }
    f = f.replace(/yyyy/, y);
    f = f.replace(/yy/, String(y).substr(2));
    f = f.replace(/MM/, z(m));
    f = f.replace(/M/, m);
    f = f.replace(/dd/, z(d));
    f = f.replace(/d/, d);
    return f;
}

function mdlHandleExpand(parentDiv, onExpandFunction) {
    var expandIcon = parentDiv.querySelector('.expand-icon-button');
    expandIcon.addEventListener(
        'click',
        function() {
            mdlToggleExpandIcon(expandIcon);
            onExpandFunction();
        },
        false
    );
}

function mdlToggleExpandIcon(expandIcon) {
    if (expandIcon.innerHTML.indexOf('more') > -1) {
        expandIcon.innerHTML = 'expand_less';
    } else {
        expandIcon.innerHTML = 'expand_more';
    }
}

function mdlChangeExpandIcon(parentDiv, isExpand) {
    var expandIcon = this.parentDiv.querySelector('.expand-icon-button');
    if (isExpand) {
        expandIcon.innerHTML = 'expand_more';
    } else {
        expandIcon.innerHTML = 'expand_less';
    }
}

//MDL Text Input Cleanup
function mdlCleanup(parent) {
    // upgrade any components
    componentHandler.upgradeElements(parent);
    // and search for all the text fileds to check they are dirty
    var mdlInputs = parent.querySelectorAll('.mdl-js-textfield');
    for (var i = 0, l = mdlInputs.length; i < l; i++) {
        if (mdlInputs[i].MaterialTextfield) {
            mdlInputs[i].MaterialTextfield.checkDirty();
        } else {
            console.error('Encountered a textfield that was not a MaterialTextField: ' + mdlInputs[i].innerHTML);
        }
    }
}

function mdlChange(element, newText) {
    // change the actual text input for the MDL code instead of setting content, so the MDL
    // label and such all change in accordance to the new input
    if (element.MaterialTextfield) {
        // the element is a material text field, change this
        dateElement.parentElement.MaterialTextfield.change(newText);
    } else if (element.parentElement && element.parentElement.MaterialTextfield) {
        // it's the parent that is the text field, change this
        element.parentElement.MaterialTextfield.change(newText);
    } else {
        // just set the content - bad really
        element.value = newText;
        console.error("Failed to 'change' the MDL text field contents: " + element.innerHTML + " to " + newText);
    }
}

function mdlShowConfirmDialog(title, contents, yesButtonText, noButtonText, yesCallback, noCallback) {
    var dialog = document.querySelector('dialog');
    if (!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }
    // change the title and contents
    if (title) {
        dialog.querySelector('.mdl-dialog__title').innerHTML = title;
    }
    if (contents) {
        dialog.querySelector('.mdl-dialog__content').innerHTML = contents;
    }
    // change the text on the buttons
    if (yesButtonText) {
        dialog.querySelector('.confirm').innerHTML = yesButtonText;
    }
    if (noButtonText) {
        dialog.querySelector('.close').innerHTML = noButtonText;
    }
    // listen to the buttons
    dialog.querySelector('.confirm').addEventListener('click',
        function() {
            // sometimes there is an error because the dialog doesn't have the attribute 'open'
            // so if we make sure it is there then it seems happier
            if (!dialog.getAttribute('open')) {
                dialog.setAttribute('open', 'true');
            }
            // now close the 'open' dialog
            dialog.close();
            // and inform the callback
            if (yesCallback) {
                yesCallback();
            }
        },
        false
    );
    dialog.querySelector('.close').addEventListener('click',
        function() {
            // sometimes there is an error because the dialog doesn't have the attribute 'open'
            // so if we make sure it is there then it seems happier
            if (!dialog.getAttribute('open')) {
                dialog.setAttribute('open', 'true');
            }
            // now close the 'open' dialog
            dialog.close();
            // and inform the callback
            if (noCallback) {
                noCallback();
            }
        },
        false
    );
    // if the dialog thinks it is open - correct it to prevent the error appearing
    dialog.removeAttribute('open');
    // show the dialog
    dialog.showModal();
}