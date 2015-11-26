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

		register.getEventBus().send("mn:/registry-connector-verticle", msg.getJson().encode());
		ctx.replyOK(getName());

	}
}
