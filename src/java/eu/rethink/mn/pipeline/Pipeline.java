package eu.rethink.mn.pipeline;

import java.util.ArrayList;
import java.util.Iterator;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;

public class Pipeline {
	final EventBus eb;
	
	final ArrayList<Handler<PipeContext>> handlers = new ArrayList<Handler<PipeContext>>();
	Handler<Throwable> failHandler = null;
	
	void process(PipeResource resource, PipeMessage msg) {
		final Iterator<Handler<PipeContext>> iter = handlers.iterator();
		if(iter.hasNext()) {
			//TODO: handle async??
			iter.next().handle(new PipeContext(this, resource, iter, msg));
		}
	}
	
	public Pipeline(EventBus eb) {
		this.eb = eb;
	}
	
	public EventBus getEventBus() {return eb;}
	
	public PipeResource createResource(String uid) {
		final PipeResource resource = new PipeResource(this, uid);
		return resource;
	}
	
	public Pipeline addHandler(Handler<PipeContext> handler) {
		//TODO: handle async parameter?
		handlers.add(handler);
		return this;
	}
	
	public Pipeline failHandler(Handler<Throwable> handler) {
		failHandler = handler;
		return this;
	}
}
