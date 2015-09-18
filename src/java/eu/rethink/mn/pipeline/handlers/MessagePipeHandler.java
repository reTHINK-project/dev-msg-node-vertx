package eu.rethink.mn.pipeline.handlers;

import io.vertx.core.Handler;
import eu.rethink.mn.Register;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeMessage;
import eu.rethink.mn.pipeline.PipeResource;

public class MessagePipeHandler implements Handler<PipeContext> {
	
	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();

		System.out.println("MessagePipeHandler: " + msg);
		final PipeResource res = Register.getResource(msg.getTo());
		res.sendMessage(msg);
		
		ctx.next();
	}
}
