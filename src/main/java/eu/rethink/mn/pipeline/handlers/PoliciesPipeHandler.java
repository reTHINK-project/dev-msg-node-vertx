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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import eu.rethink.mn.pipeline.message.PipeMessage;
import eu.rethink.mn.pipeline.message.ReplyCode;
import eu.rethink.mn.pipeline.PipeContext;

public class PoliciesPipeHandler implements Handler<PipeContext> {
	public static String NAME = "mn:/policies";
	public String policies;
	public boolean firstMessage;

	public PoliciesPipeHandler() {
		this.policies = this.loadPolicyFile();
		this.firstMessage = true;
	}

	@Override
	public void handle(PipeContext ctx) {
		if (firstMessage && this.policies.length() != 0) {
			ctx.getPipeline().getRegister().getEventBus().send("mn:/policies-connector-update", this.policies);
			firstMessage = false;
		}

		final PipeMessage msg = ctx.getMessage();
		ctx.getPipeline().getRegister().getEventBus().send("mn:/policies-connector", msg.getJson().encode(), event -> {
			final Object decision = event.result().body();
			if ((Boolean) decision) {
				ctx.next();
			} else {
				ctx.fail(NAME, "Message blocked by policy");
			}
		});
	}

	public String loadPolicyFile() {
		InputStream is;
		String line;
		StringBuilder sb = new StringBuilder();
		String fileAsString = "";
		try {
			is = new FileInputStream("./policy.json");
			BufferedReader buf = new BufferedReader(new InputStreamReader(is));
			line = buf.readLine();
			while (line != null) {
				sb.append(line).append("\n");
				line = buf.readLine();
			}
			fileAsString = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileAsString;
	}
}
