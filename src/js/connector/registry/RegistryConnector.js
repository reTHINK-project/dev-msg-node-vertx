/**
* Copyright 2016 PT Inovação e Sistemas SA
* Copyright 2016 INESC-ID
* Copyright 2016 QUOBIS NETWORKS SL
* Copyright 2016 FRAUNHOFER-GESELLSCHAFT ZUR FOERDERUNG DER ANGEWANDTEN FORSCHUNG E.V
* Copyright 2016 ORANGE SA
* Copyright 2016 Deutsche Telekom AG
* Copyright 2016 Apizee
* Copyright 2016 TECHNISCHE UNIVERSITAT BERLIN
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
**/

var RegistryConnector = function(registryURL) {

  if(engine.factory.engineName.contains("Nashorn")) {
    var RequestWrapper = require(__dirname + '/java-request');
  }else {
    var RequestWrapper = require(__dirname + '/js-request');
  }

  this._request = new RequestWrapper();
  this._registryURL = registryURL;
};

RegistryConnector.prototype.processMessage = function(msg, callback) {
  switch(msg.type.toLowerCase()) {
      case "read":
        if('hypertyURL' in msg.body.value) {
          this.getUser(msg.body.resource, callback);
        }else {
          this.getDataObject(msg.body.resource, callback);
        }
      break;

      case "create":
        if('hypertyURL' in msg.body.value) {
          this.addHyperty(msg.body.value.user, msg.body.value.hypertyURL, msg.body.value.hypertyDescriptorURL, msg.body.value.expires, callback);
        }else {
          this.addDataObject(msg.body.value.name, msg.body.value.schema, msg.body.value.expires, msg.body.value.url, msg.body.value.reporter, callback);
        }
      break;

      case "update":
        if('hypertyURL' in msg.body.value) {
          this.addHyperty(msg.body.value.user, msg.body.value.hypertyURL, msg.body.value.hypertyDescriptorURL, msg.body.value.expires, callback);
        }else {
          this.addDataObject(msg.body.value.name, msg.body.value.schema, msg.body.value.expires, msg.body.value.url, msg.body.value.reporter, callback);
        }
      break;

      case "delete":
        if('hypertyURL' in msg.body.value) {
          this.deleteHyperty(msg.body.value.user, msg.body.value.hypertyURL, callback);
        }else {
          this.deleteDataObject(msg.body.value.name, callback);
        }
      break;
  }
};

RegistryConnector.prototype.getUser = function(userid, callback) {
  this._request.get(this._registryURL + '/hyperty/user/' + encodeURIComponent(userid), function(err, response, statusCode) {
    print("Get user: " + response);

    var body = {
      'code': statusCode,
      'value': JSON.parse(response)
    };

    callback(body);
  });
};

RegistryConnector.prototype.addHyperty = function(userid, hypertyid, hypertyDescriptor, expires, callback) {
  var endpoint = '/hyperty/user/' + encodeURIComponent(userid) + '/' + encodeURIComponent(hypertyid);
  var data = {
    'descriptor': hypertyDescriptor,
    'expires': expires
  };

  this._request.put(this._registryURL + endpoint, JSON.stringify(data), function(err, response, statusCode) {
    print("Add hyperty: " + response);

    var body = {
      'code': statusCode
    };

    callback(body);
  });
};

RegistryConnector.prototype.deleteHyperty = function(userid, hypertyid, callback) {
  var endpoint = '/hyperty/user/' + encodeURIComponent(userid) + '/' + encodeURIComponent(hypertyid);

  this._request.del(this._registryURL + endpoint, function(err, response, statusCode) {
    print("Delete hyperty: " + response);

    var body = {
      'code': statusCode
    };

    callback(body);
  });
};

RegistryConnector.prototype.getDataObject = function(resource, callback) {
  var dataobj = resource.split("://")[1];

  this._request.get(this._registryURL + '/hyperty/dataobject/' + encodeURIComponent(dataobj), function(err, response, statusCode) {
    print("Get Data Object: " + response);

    var body = {
      'code': statusCode,
      'value': JSON.parse(response)
    };

    callback(body);
  });
};

RegistryConnector.prototype.addDataObject = function(dataobjName, schema, expires, url, reporter, callback) {
  var endpoint = '/hyperty/dataobject/' + encodeURIComponent(dataobjName);
  var data = {
    'name': dataobjName,
    'schema': schema,
    'url': url,
    'reporter': reporter,
    'expires': expires
  };

  this._request.put(this._registryURL + endpoint, JSON.stringify(data), function(err, response, statusCode) {
    print("Add data object: " + response);

    var body = {
      'code': statusCode
    };

    callback(body);
  });
};

RegistryConnector.prototype.deleteDataObject = function(dataObjectName, callback) {
  var endpoint = '/hyperty/dataobject/' + encodeURIComponent(dataObjectName);

  this._request.del(this._registryURL + endpoint, function(err, response, statusCode) {
    print("Delete Data Object: " + response);

    var body = {
      'code': statusCode
    };

    callback(body);
  });
};

module.exports = RegistryConnector;
