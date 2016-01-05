var RegistryConnector = require('./registry/RegistryConnector');

print("[Connectors] Registry Connector Loaded");

var registry = new RegistryConnector('http://citysdk.tagus.ist.utl.pt:4567');

vertx.eventBus().consumer("mn:/registry-connector", function (message) {
  print("[Registry-Connector][Received]: " + message.body());

  var msg = JSON.parse(message.body());

  var callback = function(msg) {
      return message.reply(msg);
  };

  switch(msg.type) {
      case "READ":
      print("[Registry-Connector] Get user with " + msg.body.user);
      registry.getUser(msg.body.user, callback);
      break;

      case "CREATE":
      print("[Registry-Connector] Add Hyperty with " + msg.body.hypertyURL);
      registry.addHyperty(msg.body.user, msg.body.hypertyURL, msg.body.hypertyDescriptorURL, callback);
      break;
  }

});
