//wiki text info and pointers
var wikiTextArrayGlobal = [];
var wikiTextStringGlobal = "";
var wikiTextArrayPointer = 0;

//accuracy measures
var correctCount = 0;
var incorrectCount = 0;
var startTime = 0;
var isTimerStarted = false;
var charsCurrentlyCorrect = 0;



$(document).ready(function () {

    wikiTextArray = getWikiContent();
    console.log("returned text array: " + wikiTextArray);


});

async function getWikiContent() {

    console.log("Calling...");
    const pageid = await getRandomWiki();
    console.log("PageID: " + pageid);
    const parsedJSON = await getCharactersFromWiki(pageid);
    //console.log(parsedJSON);

    var wikiPageTitle = parsedJSON.query.pages[0].title;
    var paragraphExtract = parsedJSON.query.pages[0].extract;



    if (paragraphExtract.length < 1200) {
        console.log("Bad Wiki, retyring...")
        getWikiContent();
    } else {
        //console.log("Wiki Title: " + wikiPageTitle);
        console.log("Wiki Extract: " + paragraphExtract);
        //console.log("Extract length: " + paragraphExtract.length);

        var sanitisedText = sanitiseText(paragraphExtract);
        wikiTextStringGlobal = sanitisedText;
        console.log("sanitiseText: " + sanitisedText);


        document.getElementById("wikiTitle").innerHTML = 'Wikipedia Page Title: ' + wikiPageTitle;

        //split sanitised text into an array
        var wikiTextArray = sanitisedText.split(" ");
        wikiTextArrayGlobal = wikiTextArray;
        //console.log(wikiTextArray);


        //update the html to display the text
        var htmlUpdateAsString = "";
        for (i = 0; i < wikiTextArray.length; i++) {

            htmlUpdateAsString += '<span wordnum="' + i + '" class="word">' + wikiTextArray[i] + "</span> "; //classes = NONE, green, red, highlight

            $("#generatedWikiText").html(htmlUpdateAsString);

            //highlight the first word ready to start
            $("#generatedWikiText span:first").addClass("highlight");
        }

    }

}

function sanitiseText(paragraphExtract) {

    //remove all newlines
    var someText = paragraphExtract.replace(/\r?\n|\r/g, " ");
    //remove all ==
    someText = someText.replace(/[=]+/g, "");
    //remove all non english characters and replace with english equivalents
    someText = someText.replace(/[ÀàÁáÂâÄäÅåÃã]+/g, "a");
    someText = someText.replace(/[ÈèÉéÊêËëɛ]+/g, "e");
    someText = someText.replace(/[ÌìÍíÎîÏï]+/g, "i");
    someText = someText.replace(/[ÒòÓóÔôÕõÖö]+/g, "o");
    someText = someText.replace(/[ÙùÚúÛûÜü]+/g, "u");
    someText = someText.replace(/[ÝýŸ]+/g, "y");
    someText = someText.replace(/[ß]+/g, "b");
    someText = someText.replace(/[Ññ]+/g, "n");
    someText = someText.replace(/[ł]+/g, "l");



    //remove any other non english characters that werent covered above.
    someText = someText.replace(/[^a-zA-Z0-9\s.,:;()[{}'"\]]+/g, "");

    //remove extra spaces so there is only 1 between each word.
    someText = someText.replace(/[\s]+/g, " ");

    //trim leading and trailing whitespace
    sometext = someText.trim();

    //fix the .letter problem
    //console.log(sometext);
    var charNo = 0;
    while (charNo != someText.length - 1) {

        var currentChar = someText[charNo];
        var nextChar = someText[charNo + 1];

        //null if we have a punctuation or numbers
        var detectLetter = nextChar.match(/[a-zA-Z]/g);

        if (currentChar == "." && detectLetter != null) {
            //add a space
            someText = someText.slice(0, charNo + 1) + " " + someText.slice(charNo + 1);
        }

        charNo++;
    }

    return someText;

}


function getRandomWiki() {

    return new Promise(resolve => {

        var http = new XMLHttpRequest();


        //get a random wiki page and return info about it as a JSON
        http.open("GET", "https://en.wikipedia.org/w/api.php?format=json&origin=*&action=query&generator=random&grnnamespace=0&grnlimit=1&prop=info", true);
        http.send();

        http.onreadystatechange = function () {

            if (http.readyState == 4 && http.status == 200) {
                //get the random wiki page's ID
                var allText = http.responseText;
                var parsedJSON = JSON.parse(allText);
                //console.log(parsedJSON);

                var pageidJSONArray = parsedJSON.query.pages;
                var pageid = Object.keys(pageidJSONArray)[0];

                //console.log("Wiki Page ID: " + pageid);

                //return pageid;
                resolve(pageid);
            } else {
                //throw exception!!!???
            }
        }
    });


}

function getCharactersFromWiki(pageid) {

    return new Promise(resolve => {


        var http = new XMLHttpRequest();

        //send request to get 1200 characters (the limit) from the random wiki page
        http.open("GET", "https://en.wikipedia.org/w/api.php?action=query&format=json&origin=*&prop=extracts&pageids=" + pageid + "&formatversion=2&exchars=1200&explaintext=1", true);
        http.send();

        http.onreadystatechange = function () {

            if (http.readyState == 4 && http.status == 200) {

                //get the 1200 characters.
                var parsedJSON2 = JSON.parse(http.responseText);

                resolve(parsedJSON2);
            } else {
                //throw exception!!!???
            }
        }

    });

}



$("#inputBox").keyup(function (event) {

    //console.log("Keyup");

    var currentTextVal = $("#inputBox").val();
    var currentTextValLength = currentTextVal.length;
    var wordToMatch = wikiTextArrayGlobal[wikiTextArrayPointer];
    var sectionOfWordToMatch = wordToMatch.substring(0, currentTextValLength);
    var currentWordHtmlDiv = $('#generatedWikiText span[wordnum="' + wikiTextArrayPointer + '"]');

    //check if the space bar was pressed
    if (event.code === 'Space') {

        //if there is not text other than the spacebar, dont submit, but instead just erase the inputted space.
        if (currentTextVal.length == 1) {
            $("#inputBox").val("");
            currentTextVal = $("#inputBox").val();
            currentTextValLength = currentTextVal.length;
            sectionOfWordToMatch = wordToMatch.substring(0, currentTextValLength);
        } else {
            //evaluate the word and submit
            var submittedWord = currentTextVal.substring(0, currentTextValLength - 1); //remove the space at the end

            if (submittedWord == wordToMatch) {
                //correct submission
                correctCount++; //inc correct count
                charsCurrentlyCorrect += currentTextValLength + 1 ; //+1 to include the space we just deleted
                currentWordHtmlDiv.addClass("highlightCorrect");

                wikiTextArrayPointer++;
                currentWordHtmlDiv = $('#generatedWikiText span[wordnum="' + wikiTextArrayPointer + '"]'); //highlight next word
                currentWordHtmlDiv.addClass("highlight");

                $("#inputBox").val(""); //empty the text box ready for next word.
            } else {
                //incorrect submission
                incorrectCount++;
                currentWordHtmlDiv.addClass("highlightIncorrect");

                wikiTextArrayPointer++;
                currentWordHtmlDiv = $('#generatedWikiText span[wordnum="' + wikiTextArrayPointer + '"]'); //highlight next word
                currentWordHtmlDiv.addClass("highlight");

                $("#inputBox").val(""); //empty the text box ready for next word.
            }

            //update stats
            var wordsPerMinute = calcWordsPerMinute();
            $("#stats").html("Correct Words: " + correctCount + ", Incorrect Words: " + incorrectCount + ", Current Words Per Minute: " + wordsPerMinute); 

        }


    } else {

        //start the timer if it's not already started
        if (!isTimerStarted) {
            startTimer();
        }


        //check what is currently in the input box matches the substring of the word it needs to match
        if (currentTextVal == sectionOfWordToMatch) {
            //word matches so far
            currentWordHtmlDiv.removeClass("highlightIncorrect");
        } else {

            currentWordHtmlDiv.addClass("highlightIncorrect");
        }
    }


});


$("#restartButton").click(function () {
    restart();
});



function restart() {
    //reset global variables

    //wiki text info and pointers
    wikiTextArrayGlobal = [];
    wikiTextStringGlobal = "";
    wikiTextArrayPointer = 0;

    //accuracy measures
    correctCount = 0;
    incorrectCount = 0;
    startTime = 0;
    isTimerStarted = false;
    charsCurrentlyCorrect = 0;
    $("#stats").html("Correct Words: 0, Incorrect Words: 0, Current Words Per Minute: 0"); 

    //remove current wiki text and title and empty the input box
    var htmlUpdateAsString = "";
    $("#generatedWikiText").html(htmlUpdateAsString);
    $("#inputBox").val("");



    getWikiContent();


}


function startTimer() {
    isTimerStarted = true;

    startTime = getCurrentTime();

}

function getCurrentTime() {
    var d = new Date();
    return d.getTime();
}

function calcWordsPerMinute() {
    var timePassedSinceStartMillis = getCurrentTime() - startTime;
    var charsPerMilli = charsCurrentlyCorrect / timePassedSinceStartMillis;
    var wordsPerMilli = charsPerMilli / 5; //5 is the standard characters in 1 'word'
    var wordsPerMin = Math.floor(wordsPerMilli * 1000 * 60);

    return wordsPerMin;
}