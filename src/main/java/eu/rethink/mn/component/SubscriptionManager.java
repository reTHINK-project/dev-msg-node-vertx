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

package eu.rethink.mn.component;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.message.PipeMessage;

public class SubscriptionManager implements IComponent {
	final String name;
	final PipeRegistry register;

	public SubscriptionManager(PipeRegistry register) {
		this.register = register;
		this.name = "domain://msg-node." + register.getDomain()  + "/sm";
	}
	
	@Override
	public String getName() { return name; }
	
	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		final JsonObject body = msg.getBody();
		System.out.println("SubscriptionManager: " + msg);
		
		final String resourceURL = body.getString("resource");
		final JsonArray children = body.getJsonArray("childrenResources");
		
		if(resourceURL != null) {
			if(msg.getType().equals("subscribe")) {
				ctx.getSession().addListener(resourceURL + "/changes");
				if(children != null) {
					for(Object child: children) {
						ctx.getSession().addListener(resourceURL + "/children/" + child);
					}
				}
				
				ctx.replyOK(name);
			} else if(msg.getType().equals("unsubscribe")) {
				ctx.getSession().removeListener(resourceURL + "/changes");
				if(children != null) {
					for(Object child: children) {
						ctx.getSession().removeListener(resourceURL + "/children/" + child);
					}
				}
				
				ctx.replyOK(name);
			} else {
				ctx.replyError(name, "Unrecognized type '" + msg.getType() + "'");
			}
		} else {
			ctx.replyError(name, "No mandatory field 'body.resource'");
		}
	}
}
