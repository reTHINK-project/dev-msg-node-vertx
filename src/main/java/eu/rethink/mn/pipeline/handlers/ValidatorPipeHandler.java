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
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.message.PipeMessage;

/**
 * @author micaelpedrosa@gmail.com
 * Message interceptor that validates mandatory fields on JSON data.
 */
public class ValidatorPipeHandler implements Handler<PipeContext> {
	public static String NAME = "mn:/validator"; 

	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		
		//header validation...
		final JsonObject json = msg.getJson();
		
		if(!json.containsKey("id")) {
			ctx.fail(NAME, "No mandatory field 'id'");
		}
		
		if(!json.containsKey("type")) {
			ctx.fail(NAME, "No mandatory field 'type'");
		}
		
		final String from = json.getString("from");
		if(from == null) {
			ctx.fail(NAME, "No mandatory field 'from'");
		}

		final String to = json.getString("to");
		if(to == null) {
			ctx.fail(NAME, "No mandatory field 'to'");
		}

		ctx.next();
	}

}
