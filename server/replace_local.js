module.exports = function(jsonStr) {
  host = process.env.HOST || "localhost"
  return jsonStr.replace(/\w+\.snstatic.fi/g, host);
}
