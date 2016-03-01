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

import eu.rethink.mn.pipeline.message.PipeMessage;
import io.vertx.core.Handler;

public class PipeResource {
	final String uid;
	final Pipeline pipeline;
	
	PipeSession session = null;
	
	final Handler<Void> closeCallback;
	final Handler<String> replyCallback;
	
	PipeResource(String uid, Pipeline pipeline, Handler<Void> closeCallback, Handler<String> replyCallback) {
		this.uid = uid;
		this.pipeline = pipeline;
		
		this.closeCallback = closeCallback;
		this.replyCallback = replyCallback;
	}
	
	public String getUid() { return uid; }
	
	public PipeSession getSession() { return session; }
	public void setSession(PipeSession session) { this.session = session; }
	
	public void processMessage(PipeMessage msg) {
		pipeline.process(this, msg);
	}
	
	void reply(PipeMessage msg) {
		replyCallback.handle(msg.toString());
	}
	
	void disconnect() {
		closeCallback.handle(null);
	}
}
