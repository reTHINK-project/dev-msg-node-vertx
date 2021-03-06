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

import java.util.UUID;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.PipeSession;
import eu.rethink.mn.pipeline.message.PipeMessage;
import eu.rethink.mn.pipeline.message.ReplyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author micaelpedrosa@gmail.com
 * Class that handles the VertxProtoStub connection and session.
 * This is only for internal use.
 */
public class SessionManager implements IComponent {
	static final Logger logger = LoggerFactory.getLogger("BROKER");
	final PipeRegistry register;

	public SessionManager(PipeRegistry register) {
		this.register = register;
	}

	@Override
	public String getName() { return "mn:/session"; }

	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		final String type = msg.getType();
		final String runtimeURL = msg.getFrom();

		if(type.equals("open")) {

			//(new connection) request - ok
			String runtimeToken = UUID.randomUUID().toString();

			PipeSession session = register.getSessionByRuntime(runtimeURL);
			if (session != null) {
				runtimeToken = session.getRuntimeSessionURL();
				runtimeToken = runtimeToken.substring(runtimeToken.lastIndexOf("/") + 1);
				logger.info("USE EXISTENT SESSION: " + runtimeToken);
			} else {
				final String runtimeSessionURL = runtimeURL + "/" + runtimeToken;
				logger.info("SESSION-OPEN: " + runtimeSessionURL);

				session = register.createSession(runtimeSessionURL);

				//FIX: this hack should not be here! Maybe there should be a separated message flow to register the runtime SM?
				session.addListener(runtimeURL + "/sm");
			}

			ctx.setSession(session);

			final PipeMessage reply = new PipeMessage();
			reply.setId(msg.getId());
			reply.setFrom(getName());
			reply.setTo(msg.getFrom());
			reply.setReplyCode(ReplyCode.OK);
			reply.getBody().put("runtimeToken", runtimeToken);

			ctx.reply(reply);

		} else if(type.equals("re-open")) {
			//(reconnection) request
			final PipeSession session = register.getSession(runtimeURL);
			if(session != null) {
				logger.info("SESSION-REOPEN: " + runtimeURL);
				ctx.setSession(session);
				ctx.replyOK(getName());
			} else {
				//(reconnection) fail
				ctx.fail(getName(), "Reconnection fail. Incorrect runtime token!");
			}

		} else if(type.equals("close")) {
			final PipeSession session = ctx.getSession();
			if (session != null) {
				logger.info("SESSION-CLOSE: " + session.getRuntimeSessionURL());
				ctx.disconnect();
			}
		}

		//TODO: manage ping message to maintain the open connection?
		//how to handle timeouts and resource release?
		//if(msg.getType().equals("ping")) {}
	}
}
