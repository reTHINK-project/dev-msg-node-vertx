var GlobalRegistryConnector = function(globalregistryURL) {

  if(engine.factory.engineName.contains("Nashorn")) {
    var RequestWrapper = require(__dirname + '/java-request');
  }else {
    var RequestWrapper = require(__dirname + '/js-request');
  }

  this._request = new RequestWrapper();
  this._globalregistryURL = globalregistryURL;
};

GlobalRegistryConnector.prototype.getUser = function(guid, callback) {
  this._request.get(this._globalregistryURL + '/guid/' + guid, function(err, response) {
    print("Get user from Global Registry: " + JSON.stringify(response));
    callback(response);
  });
};

GlobalRegistryConnector.prototype.updateRecord = function(guid, jwt, callback) {
  this._request.put(this._globalregistryURL + '/guid/' + guid, jwt, function(err, response) {
    print("Update record in Global Registry: " + response);
    callback(response);
  });
};

module.exports = GlobalRegistryConnector;
