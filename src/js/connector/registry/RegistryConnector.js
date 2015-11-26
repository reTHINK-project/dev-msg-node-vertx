var RegistryConnector = function(registryURL) {

  if(engine.factory.engineName.contains("Nashorn")) {
    var RequestWrapper = require('./java-request');
  }else {
    var RequestWrapper = require('./js-request');
  }

  this._request = new RequestWrapper();
  this._registryURL = registryURL;
};

RegistryConnector.prototype.getUser = function(userid, callback) {
  this._request.get(this._registryURL + '/user_id/' + userid, function(err, response) {
    print("Get user: " + JSON.stringify(response));
    callback(response);
  });
};

RegistryConnector.prototype.createUser = function(userid, callback) {
  this._request.put(this._registryURL + '/user_id/' + userid, "", function(err, response) {
    print("Create user: " + response);
    callback(response);
  });
};

module.exports = RegistryConnector;
