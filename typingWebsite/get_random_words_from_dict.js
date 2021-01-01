window.onload = function () {

    var http = new XMLHttpRequest();

    http.onreadystatechange = function() {

        if(http.readyState == 4 && http.status == 200) {
            
            //everything is ready
            var allText = http.responseText;
            var split = allText.split('\n');

            //get 600 random words
            for(var i =0; i < 600; i++) {
                
                 var randomNum = Math.floor(Math.random() * split.length);
                 var randomLine = split[randomNum];
     
                console.log(randomNum);
                console.log(randomLine);
            }
          
        }

        //console.log(http);
    };

    //open method sets up request
    http.open("GET", "testJson/words.txt", true);  // true means async

    //actually send request
    http.send();

    //console.log(http);

};