package eu.rethink.mn.component;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.message.PipeMessage;
import eu.rethink.mn.pipeline.message.ReplyCode;

public class ObjectAllocationManager implements IComponent {
	final String name;
	final PipeRegistry register;

	final String baseURL;

	public ObjectAllocationManager(PipeRegistry register) {
		this.register = register;
		this.name = "domain://msg-node." + register.getDomain()  + "/object-address-allocation";
		this.baseURL = "resource://" + register.getDomain() + "/";
	}
	
	@Override
	public String getName() { return name; }
	
	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		
		if(msg.getType().equals("create")) {
			int number = msg.getBody().getInteger("number", 5);
			final List<String> allocated = allocate(ctx, number);
		
			final PipeMessage reply = new PipeMessage();
			reply.setId(msg.getId());
			reply.setFrom(name);
			reply.setTo(msg.getFrom());
			reply.setReplyCode(ReplyCode.OK);
			
			final JsonObject body = reply.getBody();
			body.put("allocated", new JsonArray(allocated));
			
			ctx.reply(reply);
		} else {
			//TODO: deallocate !?
		}
	}

	private List<String> allocate(PipeContext ctx, int number) {
		final ArrayList<String> list = new ArrayList<String>(number);
		int i = 0;
		while(i < number) {
			//find unique url, not in registry...
			final String url = baseURL + UUID.randomUUID().toString();
			if(register.allocate(url + "/subscription", ctx.getRuntimeSessionUrl())) {
				//TODO: should I allocate also the URL?
				list.add(url);
				i++;
			}
		}
		
		return list;
	}
}
