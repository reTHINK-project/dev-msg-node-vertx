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
      case "add-user":
      print("[Registry-Connector] Add user with " + msg.body.userid);
      registry.createUser(msg.body.userid, callback);
      break;

      case "get-user":
      print("[Registry-Connector] Get hyperty with " + msg.body.userid);
      registry.getUser(msg.body.userid, callback);
      break;

      case "get-hyperty":
      print("[Registry-Connector] Get user with " + msg.body.hypertyid);
      registry.getHyperty(msg.body.userid, msg.body.hypertyid, callback);
      break;

      case "add-hyperty":
      print("[Registry-Connector] Add Hyperty with " + msg.body.hypertyid);
      registry.addHyperty(msg.body.userid, msg.body.hypertyid, msg.body.data, callback);
      break;
  }

});