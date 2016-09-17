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

var PoliciesConnector = function() {
  var PEP = require('runtime-core/dist/PEP');
  var VertxCtx = require('./VertxCtx');
  this.pep = new PEP(new VertxCtx());
};

PoliciesConnector.prototype.authorise = function(message, callback) {
  callback(this.pep.authorise(JSON.parse(message.body())));
};

PoliciesConnector.prototype.addPolicy = function(policies) {
  if (policies !== undefined) {
    for (var i in policies) {
      this.pep.addPolicy('SERVICE_PROVIDER', i, policies[i]);
      /*var rules = policies[i].rules;
      for (var j in rules) {
        if (rules[j].condition.attribute !== undefined) {
          var condition = [rules[j].condition.attribute, rules[j].condition.operator, rules[j].condition.params];
          this.pep.context.serviceProviderPolicy.createRule('simple', rules[j].authorise, condition, rules[j].target, rules[j].scope);
        } else {
          this.pep.context.serviceProviderPolicy.createRule('advanced', rules[j].authorise, rules[j].condition, rules[j].target, rules[j].scope);
        }
      }*/
    }
  }
};

module.exports = PoliciesConnector;
