var config = require(__dirname + '/../../node.config.json');

var configSelect = process.env.MSG_NODE_CONFIG;
if (!configSelect) {
	configSelect = "dev";
}

module.exports = config[configSelect];