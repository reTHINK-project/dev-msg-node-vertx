package eu.rethink.mn.pipeline.handlers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.message.PipeMessage;

public class ValidatorPipeHandler implements Handler<PipeContext> {
	public static String NAME = "mn:/validator"; 

	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		
		//header validation...
		final JsonObject json = msg.getJson();
		
		if(!json.containsKey("id")) {
			ctx.fail(NAME, "No mandatory field 'id'");
		}
		
		if(!json.containsKey("type")) {
			ctx.fail(NAME, "No mandatory field 'type'");
		}
		
		final String from = json.getString("from");
		if(from == null) {
			ctx.fail(NAME, "No mandatory field 'from'");
		}

		final String to = json.getString("to");
		if(to == null) {
			ctx.fail(NAME, "No mandatory field 'to'");
		}

		ctx.next();
	}

}
