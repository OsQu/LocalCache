var http = require("http");
var redis=require('redis');
var fetch=require("./fetch");
var client=redis.createClient();
const PORT=8080;
var json;

//fetch json and pictures from hs and save it to redis db
fetch.fetchFromHs();

client.on('error',function(err){
	console.log("error: ",err);
});

var server = http.createServer(function(request, response) {
	
		//if home url("/") is requested the response will be the whole json string
		if(request.url=="/"){
			client.get('json',function(err,data){
				if(data){
					response.writeHead(200, {"Content-Type": "application/json;charset=UTF-8"});  
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
					response.writeHead(200, {"Content-Type": "image/jpeg"});  
  				response.end(data,"base64");
				}
				else{
					response.writeHead(404);  
  				response.end();
				}
			});
			
		}
	
});
 
server.listen(PORT, function(){
    console.log("Server listening on: http://localhost:%s",PORT);
});
