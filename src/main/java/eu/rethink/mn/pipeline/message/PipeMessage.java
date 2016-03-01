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

package eu.rethink.mn.pipeline.message;

import io.vertx.core.json.JsonObject;

public class PipeMessage {
	public static final String REPLY = "response";
	
	final JsonObject msg;
	
	public PipeMessage() {
		this(new JsonObject());
	}

	public PipeMessage(String json) {
		this(new JsonObject(json));
	}
	
	public PipeMessage(JsonObject msg) {
		this.msg = msg;
	}
	
	public JsonObject getJson() { return msg; }
	
	public JsonObject getBody() {
		if(!msg.containsKey("body")) {
			msg.put("body", new JsonObject());
		}
		
		return msg.getJsonObject("body"); 
	}
	public PipeMessage setBody(JsonObject body) {
		msg.put("body", body);
		return this;
	}
	
	public int getId() { return msg.getInteger("id", 0); }
	public PipeMessage setId(int id) {
		msg.put("id", id);
		return this;
	}
	
	public String getFrom() { return msg.getString("from"); }
	public PipeMessage setFrom(String from) {
		msg.put("from", from);
		return this;
	}
	
	public String getTo() { return msg.getString("to"); }
	public PipeMessage setTo(String to) {
		msg.put("to", to);
		return this;
	}
	
	public String getType() { return msg.getString("type"); }
	public PipeMessage setType(String type) {
		msg.put("type", type);
		return this;
	}
	
	public int getReplyCode() { return getBody().getInteger("code"); }
	public PipeMessage setReplyCode(ReplyCode code) {
		getBody().put("code", code.code);
		return this;
	}
	
	public String getErrorDescription() { return getBody().getString("desc"); }
	public PipeMessage setErrorDescription(String desc) {
		getBody().put("desc", desc);
		return this;
	}
	
	@Override
	public String toString() {
		return msg.toString();
	}
}
