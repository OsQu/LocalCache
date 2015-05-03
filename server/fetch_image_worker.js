/* Class to queue images in a slower pace. Caller queues images and then they are processed after calling process.
   If the queue is empty, FetchImageWorker will gradually raise the timeout until it is higher than 10s and after that it stops the processing
*/
var http = require('http');
var FetchImageWorker = {
  queue: [],
  timeout: 1000,

  queueImage: function(url, id, clnt) {
    this.queue.push({
      url: url,
      id: id,
      clnt: clnt
    });
  },

  process: function() {
    _this = this;
    setTimeout(function() {
      image = _this.queue.shift()
      if(image != undefined) {
        _this.timeout = 1000;
        console.log("Processing image: " + image.url);
        _this._processImage(image.url, image.id, image.clnt);
        _this.process();
      } else if(_this.timeout < 10000) {
        _this.timeout = _this.timeout + 2000;
        console.log("No image, but increasing timeout to: " + _this.timeout);
        _this.process();
      } else {
        console.log("Empty queue for too long. Stopping processing");
        _this.timeout = 1000;
      }
    }, this.timeout);
  },

  _processImage: function(url, id, clnt) {
		var body='';
		http.get(url,function(res){
			res.setEncoding('base64');
			res.on('data',function(chunk){
				body+=chunk;
			});
			res.on('end',function(){
        console.log("Saving picture: " + id);
				clnt.hset("pictures","/"+id,body);
			});
		}).on('error', function(e) {
	  		console.log("Error: " + url + " " + e.message);
		});
  }
}

module.exports = FetchImageWorker;
