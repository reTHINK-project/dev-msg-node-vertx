package eu.rethink.mn.component;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import io.vertx.core.json.JsonObject;
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
		
		register.getEventBus().send(name, msg.getJson().encode(), event -> {
			final Object val = event.result().body();
			if(event.succeeded()) {
				//reply: {"123-1":{"catalogAddress":"12345678","guid":"123131241241241","lastUpdate":"2015-11-30"}}

				final PipeMessage replyMsg = new PipeMessage();
				replyMsg.setId(msg.getId());
				replyMsg.setFrom(msg.getTo());
				replyMsg.setTo(msg.getFrom());
				replyMsg.setBody(new JsonObject(val.toString()));
				ctx.reply(replyMsg);
			}else {
				ctx.fail(name, "Error contacting domain registry");
			}
		});
	}
}
