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

package eu.rethink.mn.pipeline;

import java.util.ArrayList;
import java.util.Iterator;

import eu.rethink.mn.pipeline.message.PipeMessage;
import io.vertx.core.Handler;

public class Pipeline {
	final PipeRegistry register;
	
	final ArrayList<Handler<PipeContext>> handlers = new ArrayList<Handler<PipeContext>>();
	Handler<String> failHandler = null;
	
	void process(PipeResource resource, PipeMessage msg) {
		final Iterator<Handler<PipeContext>> iter = handlers.iterator();
		final PipeContext ctx = new PipeContext(this, resource, iter, msg);
		ctx.next();
	}
	
	public Pipeline(PipeRegistry register) {
		this.register = register;
	}
	
	public PipeRegistry getRegister() { return register; }
	
	public PipeResource createResource(String uid, Handler<Void> closeCallback, Handler<String> replyCallback) {
		final PipeResource resource = new PipeResource(uid, this, closeCallback, replyCallback);
		return resource;
	}
	
	public Pipeline addHandler(Handler<PipeContext> handler) {
		handlers.add(handler);
		return this;
	}
	
	public Pipeline failHandler(Handler<String> handler) {
		failHandler = handler;
		return this;
	}
}
