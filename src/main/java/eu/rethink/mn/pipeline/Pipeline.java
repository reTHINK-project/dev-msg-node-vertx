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

/**
 * @author micaelpedrosa@gmail.com
 * Implementation of a pipelice circuit with interceptors and endpoints.
 */
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
	
	/** Create a connection resource that can be used in any specific implementation, websockets, tcp... 
	 * @param uid UUID for the resource
	 * @param closeCallback Callback function called when some internal process fired a request to close the connection.
	 * @param replyCallback Callback function called for any reply message in a request-response protocol scheme. It's natural that a response flows through the same link as the request.
	 * @return himself for fluent API
	 */
	public PipeResource createResource(String uid, Handler<Void> closeCallback, Handler<String> replyCallback) {
		final PipeResource resource = new PipeResource(uid, this, closeCallback, replyCallback);
		return resource;
	}
	
	/**
	 * @param handler Add interceptor handler for every message entering the pipeline.
	 * @return himself for fluent API
	 */
	public Pipeline addHandler(Handler<PipeContext> handler) {
		handlers.add(handler);
		return this;
	}
	
	/**
	 * @param handler Set the fail handler, for unexpected errors in the pipeline.
	 * @return himself for fluent API
	 */
	public Pipeline failHandler(Handler<String> handler) {
		failHandler = handler;
		return this;
	}
}
