var config = require(__dirname + '/../NodeConfig');

var RegistryConnector = require(__dirname + '/registry/RegistryConnector');
var registry = new RegistryConnector(config.registry.url);
print("[Connectors] Registry Connector Loaded");

vertx.eventBus().consumer("mn:/registry-connector", function (message) {
  print("[Registry-Connector][Received]: " + message.body());

  var msg = JSON.parse(message.body());

  var callback = function(msg) {
      return message.reply(msg);
  };

  registry.processMessage(msg, callback);

});
