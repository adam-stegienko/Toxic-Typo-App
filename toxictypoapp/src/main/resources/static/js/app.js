var timer = null;
var lastClickTime=null;

$(document).ready(function(){
    $("#word").keyup(onClick);
    hideSuggestion();
});

function onClick() {
    lastClickTime = new Date();
    if (timer==null) timer = window.setTimeout(checkIfShouldSuggest,80);
}

function checkIfShouldSuggest() {
    timer = null;
    now = new Date();
    var sinceClick = now.getTime()-lastClickTime.getTime();
    if (sinceClick>70) {
        var word = getUserInput();
        if (word.length > 2) callSuggest(word); else hideSuggestion();
    } else {
        timer = window.setTimeout(checkIfShouldSuggest,80-sinceClick);
    }
}

function callSuggest(word) {
    $.get({
        url:"/api/suggest/"+word,
        success: function(data) { onSuggestion(word,data);}
    })
}

function getUserInput() {
    return $("#word").val();
}

function hideSuggestion() {
    $("#toxic").hide();
}

function onSuggestion(originalWord,server) {
    var currentWord = getUserInput();
    if (currentWord==originalWord) {
        if(server.suggestion) {
            $("#suggestion").text(server.suggestion);
            $("#toxic").show();
        } else hideSuggestion();
    }

}