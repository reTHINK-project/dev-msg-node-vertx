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
