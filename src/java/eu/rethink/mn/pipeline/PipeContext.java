package eu.rethink.mn.pipeline;

import java.util.Iterator;
import io.vertx.core.Handler;

public class PipeContext {
	boolean inFail = false;
	
	final Pipeline pipeline;
	final PipeResource resource;
	
	final Iterator<Handler<PipeContext>> iter;
	final PipeMessage msg;
	
	PipeContext(Pipeline pipeline, PipeResource resource, Iterator<Handler<PipeContext>> iter, PipeMessage msg) {
		this.pipeline = pipeline;
		this.resource = resource;
		this.iter = iter;
		this.msg = msg;
	}
	
	public Pipeline getPipeline() {return pipeline;}
	public PipeResource getResource() {return resource;}
	
	public PipeMessage getMessage() {return msg;}
	
	public void next() {
		if(!inFail && iter.hasNext()) {
			iter.next().handle(this);
		}
	}
	
	public void fail(Throwable ex) {
		if(!inFail) {
			inFail = true;
			resource.replyError(msg, ex.getMessage());
			if(pipeline.failHandler != null) {
				pipeline.failHandler.handle(ex);
			}
		}
	}
}
