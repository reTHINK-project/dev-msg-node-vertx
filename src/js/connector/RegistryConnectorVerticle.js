var RegistryConnector = require('./registry/RegistryConnector');

print("[Connectors] Registry Connector Loaded");

var registry = new RegistryConnector('http://localhost:4567');

vertx.eventBus().consumer("mn:/registry-connector", function (message) {
  print("[Registry-Connector][Received]: " + message.body());

  var msg = JSON.parse(message.body());

  var callback = function(msg) {
      return message.reply(msg);
  };

  switch(msg.header.type) {
      case "add-user":
      print("[Registry-Connector] Add user with " + msg.body.userid);
      registry.createUser(msg.body.userid, callback);
      break;

      case "get-user":
      print("[Registry-Connector] Get user with " + msg.body.userid);
      registry.getUser(msg.body.userid, callback);
      break;
  }

});