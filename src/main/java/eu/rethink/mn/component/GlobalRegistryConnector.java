package eu.rethink.mn.component;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.message.PipeMessage;
import io.vertx.core.json.JsonObject;
import eu.rethink.mn.pipeline.PipeRegistry;

public class GlobalRegistryConnector implements IComponent {
	final String name;
	final PipeRegistry register;

	public GlobalRegistryConnector(PipeRegistry register) {
		this.register = register;
		this.name = "global://registry/";
	}

	@Override
	public String getName() { return name; }

	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();

		register.getEventBus().send("mn:/global-registry-connector", msg.getJson().encode(), event -> {
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
