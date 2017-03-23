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

var config = require('../../node.config.json');

var selection = process.env.MSG_NODE_CONFIG;
if (!selection) {
  selection = "dev";
}

var configSelect;
if (selection !== "env") {
	//load from config file
	configSelect = config[selection];
} else {
	var retries = Number(process.env.NODE_REGISTRY_RETRIES);
	var ssl = (process.env.NODE_REGISTRY_SSL === "true");
  var ownCert = (process.env.NODE_REGISTRY_OWN_CERTIFICATES === "true");
  console.log('ACTUAL PATH', __dirname);
	//load from environment variables
	if (ssl == false) {
		configSelect = {
			registry: {
					url: process.env.NODE_REGISTRY_URL,
				retries: retries,
				ssl: {
					enabled: ssl
				}
			},
			globalregistry: {
				url: process.env.NODE_GLOBAL_REGISTRY_URL
			}
		};
	} else if(! ownCert) {
		configSelect = {
			registry: {
					url: process.env.NODE_REGISTRY_URL,
				retries: retries,
				ssl: {
					enabled: ssl,
					trustStore: __dirname + '/truststore.jks',
			    trustStorePass: 'rethink',
			    keyStore: __dirname + '/connector.jks',
			    keyStorePass: 'rethink',
			    keyPassphrase: 'rethink'
				}
			},
			globalregistry: {
				url: process.env.NODE_GLOBAL_REGISTRY_URL
			}
		};

	} else {
    configSelect = {
      registry: {
          url: process.env.NODE_REGISTRY_URL,
        retries: retries,
        ssl: {
          enabled: ssl,
          trustStore: process.env.NODE_REGISTRY_TRUST_STORE,
          trustStorePass: process.env.NODE_REGISTRY_TRUST_STORE_PASS,
          keyStore: process.env.NODE_REGISTRY_KEY_STORE,
          keyStorePass: process.env.NODE_REGISTRY_KEY_STORE_PASS,
          keyPassphrase: process.env.NODE_REGISTRY_KEY_PASS_PHRASE
        }
      },
      globalregistry: {
        url: process.env.NODE_GLOBAL_REGISTRY_URL
      }
    };
  }

}


print("[JS Config] " + JSON.stringify(configSelect));
module.exports = configSelect;
