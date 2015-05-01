/* This code is used to parse the whole json data from hs and filter the pictures and save them in redis.
   The pictures are saved in key 'pictures' using hash and each picture is saved with its id.The json
   data is saved in with a key name "json" in redis db.
*/
module.exports.fetchFromHs=function(){
	var http=require('http');
	var redis=require('redis');
		
	//saves images in redis db
	function saveImage(url,id,clnt){
		var body='';
		http.get(url,function(res){
			res.setEncoding('base64');
			res.on('data',function(chunk){
				body+=chunk;	
			});	
			res.on('end',function(){
				clnt.hset("pictures","/"+id,body);
			});
		}).on('error', function(e) {
	  		console.log("Error: " + e.message);
		});
	}
	
	/*saves json string in redis db, fetchs pictures from hs server and trasfer the fetched pictures to 		saveImage function to be saved in redis db*/
	function fetchAndSavePics(jsn){
	
		var articles=jsn.data.articles;
		var client=redis.createClient();
		var json_str=JSON.stringify(jsn);
		if(jsn.data && jsn.data.articles){
	
			client.on('error',function(err){
				console.log("error: ",err);
		});
	
	    client.on('connect',function(){
				//save json in db
				client.set("json",json_str);
	
				//save pictures in db
			for(var articlekey in articles){
					var article=articles[articlekey];
					if(article){
						if(article.pictures){
							for(var picturekey in article.pictures){
								var picture=article.pictures[picturekey];
								if(picture.id && picture.url){
									picture.url=picture.url.replace('{type}','nelio');
									picture.url=picture.url.replace('{width}',picture.width);
									saveImage(picture.url,picture.id,client);
									//TODO: add code for additional type and width when available
								}
							}
						}
						if(article.mainPicture && article.mainPicture.id && article.mainPicture.url){
							
							article.mainPicture.url=article.mainPicture.url.replace('{type}','nelio');
							article.mainPicture.url=article.mainPicture.url.replace	('{width}',article.mainPicture.width);
							saveImage(article.mainPicture.url,article.mainPicture.id,client);
							//TODO: add code for additional type and width when available
						}
					}
				}
			
			});
			
			//exit process after all commands are processed
			client.unref();
		}
	}
	
	//fetchs json data from hs server
	http.get('http://www.hs.fi/rest/k/editions/uusin/',function(res){
		var body='';
		res.on('data',function(chunk){
			body+=chunk;
		});
		res.on('end',function(){
			try{
				var hsjson=JSON.parse(body);
				
				fetchAndSavePics(hsjson);
			}
			catch(err){
				console.log("invalid json"+err);
			}
	});	
	}).on('error', function(e) {
	  console.log("Got error: " + e.message);
	});
};
