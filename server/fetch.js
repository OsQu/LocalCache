/* This code is used to parse the whole json data from hs and filter the pictures and save them in redis.
   The pictures are saved in key 'pictures' using hash and each picture is saved with its id.The json
   data is saved in with a key name "json" in redis db.
*/
module.exports.fetchFromHs=function(){
	var http=require('http');
	var redis=require('redis');
  var worker=require('./fetch_image_worker')
	/*saves images in redis db*/
	function saveImage(url,id,clnt){
    worker.queueImage(url, id, clnt);
	}

	/*saves json string in redis db, fetchs pictures from hs server and trasfer the fetched pictures to 		saveImage function to be saved in redis db*/
	function fetchAndSavePics(jsn, client){
    console.log("Starting to save pictures");
		var articles=jsn.data.articles;
		var json_str=JSON.stringify(jsn);
		if(jsn.data && jsn.data.articles){
			client.on('error',function(err){
				console.log("error: ",err);
      });

      /*save json in db*/
      client.set("json",json_str);
      /*save pictures in db, firts try to delet any key with name "pictures"*/
      client.del("pictures",function (err, numRemoved) {
        if(numRemoved==0 || numRemoved==1){
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
                  }
                }
              }
              if(article.mainPicture && article.mainPicture.id && article.mainPicture.url){
                article.mainPicture.url=article.mainPicture.url.replace('{type}','nelio');
                article.mainPicture.url=article.mainPicture.url.replace	('{width}',article.mainPicture.width);
                saveImage(article.mainPicture.url,article.mainPicture.id,client);
              }
            }
          }
        }
      });
    }
	}
  var client = redis.createClient();

	/*fetchs json data from hs server*/
	http.get('http://www.hs.fi/rest/k/editions/uusin/',function(res){
		var body='';
		res.on('data',function(chunk){
			body+=chunk;
		});
		res.on('end',function(){
			try{
				var hsjson=JSON.parse(body);
				fetchAndSavePics(hsjson, client);
			}
			catch(err){
				console.log("invalid json"+err);
			}
    });
	}).on('error', function(e) {
	  console.log("Got error: " + e.message);
	});

  worker.process().then(function() { client.unref(); });
};
