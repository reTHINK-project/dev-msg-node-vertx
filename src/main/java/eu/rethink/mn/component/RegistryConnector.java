package eu.rethink.mn.component;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeMessage;
import eu.rethink.mn.pipeline.PipeRegistry;

public class RegistryConnector implements IComponent {
	final String name;
	final PipeRegistry register;

	public RegistryConnector(String name, PipeRegistry register) {
		this.name = name;
		this.register = register;
	}
	
	@Override
	public String getName() { return name; }
	
	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		System.out.println("Registry Connector: " + msg);

		if(msg.getType().equals("get-user")) {
			register.getEventBus().send("mn:/registry-connector-verticle", msg.getJson().encode());
			ctx.replyOK(getName());
		}

		if(msg.getType().equals("add-user")) {
			register.getEventBus().send("mn:/registry-connector-verticle", msg.getJson().encode());
			ctx.replyOK(getName());
		}

		if(msg.getType().equals("remove-user")) {
			register.getEventBus().send("mn:/registry-connector-verticle", msg.getJson().encode());
			ctx.replyOK(getName());
		}

		if(msg.getType().equals("add-hiperty")) {
			register.getEventBus().send("mn:/registry-connector-verticle", msg.getJson().encode());
			ctx.replyOK(getName());
		}

	}
}
