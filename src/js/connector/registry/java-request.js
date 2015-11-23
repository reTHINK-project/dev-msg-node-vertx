var JavaRequest = function() {
  this._client = vertx.createHttpClient({});
};

JavaRequest.prototype.get = function(url, callback) {
  this._client.getAbs(url, function (response) {
    response.bodyHandler(function(totalBuffer) {
      var body = totalBuffer.toString("UTF-8");
      callback(null, body);
    });
  }).end();
};

JavaRequest.prototype.put = function(url, data, callback) {
  this._client.putAbs(url)
  .putHeader("content-type", "application/json")
  .putHeader("content-length", "" + data.length())
  .handler(function(response) {
    callback(null, response.statusCode());
  })
  .write(data)
  .end();
};

module.exports = JavaRequest;
