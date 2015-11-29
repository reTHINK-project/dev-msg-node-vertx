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

RegistryConnector.prototype.getHyperty = function(userid, hypertyid, callback) {
  var endpoint = '/user_id/' + userid + '/' + hypertyid;

  this._request.get(this._registryURL + endpoint, function(err, response) {
    print("Get hyperty: " + JSON.stringify(response));
    callback(response);
  });
};

RegistryConnector.prototype.addHyperty = function(userid, hypertyid, data, callback) {
  var endpoint = '/user_id/' + userid + '/' + hypertyid;

  this._request.put(this._registryURL + endpoint, JSON.stringify(data), function(err, response) {
    print("Add hyperty: " + response);
    callback(response);
  });
};

module.exports = RegistryConnector;
