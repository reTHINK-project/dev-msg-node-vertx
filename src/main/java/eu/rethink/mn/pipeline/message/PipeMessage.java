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
