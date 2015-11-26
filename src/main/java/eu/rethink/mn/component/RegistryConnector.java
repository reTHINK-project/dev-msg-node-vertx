package eu.rethink.mn.component;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.message.PipeMessage;

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
		System.out.println(msg);
		
		ctx.replyOK(name);
	}
}
