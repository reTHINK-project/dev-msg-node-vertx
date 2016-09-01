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

package eu.rethink.mn.pipeline.handlers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import eu.rethink.mn.pipeline.message.PipeMessage;
import eu.rethink.mn.pipeline.message.ReplyCode;
import eu.rethink.mn.pipeline.PipeContext;

public class PoliciesPipeHandler implements Handler<PipeContext> {
	public static String NAME = "mn:/policies";

	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		ctx.getPipeline().getRegister().getEventBus().send("mn:/policies-connector", msg.getJson().encode(), event -> {

			final Object decision = event.result().body();
			if (event.succeeded()) {
				ctx.next();
			} else {
				ctx.fail(NAME, "Error contacting the Policy Engine");
			}

		});

	}

}
