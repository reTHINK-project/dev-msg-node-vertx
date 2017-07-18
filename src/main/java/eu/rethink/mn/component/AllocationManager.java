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

/**
 * @author micaelpedrosa@gmail.com
 * @author lduarte.suil@gmail.com
 * Address allocation manager for hyperties and objects.
 */
public class AllocationManager implements IComponent {
	final String name;
	final PipeRegistry register;

	final String baseURL;

	public AllocationManager(PipeRegistry register) {
		this.register = register;
		this.name = "domain://msg-node." + register.getDomain()  + "/address-allocation";
		this.baseURL = "://" + register.getDomain() + "/";
	}

	@Override
	public String getName() { return name; }

	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		final JsonObject body = msg.getBody();

		if(msg.getType().equals("create")) {
			//process JSON msg requesting a number of available addresses
			final JsonObject msgBodyValue = body.getJsonObject("value");
			final String scheme = body.getString("scheme");

			int number = msgBodyValue.getInteger("number", 5);
			final List<String> allocated = allocate(ctx, scheme, number);

			final PipeMessage reply = new PipeMessage();
			reply.setId(msg.getId());
			reply.setFrom(name);
			reply.setTo(msg.getFrom());
			reply.setReplyCode(ReplyCode.OK);

			final JsonObject value = new JsonObject();
			value.put("allocated", new JsonArray(allocated));

			reply.getBody().put("value", value);

			ctx.reply(reply);

		}  else if(msg.getType().equals("delete")) {
			//process JSON msg releasing an address
			final String resource = body.getString("resource");
			final JsonArray childrenResourcesList = body.getJsonArray("childrenResources");
			if (resource != null) {
				deallocate(ctx, resource);
			} else {
				for(Object childrenResource: childrenResourcesList) {
					deallocate(ctx, childrenResource.toString());
				}
			}

			ctx.replyOK(name);
		}
	}

	private List<String> allocate(PipeContext ctx, String scheme, int number) {
		final ArrayList<String> list = new ArrayList<String>(number);
		int i = 0;
		while(i < number) {
			//find unique url, not in registry...
			String url;
			if (scheme == null) {
				url = "hyperty" + baseURL + UUID.randomUUID().toString();
			} else {
				url = scheme + baseURL + UUID.randomUUID().toString();
			}

			if(ctx.getSession().allocate(url)) {
				list.add(url);
				i++;
			}
		}

		return list;
	}

	private void deallocate(PipeContext ctx, String url) {
		ctx.getSession().deallocate(url);
	}
}
