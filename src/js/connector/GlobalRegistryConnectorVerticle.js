var config = require(__dirname + '/../NodeConfig');

var GlobalRegistryConnector = require(__dirname + '/registry/GlobalRegistryConnector');
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
