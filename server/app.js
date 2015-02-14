var express = require("express");
var logger = require("winston");
var app = express();

var PORT = process.env.PORT || 8080

app.get("/", function(req, res) {
  res.send("It's alive!");
});

var server = app.listen(PORT, function() {
  var address = server.address();
  logger.info("Started server: %s:%s", address.address, address.port)
});
