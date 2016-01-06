package eu.rethink.mn.pipeline.handlers;

import io.vertx.core.Handler;
import eu.rethink.mn.pipeline.PipeContext;

public class TransitionPipeHandler implements Handler<PipeContext> {
	public static String NAME = "mn:/transition"; 

	@Override
	public void handle(PipeContext ctx) {
		final String from = ctx.getMessage().getFrom();
		
		//search for unregistered hyperties...
		//TODO: should it be verified (security risk of registering a listener for not owned hyperties)
		if (from.startsWith("hyperty")) {
			if (ctx.resolve(from) == null) {
				System.out.println("T-HYPERTY: " + from);
				ctx.getSession().allocate(from);
			}
		}
		
		ctx.next();
	}

}
