package eu.rethink.mn.component;

import java.util.UUID;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.message.PipeMessage;
import eu.rethink.mn.pipeline.message.ReplyCode;

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
		final String type = msg.getType();
		final String runtimeURL = msg.getFrom();

		if(type.equals("open")) {
			//(new connection) request - ok
			final String newRuntimeToken = UUID.randomUUID().toString();
			final String runtimeSessionURL = runtimeURL + "/" + newRuntimeToken;
			
			register.bind(runtimeSessionURL, ctx.getResourceUid());
			ctx.getResource().setRuntimeSessionUrl(runtimeSessionURL);
			
			final PipeMessage reply = new PipeMessage();
			reply.setId(msg.getId());
			reply.setFrom(getName());
			reply.setTo(msg.getFrom());
			reply.setReplyCode(ReplyCode.OK);
			reply.getBody().put("runtimeToken", newRuntimeToken);
			
			System.out.println("SESSION-OPEN: " + runtimeSessionURL);
			ctx.reply(reply);
			
		} else if(type.equals("re-open")) {
			//(reconnection) request
			final String runtimeSessionURL = runtimeURL;
			
			if(register.resolve(runtimeSessionURL) != null) {
				//(reconnection) ok
				register.rebind(runtimeSessionURL, ctx.getResourceUid());
				ctx.getResource().setRuntimeSessionUrl(runtimeSessionURL);
				
				System.out.println("SESSION-REOPEN: " + runtimeSessionURL);
				ctx.replyOK(getName());
			} else {
				//(reconnection) fail
				ctx.fail(getName(), "Reconnection fail. Incorrect runtime token!");
			}

		} else if(type.equals("close")) {
			final String runtimeSessionURL = ctx.getRuntimeSessionUrl();
			if (runtimeSessionURL != null) {
				register.unbind(runtimeSessionURL);
			
				System.out.println("SESSION-CLOSE: " + runtimeSessionURL);
				ctx.disconnect();
			}
		}
		
		//TODO: manage ping message to maintain the open connection?
		//how to handle timeouts and resource release?
		//if(msg.getType().equals("ping")) {}
	}
}
