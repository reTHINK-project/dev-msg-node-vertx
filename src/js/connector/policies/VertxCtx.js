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

var ReThinkCtx = require('runtime-core/dist/ReThinkCtx');

var VertxCtx = function() {
  ReThinkCtx.apply(this, arguments);
};

VertxCtx.prototype = ReThinkCtx.prototype;
VertxCtx.prototype.constructor = VertxCtx;

VertxCtx.prototype.authorise = function(message) {
  var result;
  if (this.pep._isToVerify(message)) {
    result = this.pep.pdp.evaluatePolicies(message);
    if (result === undefined || result === 'Not Applicable') {
      result = this.defaultBehavior;
      message.body.auth = false;
    }
    this.pep.actionsService.enforcePolicies(message, result);
    if (result) {
      message.body.auth = (message.body.auth === undefined) ? true : message.body.auth;
      return true;
    } else {
      return false;
    }
  } else {
    result = this.defaultBehavior;
    if (result) {
      message.body.auth = false;
      return true;
    } else {
      return false;
    }
  }
};

VertxCtx.prototype.getPolicies = function() {
  return { serviceProviderPolicy: this.serviceProviderPolicy };
};

VertxCtx.prototype.loadConfigurations = function() {
  this.defaultBehavior = true;
};

VertxCtx.prototype.savePolicies = function(source, policy) {
  this.serviceProviderPolicy = policy;
};

module.exports = VertxCtx;
