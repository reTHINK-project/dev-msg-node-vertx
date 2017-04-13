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

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.message.PipeMessage;
import io.vertx.core.json.JsonObject;
import eu.rethink.mn.pipeline.PipeRegistry;

public class GlobalRegistryConnector implements IComponent {
	final String name;
	final PipeRegistry register;

	public GlobalRegistryConnector(PipeRegistry register) {
		this.register = register;
		this.name = "global://registry/";
	}

	@Override
	public String getName() { return name; }

	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();

		register.getEventBus().send("mn:/global-registry-connector", msg.getJson().encode(), event -> {
			final Object val = event.result().body();
			if(event.succeeded()) {
				//reply: {"123-1":{"catalogAddress":"12345678","guid":"123131241241241","lastUpdate":"2015-11-30"}}

				final PipeMessage replyMsg = new PipeMessage();
				replyMsg.setId(msg.getId());
				replyMsg.setFrom(msg.getTo());
				replyMsg.setTo(msg.getFrom());
				replyMsg.setBody(new JsonObject(val.toString()));
				ctx.reply(replyMsg);
			}else {
				ctx.fail(name, "Error contacting domain registry");
			}
		});
	}
}
