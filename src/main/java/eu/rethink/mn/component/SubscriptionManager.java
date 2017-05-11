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

/**
 * @author micaelpedrosa@gmail.com
 * Class that handles the domain subscription manager.
 * Subscribe and UnSubscribe for listeners.
 */
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
		
		final JsonArray addressList = body.getJsonArray("resources");
		final JsonArray addressList2 = body.getJsonArray("subscribe");
		
		if(addressList != null && addressList2 == null) {
			
			addToAddressList(ctx, addressList);
			
		} else if (addressList == null && addressList2 != null) {
			
			addToAddressList(ctx, addressList2);
			
		}else if (addressList != null && addressList2 != null) {
			
			ctx.replyError(name, "You cant use field body.resources' and 'body.subscribe' at the same time");
			
		} else {
			
			ctx.replyError(name, "No mandatory field 'body.resources' or 'body.subscribe'");
			
		}
	}
	
	public void addToAddressList(PipeContext ctx, JsonArray addressList) {
		final PipeMessage msg = ctx.getMessage();
		final JsonObject body = msg.getBody();
		final JsonArray subscribeList = addressList;
		
		//subscribe to a list of addresses
		if(msg.getType().equals("subscribe")) {
			for(Object address: subscribeList) {
				ctx.getSession().addListener(address.toString());
			}
			
			ctx.replyOK(name);
		} else if(msg.getType().equals("unsubscribe")) {
			//unsubscribe from a list of addresses
			for(Object address: subscribeList) {
				ctx.getSession().removeListener(address.toString());
			}
			
			ctx.replyOK(name);
		} else {
			ctx.replyError(name, "Unrecognized type '" + msg.getType() + "'");
		}
		
	}
}
