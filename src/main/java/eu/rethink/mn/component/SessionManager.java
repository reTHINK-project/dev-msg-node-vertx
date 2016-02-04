package eu.rethink.mn.component;

import java.util.UUID;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.PipeSession;
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
			System.out.println("SESSION-OPEN: " + runtimeSessionURL);
			
			final PipeSession session = register.createSession(runtimeSessionURL);
			ctx.setSession(session);
			
			//FIX: this hack should not be here! Maybe there should be a separated message flow to register the runtime SM?
			session.addListener(runtimeURL + "/sm");
			
			final PipeMessage reply = new PipeMessage();
			reply.setId(msg.getId());
			reply.setFrom(getName());
			reply.setTo(msg.getFrom());
			reply.setReplyCode(ReplyCode.OK);
			reply.getBody().put("runtimeToken", newRuntimeToken);
			
			ctx.reply(reply);
			
		} else if(type.equals("re-open")) {
			//(reconnection) request
			final PipeSession session = register.getSession(runtimeURL);
			if(session != null) {
				System.out.println("SESSION-REOPEN: " + runtimeURL);
				ctx.setSession(session);
				ctx.replyOK(getName());
			} else {
				//(reconnection) fail
				ctx.fail(getName(), "Reconnection fail. Incorrect runtime token!");
			}

		} else if(type.equals("close")) {
			final PipeSession session = ctx.getSession();
			if (session != null) {
				System.out.println("SESSION-CLOSE: " + session.getRuntimeSessionURL());
				ctx.disconnect();
			}
		}
		
		//TODO: manage ping message to maintain the open connection?
		//how to handle timeouts and resource release?
		//if(msg.getType().equals("ping")) {}
	}
}
