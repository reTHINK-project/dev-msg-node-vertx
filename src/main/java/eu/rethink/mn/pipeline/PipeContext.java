package eu.rethink.mn.pipeline;

import java.util.Iterator;

import eu.rethink.mn.IComponent;
import io.vertx.core.Handler;

public class PipeContext {
	boolean inFail = false;
	
	final Pipeline pipeline;
	final PipeResource resource;
	
	final Iterator<Handler<PipeContext>> iter;
	final PipeMessage msg;
	
	public PipeMessage getMessage() { return msg; }
	public String getResourceUid() { return resource.getUid(); }
	
	PipeContext(Pipeline pipeline, PipeResource resource, Iterator<Handler<PipeContext>> iter, PipeMessage msg) {
		System.out.println("IN: " + msg);
		this.pipeline = pipeline;
		this.resource = resource;
		this.iter = iter;
		this.msg = msg;
	}
	
	/** Sends the context to the delivery destination. Normally this methods is called in the end of the pipeline process.
	 *  So most of the time there is no need to call this.
	 */
	public void deliver() {
		final PipeRegistry register = pipeline.getRegister();
		final String url = register.resolve(msg.getTo());
		
		if(url == null) {
			//send to internal component...
			final IComponent comp = register.getComponent(msg.getTo());
			if(comp != null) {
				comp.handle(this);
			}
		} else {
			System.out.println("OUT: " + msg);
			register.getEventBus().publish(url, msg.toString());
		}
	}
	
	/** Does nothing to the pipeline flow and sends a reply back.
	 * @param reply Should be a new PipeMessage
	 */
	public void reply(PipeMessage reply) {
		System.out.println("REPLY: " + reply);
		resource.reply(reply);
	}
	
	/** Does nothing to the pipeline flow and sends a OK reply back with a pre formatted JSON schema.  
	 * @param from The address that will be on "header.from".
	 */
	public void replyOK(String from) {
		final PipeMessage reply = new PipeMessage();
		reply.setId(msg.getId());
		reply.setFrom(from);
		reply.setTo(msg.getFrom());
		reply.setReplyCode("ok");
		
		reply(reply);
	}
	
	/** Does nothing to the pipeline flow and sends a ERROR reply back with a pre formatted JSON schema. 
	 * @param from The address that will be on "header.from".
	 * @param error The error descriptor message.
	 */
	public void replyError(String from, String error) {
		final PipeMessage reply = new PipeMessage();
		reply.setId(msg.getId());
		reply.setFrom(from);
		reply.setTo(msg.getFrom());
		reply.setReplyCode("error");
		reply.setErrorDescription(error);
		
		reply(reply);
	}
	
	/** Order the underlying resource channel to disconnect. But the client protostub can be configured to reconnect, so most of the times a reconnection is made by the client.
	 * To avoid this, the method should only be used when the client orders the disconnection.
	 */
	public void disconnect() {
		resource.disconnect();
	}
	
	/** Used by interceptors, order the pipeline to execute the next interceptor. If no other interceptor exits, a delivery is proceed.
	 */
	public void next() {
		if(!inFail) {
			if(iter.hasNext()) {
				iter.next().handle(this);
			} else {
				deliver();
			}
		}
	}
	
	/** Interrupts the pipeline flow and sends an error message back to the original "header.from". After this, other calls to "next()" or "fail(..)" are useless.
	 * @param from The address that will be on reply "header.from".
	 * @param error The error descriptor message.
	 */
	public void fail(String from, String error) {
		if(!inFail) {
			inFail = true;
			replyError(from, error);
			if(pipeline.failHandler != null) {
				pipeline.failHandler.handle(error);
			}
		}
	}
}
