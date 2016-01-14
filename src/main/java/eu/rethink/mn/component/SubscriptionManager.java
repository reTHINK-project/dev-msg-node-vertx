package eu.rethink.mn.component;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.message.PipeMessage;

public class SubscriptionManager implements IComponent {
	final String name;
	final PipeRegistry register;

	public SubscriptionManager(PipeRegistry register) {
		this.register = register;
		this.name = "domain://msg-node." + register.getDomain()  + "/sm";
	}
	
	@Override
	public String getName() { return name; }
	
	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		System.out.println("SubscriptionManager: " + msg);
		
		final String resourceURL = msg.getBody().getString("resource");
		if(resourceURL != null) {
			if(msg.getType().equals("subscribe")) {
				ctx.getSession().addListener(resourceURL);
				ctx.replyOK(name);
			} else if(msg.getType().equals("unsubscribe")) {
				ctx.getSession().removeListener(resourceURL);
				ctx.replyOK(name);
			}
		} else {
			ctx.replyError(name, "No mandatory field 'body.resource'");
		}
	}
}
