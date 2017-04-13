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

var config = require('../NodeConfig');

var GlobalRegistryConnector = require('registry/GlobalRegistryConnector');
var globalRegistry = new GlobalRegistryConnector(config.globalregistry.url);
print("[Connectors] Global Registry Connector Loaded");

vertx.eventBus().consumer("mn:/global-registry-connector", function (message) {
  print("[Global-Registry-Connector][Received]: " + message.body());

  var msg = JSON.parse(message.body());

  var callback = function(msg) {
      return message.reply(msg);
  };

 switch(msg.type) {
      case "READ":
      print("[Global-Registry-Connector] Get user " + msg.body.guid);
      globalRegistry.getUser(msg.body.guid, callback);
      break;

      case "CREATE":
      print("[Global-Registry-Connector] Update Global Registry Record for user " + msg.body.guid);
      globalRegistry.updateRecord(msg.body.guid, msg.body.jwt, callback);
      break;
  }

});
