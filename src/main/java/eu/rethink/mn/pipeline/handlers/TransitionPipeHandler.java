package eu.rethink.mn.pipeline.handlers;

import io.vertx.core.Handler;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeRegistry;

public class TransitionPipeHandler implements Handler<PipeContext> {
	public static String NAME = "mn:/transition"; 

	@Override
	public void handle(PipeContext ctx) {
		final String from = ctx.getMessage().getFrom();
		
		//search for unregistered hyperties...
		//TODO: should it be verified (security risk of registering a listener for not owned hyperties)
		if (from.startsWith("hyperty")) {
			final PipeRegistry registry = ctx.getRegistry();
			if (registry.resolve(from) == null) {
				System.out.println("T-HYPERTY: " + from);
				registry.allocate(from, ctx.getRuntimeSessionUrl());
			}
		}
		
		ctx.next();
	}

}
