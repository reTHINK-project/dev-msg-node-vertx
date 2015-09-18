package eu.rethink.mn.pipeline;

import io.vertx.core.eventbus.EventBus;

public class PipeResource {
	final Pipeline pipeline;
	final String uid;
	
	PipeResource(Pipeline pipeline, String uid) {
		this.pipeline = pipeline;
		this.uid = uid;
	}
	
	public String getUid() {return uid;}
	
	public void processMessage(PipeMessage msg) {
		pipeline.process(this, msg);
	}
	
	public void reply(PipeMessage msg) {
		msg.setType("reply");
		sendMessage(msg);
	}
	
	public void replyOK(PipeMessage original) {
		final PipeMessage msg = new PipeMessage();
		msg.setId(original.getId());
		msg.setTo(original.getFrom());
		msg.setReplyCode("ok");
		
		reply(msg);
	}
	
	public void replyError(PipeMessage original, String error) {
		final PipeMessage msg = new PipeMessage();
		msg.setId(original.getId());
		msg.setTo(original.getFrom());
		msg.setReplyCode("error");
		msg.setErrorDescription(error);
		
		reply(msg);
	}
	
	public void sendMessage(PipeMessage msg) {
		final EventBus eb = pipeline.getEventBus();
		eb.send(uid, msg.toString());
	}
}
