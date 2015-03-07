/*This code is used to check if pictures could be regenerated from the data saved 
 *in redis database there by checking if the picture binary is saved in redis database in
 *the right format.
 *In method client.hget, 1425624449175 should be replaced by the id the picture we want to save in file
 */

var fs = require('fs');
var redis=require('redis');
var client=redis.createClient();

client.on('error',function(err){
	console.log("error: ",err);
});

client.on('connect',function(){
	client.hget('pictures','1425624449175',function(err,basestr){
		var bitmap = new Buffer(basestr, 'base64');
		fs.writeFileSync('test2', bitmap);
	
	});
	
});


