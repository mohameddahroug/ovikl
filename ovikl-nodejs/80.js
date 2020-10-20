
var express   =    require("express");
var app       =    express();

app.use(express.static('../ovikl-html'));


//require("./files")(router,pool);
//app.use('/',router);
app.listen(80);
/*var httpsServer = https.createServer(credentials, app);
httpsServer.listen(8080);*/
console.log("Listening to PORT 80");
