// ==UserScript==
// @name        Teams Inspector
// @namespace   Violentmonkey Scripts
// @match       https://teams.microsoft.com.mcas.ms/v2/*
// @match       https://www.odwebp.svc.ms.mcas.ms/*
// @grant       none
// @version     1.0
// @author      -
// @description 04/09/2024, 12:24:07
// ==/UserScript==

document.addEventListener('keydown', function(event) {
    if (event.altKey && event.shiftKey && event.key === 'C') {
        let selectedText = window.getSelection().toString();
        // Create a temporary element to decode HTML entities
        let tempElement = document.createElement('div');
        tempElement.innerHTML = selectedText;
        selectedText = tempElement.textContent || tempElement.innerText || "";

        // Replace &nbsp; with a space
        selectedText = selectedText.replace(/\u00A0/g, ' ');
        if (selectedText.length > 1000) {
            alert("String too long, check console");
            console.log(selectedText);
        } else {
            prompt("", selectedText);
        }
    }
});

