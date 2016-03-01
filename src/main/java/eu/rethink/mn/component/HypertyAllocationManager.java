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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.message.PipeMessage;
import eu.rethink.mn.pipeline.message.ReplyCode;

public class HypertyAllocationManager implements IComponent {
	final String name;
	final PipeRegistry register;

	final String baseURL;
	
	public HypertyAllocationManager(PipeRegistry register) {
		this.register = register;
		this.name = "domain://msg-node." + register.getDomain()  + "/hyperty-address-allocation";
		this.baseURL = "hyperty://" + register.getDomain() + "/";
	}
	
	@Override
	public String getName() { return name; }
	
	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		
		if(msg.getType().equals("create")) {
			final JsonObject msgBodyValue = msg.getBody().getJsonObject("value");
			
			int number = msgBodyValue.getInteger("number", 5);
			final List<String> allocated = allocate(ctx, number);
		
			final PipeMessage reply = new PipeMessage();
			reply.setId(msg.getId());
			reply.setFrom(name);
			reply.setTo(msg.getFrom());
			reply.setReplyCode(ReplyCode.OK);
			
			final JsonObject value = new JsonObject();
			value.put("allocated", new JsonArray(allocated));
			
			reply.getBody().put("value", value);
			
			ctx.reply(reply);
		} else {
			//TODO: deallocate !?
		}
	}

	private List<String> allocate(PipeContext ctx, int number) {
		final ArrayList<String> list = new ArrayList<String>(number);
		int i = 0;
		while(i < number) {
			//find unique url, not in registry...
			final String url = baseURL + UUID.randomUUID().toString();
			if(ctx.getSession().allocate(url)) {
				list.add(url);
				i++;
			}
		}
		
		return list;
	}
}
