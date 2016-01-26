package eu.rethink.mn.component;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
		final JsonObject msgBody = msg.getBody();
		System.out.println("SubscriptionManager: " + msg);
		
		final String resourceURL = msgBody.getString("resource");
		final JsonArray children = msgBody.getJsonArray("children");
		
		if(resourceURL != null) {
			if(msg.getType().equals("subscribe")) {
				ctx.getSession().addListener(resourceURL);
				for(Object child: children) {
					ctx.getSession().addListener(resourceURL + "/children/" + child);
				}
				
				ctx.replyOK(name);
			} else if(msg.getType().equals("unsubscribe")) {
				ctx.getSession().removeListener(resourceURL);
				for(Object child: children) {
					ctx.getSession().removeListener(resourceURL + "/children/" + child);
				}
				
				ctx.replyOK(name);
			} else {
				ctx.replyError(name, "Unrecognized type '" + msg.getType() + "'");
			}
		} else {
			ctx.replyError(name, "No mandatory field 'body.resource'");
		}
	}
}
