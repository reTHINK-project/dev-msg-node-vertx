package eu.rethink.mn.pipeline;

import eu.rethink.mn.pipeline.message.PipeMessage;
import io.vertx.core.Handler;

public class PipeResource {
	final String uid;
	final Pipeline pipeline;
	
	//TODO: maybe this should be replaced by a PipeSession { runtimeURL, idpURL, tokenID, userID }
	String runtimeSessionUrl;
	
	final Handler<Void> closeCallback;
	final Handler<String> replyCallback;
	
	PipeResource(String uid, Pipeline pipeline, Handler<Void> closeCallback, Handler<String> replyCallback) {
		this.uid = uid;
		this.pipeline = pipeline;
		
		this.closeCallback = closeCallback;
		this.replyCallback = replyCallback;
	}
	
	public String getUid() { return uid; }
	
	public String getRuntimeSessionUrl() { return runtimeSessionUrl; }
	public void setRuntimeSessionUrl(String runtimeUrl) {
		this.runtimeSessionUrl = runtimeUrl;
	}
	
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
