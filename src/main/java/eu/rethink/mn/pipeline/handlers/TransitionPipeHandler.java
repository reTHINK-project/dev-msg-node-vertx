package eu.rethink.mn.pipeline.handlers;

import io.vertx.core.Handler;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.message.PipeMessage;

public class TransitionPipeHandler implements Handler<PipeContext> {
	public static String NAME = "mn:/transition"; 

	@Override
	public void handle(PipeContext ctx) {
		//TODO: how process hyperty messages from external domain?
		
		//TODO: hyperty://matrix.docker
		final PipeMessage msg = ctx.getMessage();
		final String[] parts = msg.getFrom().split("/");
		
		//System.out.println(parts);
		
		
		ctx.next();
	}

}
