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

  switch(msg.type) {
      case "READ":
      print("[Registry-Connector] Get user with " + msg.body.resource);
      registry.getUser(msg.body.resource, callback);
      break;

      case "CREATE":
      print("[Registry-Connector] Add Hyperty with " + msg.body.value.hypertyURL);
      registry.addHyperty(msg.body.value.user, msg.body.value.hypertyURL, msg.body.value.hypertyDescriptorURL, callback);
      break;

      case "DELETE":
      print("[Registry-Connector] Delete Hyperty with " + msg.body.value.hypertyURL);
      registry.deleteHyperty(msg.body.value.user, msg.body.value.hypertyURL, callback);
      break;
  }

});
