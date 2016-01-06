var RegistryConnector = function(registryURL) {

  if(engine.factory.engineName.contains("Nashorn")) {
    var RequestWrapper = require(__dirname + '/java-request');
  }else {
    var RequestWrapper = require(__dirname + '/js-request');
  }

  this._request = new RequestWrapper();
  this._registryURL = registryURL;
};

RegistryConnector.prototype.getUser = function(userid, callback) {
  this._request.get(this._registryURL + '/hyperty/user/' + encodeURIComponent(userid), function(err, response) {
    print("Get user: " + JSON.stringify(response));
    callback(response);
  });
};

RegistryConnector.prototype.createUser = function(userid, callback) {
  this._request.put(this._registryURL + '/hyperty/user/' + userid, "", function(err, response) {
    print("Create user: " + response);
    callback(response);
  });
};

RegistryConnector.prototype.getHyperty = function(userid, hypertyid, callback) {
  var endpoint = '/hyperty/user/' + encodeURIComponent(userid) + '/' + encodeURIComponent(hypertyid);

  this._request.get(this._registryURL + endpoint, function(err, response) {
    print("Get hyperty: " + JSON.stringify(response));
    callback(response);
  });
};

RegistryConnector.prototype.addHyperty = function(userid, hypertyid, hypertyDescriptor, callback) {
  var endpoint = '/hyperty/user/' + encodeURIComponent(userid) + '/' + encodeURIComponent(hypertyid);
  var data = { 'descriptor': hypertyDescriptor };

  this._request.put(this._registryURL + endpoint, JSON.stringify(data), function(err, response) {
    print("Add hyperty: " + response);
    callback(response);
  });
};

module.exports = RegistryConnector;
