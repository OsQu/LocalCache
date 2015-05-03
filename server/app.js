var http = require("http");
var redis=require('redis');
var fetch=require("./fetch");
var client=redis.createClient();
var PORT = process.env.PORT || 8080;
var json;

/*parse fetching time from command line*/
if(process.argv.length >= 3){
	var fetchTime=parseInt(process.argv[2],10);
	if(isNaN(fetchTime) || fetchTime>23 || fetchTime<0){
		console.log("Invalid fetchtime, fetchtime should be a number less than 24 and greater or equal to zero");
		process.exit();
	}
}
else{
 console.log("Invalid command line argument.\nValide command: node app.js <fetchtime> [disable-prefetch]  ,where fetchtime should be a number less than 24 and greater or equal to zero");
 process.exit();
}

/*fetch json fromatted data and pictures from hs and save it to redis db*/
if(process.argv[3] != "disable-prefetch") {
  fetch.fetchFromHs();
}

/*Starts a connection with redis db*/
client.on('error',function(err){
	console.log("error: ",err);
});

/*checks if it is time to download new data from HS server and download it if it is time*/
function fetchNew(){
	var date=new Date();
	var hour=date.getHours();
	hour=(hour<10 ? "0":"")+hour;
	console.log(hour);
	if(hour==fetchTime){
		console.log("fetching new");
		fetch.fetchFromHs();
		console.log("fetched new");
	}
}

var server = http.createServer(function(request, response) {
		/*if home url("/") is requested the response will be the whole json string*/
		if(request.url=="/"){
			client.get('json',function(err,data){
				if(data){
					response.writeHead(200, { "Content-Type": "application/json;charset=UTF-8", "Content-Length": Buffer.byteLength(data) });
  				response.end(data);
				}
				else{
					response.writeHead(404);
  				response.end();
				}
			});
		}
		else{
			client.hget('pictures',request.url,function(err,data){
				if(data){
					response.writeHead(200, { "Content-Type": "image/jpeg", "Content-Length": Buffer.byteLength(data, "base64") });
  				response.end(data,"base64");
				}
				else{
					response.writeHead(404);
  				response.end();
				}
			});
		}
});

/*Checks at least every 30 minutes and calls fetchNew() function which inturn checks if it is time to download new data from HS server and download it if it is time*/
setInterval(fetchNew,1800000);

server.listen(PORT, function(){
    console.log("Server listening on: http://localhost:%s",PORT);
});
