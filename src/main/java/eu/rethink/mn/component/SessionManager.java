package eu.rethink.mn.component;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.message.PipeMessage;

public class SessionManager implements IComponent {
	final PipeRegistry register;
	
	public SessionManager(PipeRegistry register) {
		this.register = register;
	}
	
	@Override
	public String getName() { return "mn:/session"; }
	
	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();

		if(msg.getType().equals("open")) {
			register.bind(msg.getFrom(), ctx.getResourceUid());
			ctx.getResource().setRuntimeUrl(msg.getFrom());
			ctx.replyOK(getName());
		}
		
		if(msg.getType().equals("close")) {
			register.unbind(msg.getFrom());
			ctx.disconnect();
		}
		
		//TODO: manage ping message to maintain the open connection?
		//how to handle timeouts ?
		//if(msg.getType().equals("ping")) {}
	}
}
