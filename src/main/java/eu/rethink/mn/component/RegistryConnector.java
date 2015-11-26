package eu.rethink.mn.component;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeMessage;
import eu.rethink.mn.pipeline.PipeRegistry;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

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

		register.getEventBus().send("mn:/registry-connector", msg.getJson().encode(), event -> {
			if(event.succeeded()) {
				JsonObject body = new JsonObject(event.result().body().toString());
				ctx.getMessage().setBody(body);
				ctx.reply(ctx.getMessage());
			}else {
				ctx.fail(getName(), "Error contacting domain registry");
			}

		});


	}
}
