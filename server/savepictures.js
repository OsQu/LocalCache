/* This code is used to parse the whole json data from hs and filter the pictures and save them in redis.
 * The pictures are saved in key 'pictures' using hash so that each picture is saved with its id.
 */

var http=require('http');
var redis=require('redis');

function saveImage(url,id,clnt){
	var body='';
	http.get(url,function(res){
		res.setEncoding('base64');
		res.on('data',function(chunk){
			body+=chunk;	
		});	
		res.on('end',function(){
			clnt.hset("pictures",id,body);
		});
	}).on('error', function(e) {
  		console.log("Error: " + e.message);
	});
}

function findAndSavePics(jsn){

	var articles=jsn.data.articles;
	var client=redis.createClient();

	if(jsn.data && jsn.data.articles){

		client.on('error',function(err){
			console.log("error: ",err);
		});

        client.on('connect',function(){
			for(var articlekey in articles){
				var article=articles[articlekey];
				if(article){
					if(article.pictures){
						for(var picturekey in article.pictures){
							var picture=article.pictures[picturekey];
							if(picture.id && picture.url){
								picture.url=picture.url.replace('{type}','pieni');
								picture.url=picture.url.replace('{width}','1');
								saveImage(picture.url,picture.id,client);
								//TODO: add code for additional type and width whena available
							}
						}
					}
					if(article.mainPicture && article.mainPicture.id && article.mainPicture.url){
						
						article.mainPicture.url=article.mainPicture.url.replace('{type}','pieni');
						article.mainPicture.url=article.mainPicture.url.replace('{width}','1');
						saveImage(article.mainPicture.url,article.mainPicture.id,client);
						//TODO: add code for additional type and width whena available
					}
				}
			}
		
		});
		
		//exit process after all commands are processed
		client.unref();
	}
}

http.get('http://www.hs.fi/rest/k/editions/uusin/',function(res){
	var body='';
	res.on('data',function(chunk){
		body+=chunk;
	});
	res.on('end',function(){
		try{
			var hsjson=JSON.parse(body);
			findAndSavePics(hsjson);
		}
		catch(err){
			console.log("invalid json"+err);
		}
	});	
}).on('error', function(e) {
  console.log("Got error: " + e.message);
});

